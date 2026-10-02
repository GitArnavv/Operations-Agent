package com.aiops.agent;

import com.aiops.domain.enums.RiskLevel;
import java.math.BigDecimal;
import java.util.Map;

public class AgentActionProposal {
    private String id;
    private String actionType; // CREATE_PURCHASE_ORDER, DISPATCH_ORDER, NOTIFY_SUPPLIER
    private RiskLevel riskLevel;
    private String title;
    private String description;
    private String reason;
    private BigDecimal estimatedCostInr;
    private String expectedImpact;
    private String affectedEntities;
    private Map<String, Object> payload;
    private boolean requiresApproval;

    public AgentActionProposal() {}

    public AgentActionProposal(String id, String actionType, RiskLevel riskLevel, String title, String description, String reason, BigDecimal estimatedCostInr, String expectedImpact, String affectedEntities, Map<String, Object> payload, boolean requiresApproval) {
        this.id = id;
        this.actionType = actionType;
        this.riskLevel = riskLevel;
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.estimatedCostInr = estimatedCostInr;
        this.expectedImpact = expectedImpact;
        this.affectedEntities = affectedEntities;
        this.payload = payload;
        this.requiresApproval = requiresApproval;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public boolean isRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(boolean requiresApproval) { this.requiresApproval = requiresApproval; }
}
