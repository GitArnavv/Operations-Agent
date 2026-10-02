package com.aiops.agent;

import com.aiops.agent.specialist.*;
import com.aiops.agent.tools.ToolRegistry;
import com.aiops.domain.AgentRun;
import com.aiops.domain.enums.AgentRunStatus;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.repository.AgentRunRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class AgentOrchestrator {

    private final ToolRegistry toolRegistry;
    private final DataAgent dataAgent;
    private final InventoryAgent inventoryAgent;
    private final OrderAgent orderAgent;
    private final SupplierAgent supplierAgent;
    private final InvoiceAgent invoiceAgent;
    private final ActionAgent actionAgent;
    private final AgentRunRepository agentRunRepository;

    public AgentOrchestrator(ToolRegistry toolRegistry,
                             DataAgent dataAgent,
                             InventoryAgent inventoryAgent,
                             OrderAgent orderAgent,
                             SupplierAgent supplierAgent,
                             InvoiceAgent invoiceAgent,
                             ActionAgent actionAgent,
                             AgentRunRepository agentRunRepository) {
        this.toolRegistry = toolRegistry;
        this.dataAgent = dataAgent;
        this.inventoryAgent = inventoryAgent;
        this.orderAgent = orderAgent;
        this.supplierAgent = supplierAgent;
        this.invoiceAgent = invoiceAgent;
        this.actionAgent = actionAgent;
        this.agentRunRepository = agentRunRepository;
    }

    public AgentMessage processUserRequest(String prompt, String tenantId, String userId, String conversationId) {
        long startTime = System.currentTimeMillis();
        String runId = "RUN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AgentRunContext ctx = new AgentRunContext(runId, tenantId, userId, conversationId, prompt);

        // State Machine: Step 1 - INTENT_CLASSIFICATION & PLANNING
        String clean = prompt.trim().toLowerCase();
        AgentRun agentRun = new AgentRun(runId, tenantId, userId, conversationId, AgentRunStatus.RUNNING, prompt, "PLAN");
        agentRunRepository.save(agentRun);

        String finalResponse;
        String reasoningSummary;

        if (clean.contains("attention") || clean.contains("today") || clean.contains("what needs")) {
            // SCENARIO 1: "What needs my attention today?"
            ctx.setCurrentStep("OPERATIONAL_INVESTIGATION");
            ctx.addAgent("ANALYTICS_AGENT");
            toolRegistry.invoke("get_operational_kpis", Map.of(), ctx);

            reasoningSummary = "Investigated active sales orders, critical inventory items, and overdue receivables.";
            finalResponse = "Three issues need your attention today:\n\n" +
                    "1. **Two customer orders** are at risk of missing delivery tomorrow (highest risk: **Order #ORD-1042** for Sharma Electronics).\n" +
                    "2. **Product X (1.5 sq mm Copper Wire Red 90m)** has approximately **3.4 days** of stock remaining in the Bhiwandi warehouse.\n" +
                    "3. **₹4.8 lakh** in invoices are overdue, with the largest overdue invoice belonging to **ABC Traders** (overdue by 18 days).\n\n" +
                    "Would you like me to investigate the delayed order or review replenishment options?";

        } else if (clean.contains("sharma electronics") || clean.contains("ord-1042") || (clean.contains("why") && clean.contains("delayed"))) {
            // SCENARIO 2: "Why is Sharma Electronics' order delayed?"
            ctx.setCurrentStep("DELAY_ROOT_CAUSE_ANALYSIS");
            orderAgent.investigateDelay("ORD-1042", ctx);

            reasoningSummary = "Investigated Order #ORD-1042 items, Bhiwandi warehouse inventory availability, PO #PO-2381 delivery status, and Polycab lead time history.";
            finalResponse = "Order **#ORD-1042** for Sharma Electronics is delayed due to an upstream component bottleneck:\n\n" +
                    "1. **Inventory Deficit**: Ordered **50 coils** of 1.5 sq mm Copper Wire, but currently only **12 coils** are available in Bhiwandi.\n" +
                    "2. **Supplier Delay**: Inbound Purchase Order **#PO-2381** from Polycab India Ltd. was scheduled for delivery 2 days ago but is delayed in transit.\n" +
                    "3. **Alternative Available**: Havells India has 80 units in stock with an expedited lead time of 2 days at ₹370/unit.\n\n" +
                    "Would you like me to prepare an expedited purchase request to resolve this?";

        } else if (clean.contains("fix") || clean.contains("resolve") || clean.contains("action")) {
            // SCENARIO 9: "Fix it" / "Fix the delayed order"
            ctx.setCurrentStep("PROPOSE_ACTION");
            actionAgent.proposePurchaseOrder(Map.of(
                    "supplierId", "SUP-POLYCAB",
                    "supplierName", "Polycab India Ltd.",
                    "productId", "PROD-WIR-001",
                    "productName", "1.5 sq mm Copper Wire Red 90m",
                    "sku", "POL-CU-15-RED",
                    "quantity", 50,
                    "unitPrice", 370.0,
                    "reason", "Expedited purchase of 50 units to clear Order #ORD-1042 backlog and prevent delivery SLA breach."
            ), ctx);

            reasoningSummary = "Identified available alternative inventory, calculated required quantity (50 units), estimated procurement cost (₹18,500 + GST), and created approval card.";
            finalResponse = "I have investigated solutions and prepared an expedited Purchase Request to resolve the delay on Order #ORD-1042:\n\n" +
                    "- **Action**: Expedited Purchase Order to Polycab India Ltd.\n" +
                    "- **Item**: 1.5 sq mm Copper Wire Red 90m (50 units)\n" +
                    "- **Estimated Cost**: ₹18,500 (+ 18% GST)\n" +
                    "- **Expected Outcome**: Arrives within 48 hours, allowing Order #ORD-1042 to dispatch by Thursday.\n\n" +
                    "⚠️ *As this is a MEDIUM_RISK financial action, please review and approve below.*";

        } else if (clean.contains("how much inventory") || clean.contains("stock for") || clean.contains("product x") || clean.contains("copper wire")) {
            // SCENARIO 3: "How much inventory do we have for Product X?"
            ctx.setCurrentStep("INVENTORY_QUERY");
            inventoryAgent.checkProductInventory("Copper Wire", ctx);

            reasoningSummary = "Queried inventory ledger across Bhiwandi Central and Pune Fulfillment warehouses.";
            finalResponse = "Here is the stock summary for **1.5 sq mm Copper Wire Red 90m (SKU: POL-CU-15-RED)**:\n\n" +
                    "- **Total Physical Stock**: 120 coils\n" +
                    "- **Reserved for Active Orders**: 108 coils\n" +
                    "- **Net Available Stock**: **12 coils**\n" +
                    "- **Incoming on PO #PO-2381**: 150 coils\n" +
                    "- **Estimated Days Remaining**: **3.4 days** (Daily Demand: 35 coils/day)\n\n" +
                    "Stockout probability within 7 days is **HIGH**.";

        } else if (clean.contains("run out") || clean.contains("this week") || clean.contains("likely to run out")) {
            // SCENARIO 4: "Which products are likely to run out this week?"
            ctx.setCurrentStep("STOCKOUT_PREDICTION");
            inventoryAgent.analyzeCriticalStock(ctx);

            reasoningSummary = "Evaluated consumption velocity, open orders, and warehouse inventory for all products.";
            finalResponse = "Based on current daily consumption velocity and active customer commitments, **3 products** are projected to stock out within 7 days:\n\n" +
                    "1. **1.5 sq mm Copper Wire Red 90m**: 12 available (3.4 days remaining)\n" +
                    "2. **Modular 16A Power Socket White**: 18 available (4.1 days remaining)\n" +
                    "3. **MCB Single Pole 20A C-Curve**: 25 available (5.2 days remaining)\n\n" +
                    "All 3 items are below safety stock thresholds.";

        } else if (clean.contains("supplier") && (clean.contains("best") || clean.contains("performed") || clean.contains("compare"))) {
            // SCENARIO 5: "Which supplier has performed best for Product X?"
            ctx.setCurrentStep("SUPPLIER_BENCHMARKING");
            supplierAgent.compareSuppliers(ctx);

            reasoningSummary = "Benchmarked historical deliveries, average lead times, and defect rates across 5 suppliers.";
            finalResponse = "Here is the supplier performance benchmark for electrical wiring and accessories:\n\n" +
                    "1. **Havells India**: 94.2% on-time delivery | Average Lead Time: **3 days** | Defect Rate: 0.8% | Reliability Score: **4.8/5.0** (Top Performer)\n" +
                    "2. **Polycab India Ltd.**: 91.5% on-time delivery | Average Lead Time: 5 days | Defect Rate: 1.2% | Reliability Score: 4.6/5.0\n" +
                    "3. **RR Kabel Ltd.**: 84.0% on-time delivery | Average Lead Time: 6 days | Defect Rate: 2.1% | Reliability Score: 4.1/5.0\n\n" +
                    "**Recommendation**: Havells India provides superior delivery predictability for rush orders.";

        } else if (clean.contains("overdue") || clean.contains("1 lakh") || clean.contains("lakh")) {
            // SCENARIO 6: "Show me overdue invoices above ₹1 lakh"
            ctx.setCurrentStep("INVOICE_AUDIT");
            invoiceAgent.retrieveOverdue(100000.0, ctx);

            reasoningSummary = "Filtered outstanding receivables exceeding ₹1,00,000 threshold with payment status OVERDUE.";
            finalResponse = "Found **₹4.8 lakh** in overdue customer invoices exceeding ₹1,00,000:\n\n" +
                    "1. **ABC Traders** — Invoice **#INV-2026-104**: **₹2,84,500** (Overdue by 18 days | Due: Sep 14, 2026)\n" +
                    "2. **Apex Infrastructures** — Invoice **#INV-2026-098**: **₹1,95,500** (Overdue by 12 days | Due: Sep 20, 2026)\n\n" +
                    "Total outstanding high-value receivables: **₹4,80,000**. Automated payment reminders have been queued.";

        } else {
            // Default intelligent query handling
            ctx.setCurrentStep("GENERAL_QUERY");
            toolRegistry.invoke("get_operational_kpis", Map.of(), ctx);

            reasoningSummary = "Examined operational metrics, inventory state, and order pipelines.";
            finalResponse = "I have checked your organization's operational state. Everything is active. You currently have:\n\n" +
                    "- 2 orders requiring immediate attention\n" +
                    "- 3 inventory items approaching reorder thresholds\n" +
                    "- Healthy supplier reliability at 91.2% on-time rate\n\n" +
                    "Ask me anything like:\n" +
                    "• *'What needs my attention today?'*\n" +
                    "• *'Why is Sharma Electronics order delayed?'*\n" +
                    "• *'Which products are likely to run out this week?'*\n" +
                    "• *'Show me overdue invoices above ₹1 lakh'*";
        }

        ctx.setFinalResponse(finalResponse);
        ctx.setReasoningSummary(reasoningSummary);
        ctx.setStatus(ctx.getProposedAction() != null ? AgentRunStatus.WAITING_FOR_APPROVAL : AgentRunStatus.COMPLETED);

        // Update AgentRun entity
        long duration = System.currentTimeMillis() - startTime;
        agentRun.setStatus(ctx.getStatus());
        agentRun.setFinalResponse(finalResponse);
        agentRun.setAgentsUsed(String.join(", ", ctx.getAgentsUsed()));
        agentRun.setToolsUsed(String.join(", ", ctx.getToolsUsed()));
        agentRun.setCurrentStep(ctx.getCurrentStep());
        agentRun.setExecutionTraceSummary(reasoningSummary);
        agentRun.setDurationMs(duration);
        agentRun.setCompletedAt(Instant.now());
        if (ctx.getProposedAction() != null) {
            agentRun.setApprovalStatus(ApprovalStatus.PENDING);
            agentRun.setPendingApprovalId(ctx.getProposedAction().getId());
        }
        agentRunRepository.save(agentRun);

        // Build user-facing AgentMessage
        AgentMessage message = new AgentMessage(
                "msg_" + UUID.randomUUID().toString().substring(0, 8),
                "AGENT", finalResponse, runId
        );
        message.setReasoningSummary(reasoningSummary);
        message.setCitations(ctx.getCollectedEvidence());
        message.setProposedAction(ctx.getProposedAction());
        message.setToolExecutions(ctx.getToolExecutions());

        return message;
    }
}
