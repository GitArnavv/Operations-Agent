package com.aiops.service;

import com.aiops.domain.Alert;
import com.aiops.domain.InventoryItem;
import com.aiops.domain.MonitoringRule;
import com.aiops.domain.enums.AlertSeverity;
import com.aiops.domain.enums.MetricType;
import com.aiops.domain.enums.RuleOperator;
import com.aiops.repository.AlertRepository;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.MonitoringRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class MonitoringService {

    private final MonitoringRuleRepository ruleRepository;
    private final AlertRepository alertRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public MonitoringService(MonitoringRuleRepository ruleRepository,
                             AlertRepository alertRepository,
                             InventoryItemRepository inventoryItemRepository) {
        this.ruleRepository = ruleRepository;
        this.alertRepository = alertRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<MonitoringRule> getRules(String tenantId) {
        return ruleRepository.findByTenantId(tenantId);
    }

    public List<Alert> getAlerts(String tenantId) {
        return alertRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public MonitoringRule createRuleFromNaturalLanguage(String tenantId, String prompt) {
        String ruleId = "RULE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String p = prompt.toLowerCase();

        MetricType metric = MetricType.INVENTORY_DAYS_REMAINING;
        RuleOperator op = RuleOperator.LESS_THAN;
        BigDecimal threshold = BigDecimal.valueOf(7.0);
        String name = "Inventory 7-Day Stockout Watcher";

        if (p.contains("order") || p.contains("delivery") || p.contains("delay")) {
            metric = MetricType.ORDER_DELAY_DAYS;
            op = RuleOperator.GREATER_THAN;
            threshold = BigDecimal.valueOf(2.0);
            name = "Order Delivery Delay Watcher";
        } else if (p.contains("invoice") || p.contains("payment") || p.contains("overdue")) {
            metric = MetricType.INVOICE_OVERDUE_DAYS;
            op = RuleOperator.GREATER_THAN;
            threshold = BigDecimal.valueOf(15.0);
            name = "Invoice Overdue Watcher (>15 Days)";
        } else if (p.contains("supplier") || p.contains("vendor")) {
            metric = MetricType.SUPPLIER_ON_TIME_RATE;
            op = RuleOperator.LESS_THAN;
            threshold = BigDecimal.valueOf(85.0);
            name = "Supplier SLA Watcher (<85% On-Time)";
        }

        MonitoringRule rule = new MonitoringRule(
                ruleId, tenantId, name, prompt, metric, op, threshold, "DAILY", "ALL_PRODUCTS"
        );
        return ruleRepository.save(rule);
    }

    public Map<String, Object> evaluateAllRules(String tenantId) {
        List<MonitoringRule> rules = ruleRepository.findByTenantIdAndActive(tenantId, true);
        int alertsGenerated = 0;

        for (MonitoringRule rule : rules) {
            rule.setLastEvaluatedAt(Instant.now());
            ruleRepository.save(rule);

            if (rule.getMetricType() == MetricType.INVENTORY_DAYS_REMAINING) {
                List<InventoryItem> critical = inventoryItemRepository.findByTenantIdAndDaysOfStockRemainingLessThanEqual(tenantId, rule.getThreshold());
                for (InventoryItem item : critical) {
                    Alert alert = new Alert(
                            "ALT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                            tenantId, "Autonomous Monitoring: " + item.getProductName() + " stock critically low",
                            "Rule '" + rule.getRuleName() + "' triggered. Product " + item.getProductName() + " has only " + String.format(Locale.US, "%.1f", item.getDaysOfStockRemaining()) + " days stock remaining in " + item.getWarehouseName(),
                            AlertSeverity.CRITICAL, "PRODUCT", item.getProductId(), "INVENTORY_DAYS_REMAINING",
                            "Create expedited purchase requisition", "/inventory"
                    );
                    alertRepository.save(alert);
                    alertsGenerated++;
                }
            }
        }

        return Map.of(
                "evaluatedRulesCount", rules.size(),
                "alertsGeneratedCount", alertsGenerated,
                "timestamp", Instant.now()
        );
    }

    public Alert acknowledgeAlert(String tenantId, String alertId) {
        Alert alert = alertRepository.findByTenantIdAndId(tenantId, alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setAcknowledged(true);
        return alertRepository.save(alert);
    }
}
