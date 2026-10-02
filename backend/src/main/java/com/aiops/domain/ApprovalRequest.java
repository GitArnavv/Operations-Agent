package com.aiops.domain;

import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.RiskLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String actionType; // CREATE_PURCHASE_ORDER, DISPATCH_SHIPMENT, SEND_SUPPLIER_PO, ADJUST_INVENTORY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String reason;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal estimatedCostInr;

    private String expectedImpact;
    private String affectedEntities; // e.g. "Order ORD-1042, Product PROD-WIR-001, Supplier Polycab"

    @Column(length = 4000)
    private String proposedPayload; // JSON string of parameters to execute

    private String requestedByAgent; // "ActionAgent", "InventoryAgent", or User ID
    private String decidedByUserId;
    private String decisionNotes;
    private Instant decidedAt;
    private Instant executedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ApprovalRequest() {}

    public ApprovalRequest(String id, String tenantId, String title, String actionType, RiskLevel riskLevel, String description, String reason, BigDecimal estimatedCostInr, String expectedImpact, String affectedEntities, String proposedPayload, String requestedByAgent) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.actionType = actionType;
        this.riskLevel = riskLevel;
        this.status = ApprovalStatus.PENDING;
        this.description = description;
        this.reason = reason;
        this.estimatedCostInr = estimatedCostInr;
        this.expectedImpact = expectedImpact;
        this.affectedEntities = affectedEntities;
        this.proposedPayload = proposedPayload;
        this.requestedByAgent = requestedByAgent;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BigDecimal getEstimatedCostInr() { return estimatedCostInr; }
    public void setEstimatedCostInr(BigDecimal estimatedCostInr) { this.estimatedCostInr = estimatedCostInr; }

    public String getExpectedImpact() { return expectedImpact; }
    public void setExpectedImpact(String expectedImpact) { this.expectedImpact = expectedImpact; }

    public String getAffectedEntities() { return affectedEntities; }
    public void setAffectedEntities(String affectedEntities) { this.affectedEntities = affectedEntities; }

    public String getProposedPayload() { return proposedPayload; }
    public void setProposedPayload(String proposedPayload) { this.proposedPayload = proposedPayload; }

    public String getRequestedByAgent() { return requestedByAgent; }
    public void setRequestedByAgent(String requestedByAgent) { this.requestedByAgent = requestedByAgent; }

    public String getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(String decidedByUserId) { this.decidedByUserId = decidedByUserId; }

    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
