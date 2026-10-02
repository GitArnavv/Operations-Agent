package com.aiops.agent.specialist;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.tools.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InvoiceAgent {

    private final ToolRegistry toolRegistry;

    public InvoiceAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public void retrieveOverdue(double minAmountInr, AgentRunContext ctx) {
        ctx.addAgent("INVOICE_AGENT");
        toolRegistry.invoke("get_invoices", Map.of("status", "OVERDUE", "minAmountInr", minAmountInr), ctx);
    }

    public void validateGst(String invoiceNumber, AgentRunContext ctx) {
        ctx.addAgent("INVOICE_AGENT");
        toolRegistry.invoke("validate_invoice_gst", Map.of("invoiceNumber", invoiceNumber), ctx);
    }
}
