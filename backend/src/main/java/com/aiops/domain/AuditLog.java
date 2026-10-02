package com.aiops.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Immutable AuditLog Entity.
 * Enforces tamper-proof record keeping of all sensitive tool invocations,
 * acting identities, prompt contexts, state diffs, and network telemetry.
 */
@Entity
@Immutable
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_tenant", columnList = "tenantId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_actor", columnList = "actingUserId"),
        @Index(name = "idx_audit_tool", columnList = "toolExecuted")
})
public class AuditLog {

    @Id
    @Column(updatable = false, nullable = false)
    private String id;

    @org.hibernate.annotations.TenantId
    @Column(nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(nullable = false, updatable = false)
    private String actingUserId;

    @Column(updatable = false)
    private String actingAgentId;

    @Column(updatable = false)
    private String actorName;

    @Column(nullable = false, updatable = false)
    private String action; // e.g. "APPROVAL_GATE_CREATED", "PURCHASE_ORDER_APPROVED", "WHATSAPP_REMINDER_TRIGGERED"

    @Column(length = 255, updatable = false)
    private String toolExecuted;

    @Column(length = 4000, updatable = false)
    private String promptContext;

    @Column(updatable = false)
    private String entityType;

    @Column(updatable = false)
    private String entityId;

    @Column(length = 4000, updatable = false)
    private String beforeEntityState;

    @Column(length = 4000, updatable = false)
    private String afterEntityState;

    @Column(length = 1000, updatable = false)
    private String reason;

    @Column(updatable = false)
    private String requestId;

    @Column(updatable = false)
    private String agentRunId;

    @Column(updatable = false)
    private String ipAddress;

    // For backward compatibility with existing queries
    @Column(updatable = false)
    private String actorId;

    @Column(length = 4000, updatable = false)
    private String beforeState;

    @Column(length = 4000, updatable = false)
    private String afterState;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}

    /**
     * Primary constructor for comprehensive immutable audit logging.
     */
    public AuditLog(String id,
                    String tenantId,
                    String actingUserId,
                    String actingAgentId,
                    String actorName,
                    String action,
                    String toolExecuted,
                    String promptContext,
                    String entityType,
                    String entityId,
                    String beforeEntityState,
                    String afterEntityState,
                    String reason,
                    String requestId,
                    String agentRunId,
                    String ipAddress) {
        this.id = id;
        this.tenantId = tenantId;
        this.timestamp = Instant.now();
        this.actingUserId = actingUserId != null ? actingUserId : "system";
        this.actingAgentId = actingAgentId;
        this.actorName = actorName != null ? actorName : (actingAgentId != null ? actingAgentId : actingUserId);
        this.action = action;
        this.toolExecuted = toolExecuted;
        this.promptContext = promptContext;
        this.entityType = entityType;
        this.entityId = entityId;
        this.beforeEntityState = beforeEntityState;
        this.afterEntityState = afterEntityState;
        this.reason = reason;
        this.requestId = requestId;
        this.agentRunId = agentRunId;
        this.ipAddress = ipAddress;

        // Populate legacy alias fields
        this.actorId = this.actingUserId;
        this.beforeState = beforeEntityState;
        this.afterState = afterEntityState;
        this.createdAt = this.timestamp;
    }

    /**
     * Backward-compatible constructor for existing codebase callers.
     */
    public AuditLog(String id,
                    String tenantId,
                    String actorId,
                    String actorName,
                    String action,
                    String entityType,
                    String entityId,
                    String beforeState,
                    String afterState,
                    String reason,
                    String requestId,
                    String agentRunId) {
        this(id, tenantId, actorId, null, actorName, action, null, null, entityType, entityId, beforeState, afterState, reason, requestId, agentRunId, null);
    }

    // Immutable getters only - no public setters to enforce record integrity
    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public Instant getTimestamp() { return timestamp; }
    public String getActingUserId() { return actingUserId; }
    public String getActingAgentId() { return actingAgentId; }
    public String getActorName() { return actorName; }
    public String getAction() { return action; }
    public String getToolExecuted() { return toolExecuted; }
    public String getPromptContext() { return promptContext; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getBeforeEntityState() { return beforeEntityState; }
    public String getAfterEntityState() { return afterEntityState; }
    public String getReason() { return reason; }
    public String getRequestId() { return requestId; }
    public String getAgentRunId() { return agentRunId; }
    public String getIpAddress() { return ipAddress; }

    // Legacy getters
    public String getActorId() { return actorId != null ? actorId : actingUserId; }
    public String getBeforeState() { return beforeState != null ? beforeState : beforeEntityState; }
    public String getAfterState() { return afterState != null ? afterState : afterEntityState; }
    public Instant getCreatedAt() { return createdAt != null ? createdAt : timestamp; }
}
