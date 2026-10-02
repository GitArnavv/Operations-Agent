package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.ToolExecutionResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(OrderTools orderTools,
                        InventoryTools inventoryTools,
                        SupplierTools supplierTools,
                        InvoiceTools invoiceTools,
                        AnalyticsTools analyticsTools,
                        ActionTools actionTools) {
        // Register Order tools
        register(orderTools.createGetOrdersTool());
        register(orderTools.createInvestigateOrderDelayTool());

        // Register Inventory tools
        register(inventoryTools.createGetInventoryTool());
        register(inventoryTools.createGetProductInventoryTool());
        register(inventoryTools.createCalculateReorderQuantityTool());

        // Register Supplier tools
        register(supplierTools.createGetSuppliersTool());
        register(supplierTools.createCompareSuppliersTool());

        // Register Invoice tools
        register(invoiceTools.createGetInvoicesTool());
        register(invoiceTools.createValidateInvoiceGstTool());

        // Register Analytics tools
        register(analyticsTools.createGetOperationalKpisTool());

        // Register Action tools
        register(actionTools.createProposePurchaseRequestTool());
        register(actionTools.createExecuteApprovedActionTool());
    }

    public void register(AgentTool tool) {
        tools.put(tool.getName(), tool);
    }

    public Optional<AgentTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<AgentTool> getAllTools() {
        return tools.values();
    }

    public ToolExecutionResult invoke(String toolName, Map<String, Object> input, AgentRunContext ctx) {
        AgentTool tool = tools.get(toolName);
        if (tool == null) {
            return new ToolExecutionResult(toolName, false, null, null,
                    "Tool '" + toolName + "' is not registered in the system", 0);
        }

        ctx.addTool(toolName);
        ToolExecutionResult result = tool.execute(input, ctx);
        ctx.addToolExecution(result);
        return result;
    }
}
