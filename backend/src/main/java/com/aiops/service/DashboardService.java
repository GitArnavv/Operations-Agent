package com.aiops.service;

import com.aiops.domain.*;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.PaymentStatus;
import com.aiops.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final AlertRepository alertRepository;
    private final AgentRunRepository agentRunRepository;

    public DashboardService(SalesOrderRepository salesOrderRepository,
                            InventoryItemRepository inventoryItemRepository,
                            InvoiceRepository invoiceRepository,
                            SupplierRepository supplierRepository,
                            ApprovalRequestRepository approvalRequestRepository,
                            AlertRepository alertRepository,
                            AgentRunRepository agentRunRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.supplierRepository = supplierRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.alertRepository = alertRepository;
        this.agentRunRepository = agentRunRepository;
    }

    public Map<String, Object> getCommandCenterData(String tenantId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1. Attention Items (Exceptions)
        List<Map<String, Object>> attentionItems = new ArrayList<>();

        List<SalesOrder> highRiskOrders = salesOrderRepository.findByTenantIdAndDeliveryRisk(tenantId, "HIGH");
        for (SalesOrder o : highRiskOrders) {
            attentionItems.add(Map.of(
                    "id", o.getId(),
                    "type", "DELAYED_ORDER",
                    "severity", "HIGH",
                    "title", "Order #" + o.getOrderNumber() + " at risk of SLA breach",
                    "description", "Promised: " + o.getPromisedDeliveryDate() + ". Customer: " + o.getCustomerName() + ". Cause: " + o.getDelayRootCause(),
                    "link", "/orders/" + o.getOrderNumber(),
                    "recommendedAction", "Expedite replacement procurement"
            ));
        }

        List<InventoryItem> criticalStock = inventoryItemRepository.findByTenantIdAndStockoutRiskIn(tenantId, List.of("HIGH", "CRITICAL"));
        for (InventoryItem i : criticalStock) {
            attentionItems.add(Map.of(
                    "id", i.getId(),
                    "type", "STOCKOUT_RISK",
                    "severity", "HIGH",
                    "title", "Stockout Risk: " + i.getProductName(),
                    "description", "Only " + i.getAvailableStock() + " units available (" + String.format(Locale.US, "%.1f", i.getDaysOfStockRemaining()) + " days remaining). Warehouse: " + i.getWarehouseName(),
                    "link", "/inventory",
                    "recommendedAction", "Trigger safety stock replenishment"
            ));
        }

        List<Invoice> overdueInvoices = invoiceRepository.findByTenantIdAndPaymentStatus(tenantId, PaymentStatus.OVERDUE);
        for (Invoice inv : overdueInvoices) {
            BigDecimal inLakhs = inv.getTotalAmount().divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP);
            attentionItems.add(Map.of(
                    "id", inv.getId(),
                    "type", "OVERDUE_INVOICE",
                    "severity", "MEDIUM",
                    "title", "Overdue Invoice: " + inv.getEntityName(),
                    "description", "Invoice #" + inv.getInvoiceNumber() + " is overdue by " + inv.getOverdueDays() + " days. Amount: ₹" + inLakhs.toPlainString() + " Lakh",
                    "link", "/invoices/" + inv.getInvoiceNumber(),
                    "recommendedAction", "Send WhatsApp payment reminder"
            ));
        }

        data.put("attentionItems", attentionItems);

        // 2. Operational KPIs
        List<SalesOrder> allOrders = salesOrderRepository.findByTenantId(tenantId);
        BigDecimal totalRevenue = allOrders.stream().map(SalesOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOverdue = overdueInvoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Supplier> suppliers = supplierRepository.findByTenantId(tenantId);
        BigDecimal avgOnTimeRate = suppliers.isEmpty() ? BigDecimal.ZERO :
                suppliers.stream()
                        .map(Supplier::getOnTimeDeliveryRate)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(suppliers.size()), 2, java.math.RoundingMode.HALF_UP);

        List<ApprovalRequest> pendingApprovals = approvalRequestRepository.findByTenantIdAndStatus(tenantId, ApprovalStatus.PENDING);
        List<Alert> recentAlerts = alertRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        List<AgentRun> recentRuns = agentRunRepository.findByTenantIdOrderByStartedAtDesc(tenantId);

        BigDecimal revenueLakhs = totalRevenue.divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal overdueLakhs = totalOverdue.divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalRevenueInr", totalRevenue);
        kpis.put("totalRevenueLakhs", revenueLakhs.toPlainString());
        kpis.put("totalOrdersCount", allOrders.size());
        kpis.put("delayedOrdersCount", highRiskOrders.size());
        kpis.put("criticalStockCount", criticalStock.size());
        kpis.put("totalOverdueInr", totalOverdue);
        kpis.put("totalOverdueLakhs", overdueLakhs.toPlainString());
        kpis.put("averageSupplierOnTimeRate", avgOnTimeRate);
        kpis.put("pendingApprovalsCount", pendingApprovals.size());
        kpis.put("activeAlertsCount", recentAlerts.size());

        data.put("kpis", kpis);
        data.put("pendingApprovals", pendingApprovals);
        data.put("recentAlerts", recentAlerts.stream().limit(6).toList());
        data.put("recentAgentRuns", recentRuns.stream().limit(6).toList());

        return data;
    }
}
