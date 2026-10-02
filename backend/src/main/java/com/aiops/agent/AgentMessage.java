package com.aiops.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AgentMessage {
    private String id;
    private String role; // "USER", "AGENT", "SYSTEM"
    private String content;
    private String runId;
    private String reasoningSummary; // User-safe concise summary (e.g. "Investigated 4 orders, 2 supplier records, inventory availability")
    private List<CitationEvidence> citations = new ArrayList<>();
    private AgentActionProposal proposedAction;
    private List<ToolExecutionResult> toolExecutions = new ArrayList<>();
    private Instant timestamp = Instant.now();

    public AgentMessage() {}

    public AgentMessage(String id, String role, String content, String runId) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.runId = runId;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public String getReasoningSummary() { return reasoningSummary; }
    public void setReasoningSummary(String reasoningSummary) { this.reasoningSummary = reasoningSummary; }

    public List<CitationEvidence> getCitations() { return citations; }
    public void setCitations(List<CitationEvidence> citations) { this.citations = citations; }

    public AgentActionProposal getProposedAction() { return proposedAction; }
    public void setProposedAction(AgentActionProposal proposedAction) { this.proposedAction = proposedAction; }

    public List<ToolExecutionResult> getToolExecutions() { return toolExecutions; }
    public void setToolExecutions(List<ToolExecutionResult> toolExecutions) { this.toolExecutions = toolExecutions; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
