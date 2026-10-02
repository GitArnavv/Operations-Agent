package com.aiops.domain;

import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.RiskLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * ActionApproval Entity mapped to the 'action_approvals' table.
 * Holds pending transactions, sensitive payloads, and approval gates.
 */
@Entity
@Table(name = "action_approvals", indexes = {
        @Index(name = "idx_action_app_tenant", columnList = "tenantId"),
        @Index(name = "idx_action_app_token", columnList = "transactionToken", unique = true),
        @Index(name = "idx_action_app_status", columnList = "status"),
        @Index(name = "idx_action_app_type", columnList = "actionType")
})
public class ActionApproval {

    @Id
    @Column(nullable = false, length = 64)
    private String id; // e.g. APP-XXXXXXXX

    @Column(nullable = false, unique = true, length = 128)
    private String transactionToken; // e.g. TXN-XXXXXXXX-XXXX-...

    @org.hibernate.annotations.TenantId
    @Column(nullable = false, updatable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String actionType; // TRIGGER_WHATSAPP_REMINDER, APPROVE_PURCHASE_ORDER, ALTER_CREDIT_TERMS

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RiskLevel riskLevel; // LOW_RISK, MEDIUM_RISK, HIGH_RISK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal estimatedAmount;

    @Column(length = 8)
    private String currency = "INR";

    @Column(length = 4000)
    private String actionPayload; // JSON serialized parameters required for execution

    @Column(length = 64)
    private String targetEntityId; // e.g. ORD-1042, SUP-POLYCAB

    @Column(length = 64)
    private String targetEntityType; // ORDER, SUPPLIER, INVOICE, CUSTOMER

    @Column(length = 64)
    private String requestedBy; // e.g. GeminiFlashAgent, usr_sharma_ops

    @Column(length = 64)
    private String decidedByUserId;

    @Column(length = 2000)
    private String decisionNotes;

    private Instant decidedAt;

    private Instant executedAt;

    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ActionApproval() {}

    public ActionApproval(String id,
                          String transactionToken,
                          String tenantId,
                          String actionType,
                          String title,
                          String description,
                          String reason,
                          RiskLevel riskLevel,
                          BigDecimal estimatedAmount,
                          String currency,
                          String actionPayload,
                          String targetEntityId,
                          String targetEntityType,
                          String requestedBy,
                          Instant expiresAt) {
        this.id = id;
        this.transactionToken = transactionToken;
        this.tenantId = tenantId;
        this.actionType = actionType;
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.riskLevel = riskLevel;
        this.status = ApprovalStatus.PENDING;
        this.estimatedAmount = estimatedAmount;
        this.currency = currency != null ? currency : "INR";
        this.actionPayload = actionPayload;
        this.targetEntityId = targetEntityId;
        this.targetEntityType = targetEntityType;
        this.requestedBy = requestedBy;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTransactionToken() { return transactionToken; }
    public void setTransactionToken(String transactionToken) { this.transactionToken = transactionToken; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }

    public String getTargetEntityId() { return targetEntityId; }
    public void setTargetEntityId(String targetEntityId) { this.targetEntityId = targetEntityId; }

    public String getTargetEntityType() { return targetEntityType; }
    public void setTargetEntityType(String targetEntityType) { this.targetEntityType = targetEntityType; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(String decidedByUserId) { this.decidedByUserId = decidedByUserId; }

    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
