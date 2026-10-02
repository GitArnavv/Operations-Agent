package com.aiops.domain;

import com.aiops.domain.enums.AgentRunStatus;
import com.aiops.domain.enums.ApprovalStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_runs")
public class AgentRun {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    private String userId;
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRunStatus status;

    @Column(length = 2000)
    private String userPrompt;

    @Column(length = 4000)
    private String finalResponse;

    private String currentStep;
    private String agentsUsed; // JSON or comma-separated list: "DATA_AGENT, INVENTORY_AGENT, ORDER_AGENT, ACTION_AGENT"
    private String toolsUsed;  // "get_order, get_inventory, calculate_reorder_quantity, request_approval"

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private String pendingApprovalId;

    @Column(length = 4000)
    private String executionTraceSummary;

    private long durationMs;

    @Column(nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    private Instant completedAt;

    public AgentRun() {}

    public AgentRun(String id, String tenantId, String userId, String conversationId, AgentRunStatus status, String userPrompt, String currentStep) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.conversationId = conversationId;
        this.status = status;
        this.userPrompt = userPrompt;
        this.currentStep = currentStep;
        this.startedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public AgentRunStatus getStatus() { return status; }
    public void setStatus(AgentRunStatus status) { this.status = status; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getFinalResponse() { return finalResponse; }
    public void setFinalResponse(String finalResponse) { this.finalResponse = finalResponse; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getAgentsUsed() { return agentsUsed; }
    public void setAgentsUsed(String agentsUsed) { this.agentsUsed = agentsUsed; }

    public String getToolsUsed() { return toolsUsed; }
    public void setToolsUsed(String toolsUsed) { this.toolsUsed = toolsUsed; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getPendingApprovalId() { return pendingApprovalId; }
    public void setPendingApprovalId(String pendingApprovalId) { this.pendingApprovalId = pendingApprovalId; }

    public String getExecutionTraceSummary() { return executionTraceSummary; }
    public void setExecutionTraceSummary(String executionTraceSummary) { this.executionTraceSummary = executionTraceSummary; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
