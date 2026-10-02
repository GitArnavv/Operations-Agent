package com.aiops.service;

import com.aiops.domain.ActionApproval;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.dto.ActionEvaluationRequest;
import com.aiops.dto.ActionableCardResponse;
import com.aiops.repository.ActionApprovalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * ApprovalPolicyService enforces strict governance gates for high-impact autonomous actions.
 * Prevents immediate execution of:
 * 1. Vendor WhatsApp messages / broadcasts.
 * 2. Purchase orders or commitments exceeding ₹50,000 INR.
 * 3. Alterations to credit terms, payment limits, or credit periods.
 */
@Service
public class ApprovalPolicyService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalPolicyService.class);

    public static final BigDecimal PURCHASE_ORDER_THRESHOLD_INR = new BigDecimal("50000.00");

    private final ActionApprovalRepository actionApprovalRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ApprovalPolicyService(ActionApprovalRepository actionApprovalRepository,
                                 AuditService auditService,
                                 ObjectMapper objectMapper) {
        this.actionApprovalRepository = actionApprovalRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Determines whether an autonomous action requires human management approval.
     */
    public boolean isApprovalRequired(ActionEvaluationRequest request) {
        if (request == null || request.actionType() == null) {
            return false;
        }

        String type = request.actionType().trim().toUpperCase();

        // 1. WhatsApp vendor communications cannot execute autonomously
        if (type.contains("WHATSAPP") || (request.payload() != null && "WHATSAPP".equalsIgnoreCase(String.valueOf(request.payload().get("channel"))))) {
            log.info("Policy Gate: Action '{}' requires approval because it targets external WhatsApp communications.", type);
            return true;
        }

        // 2. Purchase orders or financial commitments > ₹50,000 INR
        if (type.contains("PURCHASE_ORDER") || type.contains("PO_") || type.contains("EXPEDITE") || type.contains("ORDER")) {
            BigDecimal amount = request.estimatedAmount();
            if (amount != null && amount.compareTo(PURCHASE_ORDER_THRESHOLD_INR) > 0) {
                log.info("Policy Gate: Action '{}' with estimated cost ₹{} exceeds threshold ₹{} and requires approval.",
                        type, amount, PURCHASE_ORDER_THRESHOLD_INR);
                return true;
            }
        }

        // 3. Altering credit terms, credit limits, or payment terms
        if (type.contains("CREDIT_TERMS") || type.contains("PAYMENT_TERMS") || type.contains("CREDIT_LIMIT")) {
            log.info("Policy Gate: Action '{}' alters credit terms and requires approval.", type);
            return true;
        }

        // 4. Any action explicitly marked with HIGH_RISK
        if (request.riskLevel() == RiskLevel.HIGH_RISK) {
            log.info("Policy Gate: Action '{}' is classified as HIGH_RISK and requires approval.", type);
            return true;
        }

        return false;
    }

    /**
     * Evaluates an action against policy rules. If critical, stores the action payload in the
     * 'action_approvals' table with status 'PENDING' and returns an actionable card.
     */
    @Transactional
    public ActionableCardResponse evaluateAndGateAction(String tenantId,
                                                        String actingUserId,
                                                        String actingAgentId,
                                                        ActionEvaluationRequest request) {
        boolean approvalRequired = isApprovalRequired(request);

        if (!approvalRequired) {
            log.info("Action '{}' passed policy evaluation without gate requirement.", request.actionType());
            return new ActionableCardResponse(
                    false,
                    null,
                    null,
                    "AUTO_APPROVED",
                    request.actionType(),
                    request.title(),
                    request.description(),
                    request.reason(),
                    request.riskLevel() != null ? request.riskLevel().name() : RiskLevel.LOW_RISK.name(),
                    request.estimatedAmount(),
                    request.currency() != null ? request.currency() : "INR",
                    request.targetEntityId(),
                    request.targetEntityType(),
                    List.of(),
                    Map.of(),
                    Instant.now(),
                    null
            );
        }

        // Generate pending transaction token & approval ID
        String transactionToken = "TXN-" + UUID.randomUUID().toString();
        String approvalId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String payloadJson = "{}";
        if (request.payload() != null) {
            try {
                payloadJson = objectMapper.writeValueAsString(request.payload());
            } catch (Exception e) {
                log.warn("Failed to serialize action payload for token {}", transactionToken, e);
            }
        }

        RiskLevel assessedRisk = request.riskLevel() != null ? request.riskLevel() : RiskLevel.HIGH_RISK;
        Instant expiresAt = Instant.now().plus(Duration.ofHours(48));

        ActionApproval approval = new ActionApproval(
                approvalId,
                transactionToken,
                tenantId,
                request.actionType(),
                request.title(),
                request.description(),
                request.reason(),
                assessedRisk,
                request.estimatedAmount(),
                request.currency(),
                payloadJson,
                request.targetEntityId(),
                request.targetEntityType(),
                actingAgentId != null ? actingAgentId : "GeminiFlashOperationsAgent",
                expiresAt
        );

        actionApprovalRepository.save(approval);

        // Record immutable audit entry
        auditService.recordAudit(
                tenantId,
                actingUserId,
                actingAgentId,
                "APPROVAL_GATE_CREATED",
                request.actionType(),
                request.promptContext(),
                request.targetEntityType(),
                request.targetEntityId(),
                null,
                "Status: PENDING",
                "Action held at policy gate: " + request.title()
        );

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("approve", "/api/v1/action-approvals/" + transactionToken + "/approve");
        endpoints.put("reject", "/api/v1/action-approvals/" + transactionToken + "/reject");
        endpoints.put("acknowledge", "/api/v1/action-approvals/" + transactionToken + "/acknowledge");

        return new ActionableCardResponse(
                true,
                transactionToken,
                approvalId,
                ApprovalStatus.PENDING.name(),
                request.actionType(),
                request.title(),
                request.description(),
                request.reason(),
                assessedRisk.name(),
                request.estimatedAmount(),
                request.currency() != null ? request.currency() : "INR",
                request.targetEntityId(),
                request.targetEntityType(),
                List.of("ACKNOWLEDGE", "APPROVE", "REJECT"),
                endpoints,
                approval.getCreatedAt(),
                expiresAt
        );
    }

    /**
     * Approves and authorizes the pending action.
     */
    @Transactional
    public ActionApproval approveAction(String tenantId, String tokenOrId, String userId, String notes) {
        ActionApproval approval = findApprovalByTokenOrId(tenantId, tokenOrId);

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Action approval is not in PENDING status (current: " + approval.getStatus() + ")");
        }

        String beforeState = "Status: PENDING, DecidedBy: null";
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setDecidedByUserId(userId);
        approval.setDecisionNotes(notes != null ? notes : "Approved by authorized operations manager.");
        approval.setDecidedAt(Instant.now());
        approval.setExecutedAt(Instant.now());
        actionApprovalRepository.save(approval);

        String afterState = "Status: APPROVED, DecidedBy: " + userId + ", DecisionNotes: " + approval.getDecisionNotes();

        // Record immutable audit entry
        auditService.recordAudit(
                tenantId,
                userId,
                "ApprovalPolicyService",
                "ACTION_APPROVED",
                approval.getActionType(),
                null,
                approval.getTargetEntityType(),
                approval.getTargetEntityId(),
                beforeState,
                afterState,
                approval.getDecisionNotes()
        );

        log.info("Action approval '{}' (Token: {}) APPROVED by user '{}'", approval.getId(), approval.getTransactionToken(), userId);
        return approval;
    }

    /**
     * Rejects the pending action.
     */
    @Transactional
    public ActionApproval rejectAction(String tenantId, String tokenOrId, String userId, String reason) {
        ActionApproval approval = findApprovalByTokenOrId(tenantId, tokenOrId);

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Action approval is not in PENDING status (current: " + approval.getStatus() + ")");
        }

        String beforeState = "Status: PENDING, DecidedBy: null";
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setDecidedByUserId(userId);
        approval.setDecisionNotes(reason != null ? reason : "Rejected by operations manager.");
        approval.setDecidedAt(Instant.now());
        actionApprovalRepository.save(approval);

        String afterState = "Status: REJECTED, DecidedBy: " + userId + ", Reason: " + approval.getDecisionNotes();

        // Record immutable audit entry
        auditService.recordAudit(
                tenantId,
                userId,
                "ApprovalPolicyService",
                "ACTION_REJECTED",
                approval.getActionType(),
                null,
                approval.getTargetEntityType(),
                approval.getTargetEntityId(),
                beforeState,
                afterState,
                approval.getDecisionNotes()
        );

        log.info("Action approval '{}' (Token: {}) REJECTED by user '{}'", approval.getId(), approval.getTransactionToken(), userId);
        return approval;
    }

    /**
     * Acknowledges the pending action notification without immediate mutation.
     */
    @Transactional
    public ActionApproval acknowledgeAction(String tenantId, String tokenOrId, String userId, String notes) {
        ActionApproval approval = findApprovalByTokenOrId(tenantId, tokenOrId);

        String beforeState = "Status: " + approval.getStatus();
        approval.setStatus(ApprovalStatus.ACKNOWLEDGED);
        approval.setDecidedByUserId(userId);
        approval.setDecisionNotes(notes != null ? notes : "Acknowledged by operations manager.");
        approval.setDecidedAt(Instant.now());
        actionApprovalRepository.save(approval);

        String afterState = "Status: ACKNOWLEDGED, User: " + userId;

        auditService.recordAudit(
                tenantId,
                userId,
                "ApprovalPolicyService",
                "ACTION_ACKNOWLEDGED",
                approval.getActionType(),
                null,
                approval.getTargetEntityType(),
                approval.getTargetEntityId(),
                beforeState,
                afterState,
                approval.getDecisionNotes()
        );

        log.info("Action approval '{}' (Token: {}) ACKNOWLEDGED by user '{}'", approval.getId(), approval.getTransactionToken(), userId);
        return approval;
    }

    public List<ActionApproval> getApprovals(String tenantId) {
        return actionApprovalRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public Optional<ActionApproval> getApprovalByToken(String tenantId, String token) {
        return actionApprovalRepository.findByTenantIdAndTransactionToken(tenantId, token);
    }

    public Optional<ActionApproval> getApprovalById(String tenantId, String id) {
        return actionApprovalRepository.findByTenantIdAndId(tenantId, id);
    }

    private ActionApproval findApprovalByTokenOrId(String tenantId, String tokenOrId) {
        return actionApprovalRepository.findByTenantIdAndTransactionToken(tenantId, tokenOrId)
                .or(() -> actionApprovalRepository.findByTenantIdAndId(tenantId, tokenOrId))
                .orElseThrow(() -> new IllegalArgumentException("Action approval not found for token/ID: " + tokenOrId));
    }
}
