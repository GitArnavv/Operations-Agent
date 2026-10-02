package com.aiops.controller;

import com.aiops.domain.AuditLog;
import com.aiops.security.SecurityUtils;
import com.aiops.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'OPERATIONS_MANAGER')")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(auditService.getAuditLogs(tenantId));
    }
}
