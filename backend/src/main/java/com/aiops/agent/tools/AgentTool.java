package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.enums.RiskLevel;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    RiskLevel getRiskLevel();
    String getRequiredPermission();
    ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx);
}
