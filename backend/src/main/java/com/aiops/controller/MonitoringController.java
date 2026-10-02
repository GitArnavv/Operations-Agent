package com.aiops.controller;

import com.aiops.domain.Alert;
import com.aiops.domain.MonitoringRule;
import com.aiops.security.SecurityUtils;
import com.aiops.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<MonitoringRule>> getRules() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(monitoringService.getRules(tenantId));
    }

    @PostMapping("/rules")
    public ResponseEntity<MonitoringRule> createRule(@RequestBody Map<String, String> request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String prompt = request.get("prompt");
        MonitoringRule rule = monitoringService.createRuleFromNaturalLanguage(tenantId, prompt);
        return ResponseEntity.ok(rule);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(monitoringService.getAlerts(tenantId));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateRules() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(monitoringService.evaluateAllRules(tenantId));
    }

    @PostMapping("/alerts/{id}/ack")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(monitoringService.acknowledgeAlert(tenantId, id));
    }
}
