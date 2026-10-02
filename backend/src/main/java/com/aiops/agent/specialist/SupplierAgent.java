package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SupplierAgent {

    private final ToolRegistry toolRegistry;

    public SupplierAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void compareSuppliers(AgentRunContext ctx) {
        ctx.addAgent("SUPPLIER_AGENT");
        toolRegistry.invoke("compare_suppliers", Map.of(), ctx);
    }
}
