package com.aiops.agent.tools;

import com.aiops.agent.AgentActionProposal;
import com.aiops.agent.AgentRunContext;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.ApprovalRequest;
import com.aiops.domain.AuditLog;
import com.aiops.domain.PurchaseOrder;
import com.aiops.domain.PurchaseOrderItem;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.OrderStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.ApprovalRequestRepository;
import com.aiops.repository.AuditLogRepository;
import com.aiops.repository.PurchaseOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class ActionTools {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public ActionTools(ApprovalRequestRepository approvalRequestRepository,
                       PurchaseOrderRepository purchaseOrderRepository,
                       AuditLogRepository auditLogRepository,
                       ObjectMapper objectMapper) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public AgentTool createProposePurchaseRequestTool() {
        return new AgentTool() {
            @Override public String getName() { return "create_purchase_request"; }
            @Override public String getDescription() { return "Prepares a formal purchase order request with cost estimation and creates an approval request."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.MEDIUM_RISK; }
            @Override public String getRequiredPermission() { return "purchase_orders.approve"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();

                String supplierId = (String) input.getOrDefault("supplierId", "SUP-POLYCAB");
                String supplierName = (String) input.getOrDefault("supplierName", "Polycab India Ltd.");
                String productId = (String) input.getOrDefault("productId", "PROD-WIR-001");
                String productName = (String) input.getOrDefault("productName", "1.5 sq mm Copper Wire Red 90m");
                String sku = (String) input.getOrDefault("sku", "POL-CU-15-RED");
                int quantity = ((Number) input.getOrDefault("quantity", 50)).intValue();
                BigDecimal unitPrice = BigDecimal.valueOf(((Number) input.getOrDefault("unitPrice", 370.0)).doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal costInr = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, java.math.RoundingMode.HALF_UP);

                String approvalId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String title = "Purchase " + quantity + " units of " + productName + " from " + supplierName;
                String reason = (String) input.getOrDefault("reason", "Replenish safety stock and prevent customer order fulfillment delay.");
                String affected = "Order #ORD-1042, Product: " + productName + ", Supplier: " + supplierName;
                String expectedImpact = "Prevents delayed SLA delivery for customer orders and restores warehouse inventory to 14 days coverage.";

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("supplierId", supplierId);
                payload.put("supplierName", supplierName);
                payload.put("productId", productId);
                payload.put("productName", productName);
                payload.put("sku", sku);
                payload.put("quantity", quantity);
                payload.put("unitPrice", unitPrice);
                payload.put("estimatedCostInr", costInr);

                String payloadJson = "";
                try {
                    payloadJson = objectMapper.writeValueAsString(payload);
                } catch (Exception ignored) {}

                ApprovalRequest approval = new ApprovalRequest(
                        approvalId, tenantId, title, "CREATE_PURCHASE_ORDER", RiskLevel.MEDIUM_RISK,
                        "Expedited procurement of " + quantity + " units to clear order backlog.",
                        reason, costInr, expectedImpact, affected, payloadJson, "ActionAgent"
                );
                approvalRequestRepository.save(approval);

                AgentActionProposal proposal = new AgentActionProposal(
                        approvalId, "CREATE_PURCHASE_ORDER", RiskLevel.MEDIUM_RISK,
                        title, "Purchase " + quantity + " units from " + supplierName,
                        reason, costInr, expectedImpact, affected, payload, true
                );
                ctx.setProposedAction(proposal);

                // Audit log for proposal creation
                AuditLog audit = new AuditLog(
                        "AUD-" + UUID.randomUUID().toString().substring(0, 8),
                        tenantId, ctx.getUserId() != null ? ctx.getUserId() : "agent",
                        "AI ActionAgent", "PROPOSE_ACTION", "APPROVAL_REQUEST", approvalId,
                        null, payloadJson, reason, ctx.getRunId(), ctx.getRunId()
                );
                auditLogRepository.save(audit);

                return new ToolExecutionResult("create_purchase_request", true, approval,
                        "Created Approval Request #" + approvalId + " requiring human sign-off for ₹" + costInr, null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createExecuteApprovedActionTool() {
        return new AgentTool() {
            @Override public String getName() { return "execute_approved_action"; }
            @Override public String getDescription() { return "Executes an action that has been explicitly approved by an authorized human operator."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.HIGH_RISK; }
            @Override public String getRequiredPermission() { return "agent.execute"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String approvalId = (String) input.get("approvalRequestId");

                Optional<ApprovalRequest> appOpt = approvalRequestRepository.findByTenantIdAndId(tenantId, approvalId);
                if (appOpt.isEmpty()) {
                    return new ToolExecutionResult("execute_approved_action", false, null, null,
                            "Approval request " + approvalId + " not found", System.currentTimeMillis() - start);
                }

                ApprovalRequest app = appOpt.get();
                if (app.getStatus() != ApprovalStatus.APPROVED) {
                    return new ToolExecutionResult("execute_approved_action", false, null, null,
                            "Cannot execute action: current status is " + app.getStatus() + ". Human approval required first.",
                            System.currentTimeMillis() - start);
                }

                // Execute the side effect: create real Purchase Order
                String poNumber = "PO-" + (2400 + new Random().nextInt(500));
                PurchaseOrder po = new PurchaseOrder(
                        "po_" + UUID.randomUUID().toString().substring(0, 8),
                        tenantId, poNumber, "SUP-POLYCAB", "Polycab India Ltd.",
                        LocalDate.now(), LocalDate.now().plusDays(3), null,
                        OrderStatus.PROCESSING, app.getEstimatedCostInr(),
                        app.getEstimatedCostInr().multiply(BigDecimal.valueOf(0.18)),
                        app.getEstimatedCostInr().multiply(BigDecimal.valueOf(1.18)),
                        0, "Auto-created from Approved AI Action #" + app.getId()
                );

                PurchaseOrderItem item = new PurchaseOrderItem(
                        "poi_" + UUID.randomUUID().toString().substring(0, 8),
                        "PROD-WIR-001", "1.5 sq mm Copper Wire Red 90m", "POL-CU-15-RED",
                        50, BigDecimal.valueOf(370.0), 0.18, app.getEstimatedCostInr()
                );
                po.addItem(item);
                purchaseOrderRepository.save(po);

                // Update approval request status to EXECUTED
                app.setStatus(ApprovalStatus.EXECUTED);
                app.setExecutedAt(java.time.Instant.now());
                approvalRequestRepository.save(app);

                // Create immutable audit record
                AuditLog audit = new AuditLog(
                        "AUD-" + UUID.randomUUID().toString().substring(0, 8),
                        tenantId, ctx.getUserId() != null ? ctx.getUserId() : "user",
                        "Operations Manager", "EXECUTE_APPROVED_ACTION", "PURCHASE_ORDER", po.getId(),
                        "ApprovalStatus: APPROVED", "PurchaseOrder: " + poNumber + ", Status: EXECUTED",
                        "Human-approved execution of PO #" + poNumber + " for ₹" + app.getEstimatedCostInr(),
                        ctx.getRunId(), ctx.getRunId()
                );
                auditLogRepository.save(audit);

                Map<String, Object> res = new LinkedHashMap<>();
                res.put("status", "SUCCESS");
                res.put("poNumber", poNumber);
                res.put("purchaseOrder", po);
                res.put("auditLogId", audit.getId());

                return new ToolExecutionResult("execute_approved_action", true, res,
                        "Successfully executed action: Created Purchase Order #" + poNumber + " and updated audit log.", null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
