package com.aiops.controller;

import com.aiops.domain.ApprovalRequest;
import com.aiops.security.SecurityUtils;
import com.aiops.service.ApprovalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<ApprovalRequest>> getApprovals() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(approvalService.getApprovals(tenantId));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalRequest> approve(@PathVariable String id,
                                                   @RequestBody(required = false) Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        String notes = body != null ? body.get("notes") : null;

        ApprovalRequest approved = approvalService.approveAction(tenantId, id, userId, notes);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<ApprovalRequest> reject(@PathVariable String id,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        String reason = body != null ? body.get("reason") : null;

        ApprovalRequest rejected = approvalService.rejectAction(tenantId, id, userId, reason);
        return ResponseEntity.ok(rejected);
    }
}
