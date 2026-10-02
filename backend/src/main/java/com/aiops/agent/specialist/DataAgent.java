package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataAgent {

    private final ToolRegistry toolRegistry;

    public DataAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void retrieveOrders(Map<String, Object> params, AgentRunContext ctx) {
        ctx.addAgent("DATA_AGENT");
        toolRegistry.invoke("get_orders", params, ctx);
    }

    public void retrieveInvoices(Map<String, Object> params, AgentRunContext ctx) {
        ctx.addAgent("DATA_AGENT");
        toolRegistry.invoke("get_invoices", params, ctx);
    }
}
