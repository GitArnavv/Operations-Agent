package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActionAgent {

    private final ToolRegistry toolRegistry;

    public ActionAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void proposePurchaseOrder(Map<String, Object> params, AgentRunContext ctx) {
        ctx.addAgent("ACTION_AGENT");
        toolRegistry.invoke("create_purchase_request", params, ctx);
    }

    public void executeApprovedAction(String approvalRequestId, AgentRunContext ctx) {
        ctx.addAgent("ACTION_AGENT");
        toolRegistry.invoke("execute_approved_action", Map.of("approvalRequestId", approvalRequestId), ctx);
    }
}
