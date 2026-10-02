package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryAgent {

    private final ToolRegistry toolRegistry;

    public InventoryAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void analyzeCriticalStock(AgentRunContext ctx) {
        ctx.addAgent("INVENTORY_AGENT");
        toolRegistry.invoke("get_inventory", Map.of("criticalOnly", true), ctx);
    }

    public void checkProductInventory(String productName, AgentRunContext ctx) {
        ctx.addAgent("INVENTORY_AGENT");
        toolRegistry.invoke("get_product_inventory", Map.of("productQuery", productName), ctx);
    }

    public void calculateReorder(String productId, AgentRunContext ctx) {
        ctx.addAgent("INVENTORY_AGENT");
        toolRegistry.invoke("calculate_reorder_quantity", Map.of("productId", productId), ctx);
    }
}
