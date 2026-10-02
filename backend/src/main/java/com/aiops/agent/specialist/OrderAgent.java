package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderAgent {

    private final ToolRegistry toolRegistry;

    public OrderAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void investigateDelay(String orderNumber, AgentRunContext ctx) {
        ctx.addAgent("ORDER_AGENT");
        toolRegistry.invoke("investigate_order_delay", Map.of("orderNumber", orderNumber), ctx);
    }
}
