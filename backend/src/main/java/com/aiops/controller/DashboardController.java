package com.aiops.controller;

import com.aiops.security.SecurityUtils;
import com.aiops.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(dashboardService.getCommandCenterData(tenantId));
    }
}
