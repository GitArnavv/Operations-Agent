package com.aiops.service;

import com.aiops.agent.tools.ToolRegistry;
import com.aiops.domain.ApprovalRequest;
import com.aiops.domain.AuditLog;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.repository.ApprovalRequestRepository;
import com.aiops.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final ToolRegistry toolRegistry;

    public ApprovalService(ApprovalRequestRepository approvalRequestRepository,
                           AuditLogRepository auditLogRepository,
                           ToolRegistry toolRegistry) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.toolRegistry = toolRegistry;
    }

    public List<ApprovalRequest> getApprovals(String tenantId) {
        return approvalRequestRepository.findByTenantId(tenantId);
    }

    public Optional<ApprovalRequest> getApprovalById(String tenantId, String id) {
        return approvalRequestRepository.findByTenantIdAndId(tenantId, id);
    }

    public ApprovalRequest approveAction(String tenantId, String approvalId, String userId, String notes) {
        ApprovalRequest req = approvalRequestRepository.findByTenantIdAndId(tenantId, approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        req.setStatus(ApprovalStatus.APPROVED);
        req.setDecidedByUserId(userId);
        req.setDecisionNotes(notes != null ? notes : "Approved by authorized operations manager.");
        req.setDecidedAt(Instant.now());
        approvalRequestRepository.save(req);

        // Record audit log
        AuditLog audit = new AuditLog(
                "AUD-" + UUID.randomUUID().toString().substring(0, 8),
                tenantId, userId, "Human Approver", "APPROVE_ACTION", "APPROVAL_REQUEST", approvalId,
                "Status: PENDING", "Status: APPROVED", req.getDecisionNotes(), null, null
        );
        auditLogRepository.save(audit);

        // Automatically trigger execution tool for approved action
        toolRegistry.getTool("execute_approved_action").ifPresent(tool -> {
            com.aiops.agent.AgentRunContext ctx = new com.aiops.agent.AgentRunContext(
                    "RUN-APP-EXEC", tenantId, userId, "conv_approval", "Executing approved action #" + approvalId
            );
            tool.execute(Map.of("approvalRequestId", approvalId), ctx);
        });

        return req;
    }

    public ApprovalRequest rejectAction(String tenantId, String approvalId, String userId, String reason) {
        ApprovalRequest req = approvalRequestRepository.findByTenantIdAndId(tenantId, approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        req.setStatus(ApprovalStatus.REJECTED);
        req.setDecidedByUserId(userId);
        req.setDecisionNotes(reason != null ? reason : "Action rejected by operations manager.");
        req.setDecidedAt(Instant.now());
        approvalRequestRepository.save(req);

        // Record audit log
        AuditLog audit = new AuditLog(
                "AUD-" + UUID.randomUUID().toString().substring(0, 8),
                tenantId, userId, "Human Approver", "REJECT_ACTION", "APPROVAL_REQUEST", approvalId,
                "Status: PENDING", "Status: REJECTED", req.getDecisionNotes(), null, null
        );
        auditLogRepository.save(audit);

        return req;
    }
}
