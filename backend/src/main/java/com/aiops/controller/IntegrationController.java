package com.aiops.controller;

import com.aiops.domain.Integration;
import com.aiops.security.SecurityUtils;
import com.aiops.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping
    public ResponseEntity<List<Integration>> getIntegrations() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(integrationService.getIntegrations(tenantId));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<Integration> syncIntegration(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(integrationService.triggerSync(tenantId, id));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<Integration> toggleIntegration(@PathVariable String id,
                                                         @RequestBody Map<String, Boolean> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        boolean enabled = body.getOrDefault("enabled", true);
        return ResponseEntity.ok(integrationService.toggleIntegration(tenantId, id, enabled));
    }
}
