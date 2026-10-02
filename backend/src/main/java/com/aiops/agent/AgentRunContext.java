package com.aiops.agent;

import com.aiops.domain.enums.AgentRunStatus;

import java.util.ArrayList;
import java.util.List;

public class AgentRunContext {
    private String runId;
    private String tenantId;
    private String userId;
    private String conversationId;
    private String userPrompt;
    private AgentRunStatus status;
    private String currentStep;
    private List<String> agentsUsed = new ArrayList<>();
    private List<String> toolsUsed = new ArrayList<>();
    private List<CitationEvidence> collectedEvidence = new ArrayList<>();
    private List<ToolExecutionResult> toolExecutions = new ArrayList<>();
    private AgentActionProposal proposedAction;
    private String finalResponse;
    private String reasoningSummary;

    public AgentRunContext() {}

    public AgentRunContext(String runId, String tenantId, String userId, String conversationId, String userPrompt) {
        this.runId = runId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.conversationId = conversationId;
        this.userPrompt = userPrompt;
        this.status = AgentRunStatus.PLANNED;
        this.currentStep = "INTENT_CLASSIFICATION";
    }

    public void addAgent(String agentName) {
        if (!agentsUsed.contains(agentName)) {
            agentsUsed.add(agentName);
        }
    }

    public void addTool(String toolName) {
        if (!toolsUsed.contains(toolName)) {
            toolsUsed.add(toolName);
        }
    }

    public void addEvidence(CitationEvidence evidence) {
        collectedEvidence.add(evidence);
    }

    public void addToolExecution(ToolExecutionResult execution) {
        toolExecutions.add(execution);
    }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public AgentRunStatus getStatus() { return status; }
    public void setStatus(AgentRunStatus status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public List<String> getAgentsUsed() { return agentsUsed; }
    public void setAgentsUsed(List<String> agentsUsed) { this.agentsUsed = agentsUsed; }

    public List<String> getToolsUsed() { return toolsUsed; }
    public void setToolsUsed(List<String> toolsUsed) { this.toolsUsed = toolsUsed; }

    public List<CitationEvidence> getCollectedEvidence() { return collectedEvidence; }
    public void setCollectedEvidence(List<CitationEvidence> collectedEvidence) { this.collectedEvidence = collectedEvidence; }

    public List<ToolExecutionResult> getToolExecutions() { return toolExecutions; }
    public void setToolExecutions(List<ToolExecutionResult> toolExecutions) { this.toolExecutions = toolExecutions; }

    public AgentActionProposal getProposedAction() { return proposedAction; }
    public void setProposedAction(AgentActionProposal proposedAction) { this.proposedAction = proposedAction; }

    public String getFinalResponse() { return finalResponse; }
    public void setFinalResponse(String finalResponse) { this.finalResponse = finalResponse; }

    public String getReasoningSummary() { return reasoningSummary; }
    public void setReasoningSummary(String reasoningSummary) { this.reasoningSummary = reasoningSummary; }
}
