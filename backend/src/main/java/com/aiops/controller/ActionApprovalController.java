package com.aiops.controller;

import com.aiops.domain.ActionApproval;
import com.aiops.dto.ActionEvaluationRequest;
import com.aiops.dto.ActionableCardResponse;
import com.aiops.security.SecurityUtils;
import com.aiops.service.ApprovalPolicyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing the approval gate and decision actions
 * (Acknowledge / Approve / Reject) for high-impact autonomous actions.
 */
@RestController
@RequestMapping("/api/v1/action-approvals")
public class ActionApprovalController {

    private final ApprovalPolicyService approvalPolicyService;

    public ActionApprovalController(ApprovalPolicyService approvalPolicyService) {
        this.approvalPolicyService = approvalPolicyService;
    }

    /**
     * Evaluates an action against governance policy.
     * If critical, generates a pending transaction token and stores the payload in 'action_approvals' table.
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<ActionableCardResponse> evaluate(@RequestBody ActionEvaluationRequest request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();

        ActionableCardResponse response = approvalPolicyService.evaluateAndGateAction(
                tenantId, userId, "GeminiFlashOperationsAgent", request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all action approvals for the authenticated tenant.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<ActionApproval>> getApprovals() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(approvalPolicyService.getApprovals(tenantId));
    }

    /**
     * Retrieves an approval by its transaction token or ID.
     */
    @GetMapping("/{tokenOrId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<ActionApproval> getApproval(@PathVariable String tokenOrId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return approvalPolicyService.getApprovalByToken(tenantId, tokenOrId)
                .or(() -> approvalPolicyService.getApprovalById(tenantId, tokenOrId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Approves the pending action, triggering execution and recording immutable audit entry.
     * Restricted to ROLE_ADMIN for executive governance.
     */
    @PostMapping("/{tokenOrId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionApproval> approve(@PathVariable String tokenOrId,
                                                  @RequestBody(required = false) Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        String notes = body != null ? body.get("notes") : null;

        ActionApproval approved = approvalPolicyService.approveAction(tenantId, tokenOrId, userId, notes);
        return ResponseEntity.ok(approved);
    }

    /**
     * Rejects the pending action with a reason.
     */
    @PostMapping("/{tokenOrId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<ActionApproval> reject(@PathVariable String tokenOrId,
                                                 @RequestBody(required = false) Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        String reason = body != null ? body.get("reason") : null;

        ActionApproval rejected = approvalPolicyService.rejectAction(tenantId, tokenOrId, userId, reason);
        return ResponseEntity.ok(rejected);
    }

    /**
     * Acknowledges the pending action notification without immediate mutation.
     */
    @PostMapping("/{tokenOrId}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<ActionApproval> acknowledge(@PathVariable String tokenOrId,
                                                      @RequestBody(required = false) Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();
        String notes = body != null ? body.get("notes") : null;

        ActionApproval ack = approvalPolicyService.acknowledgeAction(tenantId, tokenOrId, userId, notes);
        return ResponseEntity.ok(ack);
    }
}
