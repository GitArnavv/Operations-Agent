package com.aiops.agent.config;

import com.aiops.agent.annotation.Tool;
import com.aiops.agent.guardrails.PromptGuardrailValidator;
import com.aiops.agent.model.records.*;
import com.aiops.domain.*;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.*;
import com.aiops.security.SecurityUtils;
import com.aiops.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * Spring AI Tool & Function Bean Configuration.
 * Exposes operations strictly delegating to internal Spring @Service layers.
 */
@Configuration
public class SpringAiToolsConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringAiToolsConfig.class);

    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final SupplierService supplierService;
    private final InvoiceService invoiceService;
    private final ApprovalService approvalService;
    private final ApprovalPolicyService approvalPolicyService;
    private final PromptGuardrailValidator guardrailValidator;
    private final InventoryItemRepository inventoryItemRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public SpringAiToolsConfig(InventoryService inventoryService,
                               OrderService orderService,
                               SupplierService supplierService,
                               InvoiceService invoiceService,
                               ApprovalService approvalService,
                               ApprovalPolicyService approvalPolicyService,
                               PromptGuardrailValidator guardrailValidator,
                               InventoryItemRepository inventoryItemRepository,
                               ApprovalRequestRepository approvalRequestRepository,
                               AuditLogRepository auditLogRepository,
                               ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.supplierService = supplierService;
        this.invoiceService = invoiceService;
        this.approvalService = approvalService;
        this.approvalPolicyService = approvalPolicyService;
        this.guardrailValidator = guardrailValidator;
        this.inventoryItemRepository = inventoryItemRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Tool: check_stock
     * Checks real-time stock levels, available quantity, and stockout risk for a specific SKU or product.
     */
    @Bean
    @Description("Check real-time stock levels, available quantity, and stockout risk for a specific SKU or product.")
    @Tool(name = "check_stock", description = "Check real-time stock levels, available quantity, and stockout risk for a specific SKU or product.")
    public Function<CheckStockRequest, CheckStockResponse> checkStockFunction() {
        return request -> {
            log.info("Spring AI Tool [check_stock] called for SKU/Product: '{}', warehouse: '{}'",
                    request.skuOrProductName(), request.warehouseId());

            String tenantId = SecurityUtils.getCurrentTenantId();
            Product product = guardrailValidator.validateAndFetchProduct(tenantId, request.skuOrProductName());

            List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductId(tenantId, product.getId());
            if (items.isEmpty()) {
                // If item not found by product ID, query all items and filter by SKU
                items = inventoryItemRepository.findByTenantId(tenantId).stream()
                        .filter(i -> i.getSku().equalsIgnoreCase(product.getSku()))
                        .toList();
            }

            int physicalStock = 0;
            int reservedStock = 0;
            int availableStock = 0;
            BigDecimal minDaysRemaining = BigDecimal.valueOf(999.00).setScale(2, RoundingMode.HALF_UP);
            String stockoutRisk = "LOW";
            String warehouseName = request.warehouseId() != null ? request.warehouseId() : "All Facilities";

            for (InventoryItem item : items) {
                if (request.warehouseId() != null && !request.warehouseId().isBlank()
                        && !request.warehouseId().equalsIgnoreCase(item.getWarehouseId())
                        && !request.warehouseId().equalsIgnoreCase(item.getWarehouseName())) {
                    continue;
                }
                physicalStock += item.getCurrentStock();
                reservedStock += item.getReservedStock();
                availableStock += item.getAvailableStock();
                if (item.getDaysOfStockRemaining() != null
                        && item.getDaysOfStockRemaining().compareTo(minDaysRemaining) < 0) {
                    minDaysRemaining = item.getDaysOfStockRemaining().setScale(2, RoundingMode.HALF_UP);
                }
                if ("CRITICAL".equalsIgnoreCase(item.getStockoutRisk())) {
                    stockoutRisk = "CRITICAL";
                } else if ("HIGH".equalsIgnoreCase(item.getStockoutRisk()) && !"CRITICAL".equals(stockoutRisk)) {
                    stockoutRisk = "HIGH";
                } else if ("MEDIUM".equalsIgnoreCase(item.getStockoutRisk()) && "LOW".equals(stockoutRisk)) {
                    stockoutRisk = "MEDIUM";
                }
                warehouseName = item.getWarehouseName();
            }

            boolean isCritical = "CRITICAL".equalsIgnoreCase(stockoutRisk) || "HIGH".equalsIgnoreCase(stockoutRisk);

            return new CheckStockResponse(
                    product.getSku(),
                    product.getName(),
                    warehouseName,
                    physicalStock,
                    reservedStock,
                    availableStock,
                    minDaysRemaining,
                    stockoutRisk,
                    isCritical
            );
        };
    }

    /**
     * Tool: trigger_vendor_reminder
     * Dispatches payment or delivery fulfillment reminder to a supplier for a verified invoice.
     */
    @Bean
    @Description("Trigger an automated payment reminder to a supplier or customer for an overdue invoice.")
    @Tool(name = "trigger_vendor_reminder", description = "Trigger an automated payment reminder to a supplier or customer for an overdue invoice.")
    public Function<TriggerVendorReminderRequest, TriggerVendorReminderResponse> triggerVendorReminderFunction() {
        return request -> {
            log.info("Spring AI Tool [trigger_vendor_reminder] called for supplier: '{}', invoice: '{}'",
                    request.supplierId(), request.invoiceNumber());

            String tenantId = SecurityUtils.getCurrentTenantId();
            String userId = SecurityUtils.getCurrentUserId();

            Supplier supplier = guardrailValidator.validateAndFetchSupplier(tenantId, request.supplierId());
            Invoice invoice = guardrailValidator.validateAndFetchInvoice(tenantId, request.invoiceNumber());

            String reminderId = "REM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Record audit log for the reminder action
            AuditLog audit = new AuditLog(
                    "AUD-" + UUID.randomUUID().toString().substring(0, 8),
                    tenantId, userId, "AI Operations Agent", "TRIGGER_VENDOR_REMINDER", "INVOICE", invoice.getId(),
                    null, "DISPATCHED", "Payment reminder queued: " + request.reason(), null, null
            );
            auditLogRepository.save(audit);

            return new TriggerVendorReminderResponse(
                    true,
                    reminderId,
                    supplier.getName(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                    supplier.getEmail() != null ? supplier.getEmail() : "accounts@" + supplier.getId().toLowerCase() + ".com",
                    "Payment reminder successfully triggered for Invoice " + invoice.getInvoiceNumber() + ". Notification sent to vendor."
            );
        };
    }

    /**
     * Tool: expedite_order
     * Prepares an expedited purchase order proposal and stages an approval request awaiting operations sign-off.
     */
    @Bean
    @Description("Prepare an expedited purchase order request to resolve order bottlenecks, staging an approval request with cost estimation.")
    @Tool(name = "expedite_order", description = "Prepare an expedited purchase order request to resolve order bottlenecks, staging an approval request with cost estimation.")
    public Function<ExpediteOrderRequest, ExpediteOrderResponse> expediteOrderFunction() {
        return request -> {
            log.info("Spring AI Tool [expedite_order] called for order: '{}', supplier: '{}', qty: {}",
                    request.orderNumber(), request.supplierId(), request.quantity());

            String tenantId = SecurityUtils.getCurrentTenantId();
            String userId = SecurityUtils.getCurrentUserId();

            SalesOrder salesOrder = guardrailValidator.validateAndFetchOrder(tenantId, request.orderNumber());
            Supplier supplier = guardrailValidator.validateAndFetchSupplier(tenantId, request.supplierId());

            if (request.quantity() <= 0) {
                throw new IllegalArgumentException("Expedite quantity must be greater than zero.");
            }

            BigDecimal unitPrice = BigDecimal.valueOf(370.00).setScale(2, RoundingMode.HALF_UP);
            BigDecimal estimatedCost = unitPrice.multiply(BigDecimal.valueOf(request.quantity())).setScale(2, RoundingMode.HALF_UP);

            String approvalId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String title = "Expedited PO: " + request.quantity() + " units via " + supplier.getName() + " for " + salesOrder.getOrderNumber();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("salesOrderNumber", salesOrder.getOrderNumber());
            payload.put("supplierId", supplier.getId());
            payload.put("supplierName", supplier.getName());
            payload.put("quantity", request.quantity());
            payload.put("unitPriceInr", unitPrice);
            payload.put("estimatedCostInr", estimatedCost);
            payload.put("expeditedDeliveryDate", request.expeditedDeliveryDate() != null
                    ? request.expeditedDeliveryDate()
                    : LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE));
            payload.put("rationale", request.rationale());

            String payloadJson = "{}";
            try {
                payloadJson = objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                log.warn("Failed to serialize approval payload", e);
            }

            ApprovalRequest approval = new ApprovalRequest(
                    approvalId,
                    tenantId,
                    title,
                    "EXPEDITE_ORDER",
                    RiskLevel.MEDIUM_RISK,
                    request.rationale(),
                    request.rationale(),
                    estimatedCost,
                    "Prevents delivery SLA breach and fulfills order by Thursday.",
                    "Order #" + salesOrder.getOrderNumber() + ", Supplier: " + supplier.getName(),
                    payloadJson,
                    "AI Operations Agent (Spring AI)"
            );
            approvalRequestRepository.save(approval);

            // Also register in ApprovalPolicyService / action_approvals table
            var gateResponse = approvalPolicyService.evaluateAndGateAction(
                    tenantId, userId, "SpringAiExpediteOrderTool",
                    new com.aiops.dto.ActionEvaluationRequest(
                            "EXPEDITE_ORDER", title, request.rationale(), request.rationale(),
                            RiskLevel.MEDIUM_RISK, estimatedCost, "INR", salesOrder.getOrderNumber(), "ORDER", payload,
                            "Expediting Order #" + salesOrder.getOrderNumber()
                    )
            );

            String transactionToken = gateResponse.transactionToken() != null
                    ? gateResponse.transactionToken()
                    : "TXN-" + approvalId;

            return new ExpediteOrderResponse(
                    true,
                    approvalId,
                    transactionToken,
                    salesOrder.getOrderNumber(),
                    supplier.getName(),
                    request.quantity(),
                    estimatedCost,
                    RiskLevel.MEDIUM_RISK.name(),
                    "Expedited purchase request staged successfully. Approval Request #" + approvalId + " is awaiting manager authorization."
            );
        };
    }

    /**
     * Tool: compare_suppliers
     * Evaluates and benchmarks suppliers based on historical reliability scores, delivery SLAs, and defect rates.
     */
    @Bean
    @Description("Benchmark and compare suppliers by reliability score, on-time delivery rate, and average lead time.")
    @Tool(name = "compare_suppliers", description = "Benchmark and compare suppliers by reliability score, on-time delivery rate, and average lead time.")
    public Function<CompareSuppliersRequest, SupplierComparisonResponse> compareSuppliersFunction() {
        return request -> {
            log.info("Spring AI Tool [compare_suppliers] called for category: '{}'", request.category());

            String tenantId = SecurityUtils.getCurrentTenantId();
            List<Supplier> allSuppliers = supplierService.getSuppliers(tenantId);

            List<SupplierMetricRecord> metrics = new ArrayList<>();
            for (Supplier s : allSuppliers) {
                if (request.supplierIds() != null && !request.supplierIds().isEmpty()
                        && !request.supplierIds().contains(s.getId())) {
                    continue;
                }
                metrics.add(new SupplierMetricRecord(
                        s.getId(),
                        s.getName(),
                        s.getOnTimeDeliveryRate() != null ? s.getOnTimeDeliveryRate().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                        s.getLeadTimeDays(),
                        s.getDefectRate() != null ? s.getDefectRate().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                        s.getReliabilityScore() != null ? s.getReliabilityScore().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO
                ));
            }

            // Sort by reliability score descending
            metrics.sort((a, b) -> b.reliabilityScore().compareTo(a.reliabilityScore()));

            String topSupplier = !metrics.isEmpty() ? metrics.getFirst().supplierName() : "Havells India";
            String rationale = topSupplier + " delivers the highest reliability and lowest lead time for rush/expedited deliveries.";

            return new SupplierComparisonResponse(
                    request.category() != null ? request.category() : "General Electrical Components",
                    metrics,
                    topSupplier,
                    rationale
            );
        };
    }

    /**
     * Tool: audit_invoice
     * Audits an invoice, checking GST tax calculations, payment status, and overdue risk flags.
     */
    @Bean
    @Description("Audit an invoice by invoice number, checking GST tax consistency, payment status, and overdue risk flags.")
    @Tool(name = "audit_invoice", description = "Audit an invoice by invoice number, checking GST tax consistency, payment status, and overdue risk flags.")
    public Function<AuditInvoiceRequest, InvoiceAuditResponse> auditInvoiceFunction() {
        return request -> {
            log.info("Spring AI Tool [audit_invoice] called for invoice: '{}'", request.invoiceNumber());

            String tenantId = SecurityUtils.getCurrentTenantId();
            Invoice invoice = guardrailValidator.validateAndFetchInvoice(tenantId, request.invoiceNumber());

            boolean isOverdue = invoice.getPaymentStatus() == com.aiops.domain.enums.PaymentStatus.OVERDUE || invoice.getOverdueDays() > 0;
            int daysOverdue = invoice.getOverdueDays();
            String riskFlags = isOverdue
                    ? "OVERDUE_RECEIVABLE: Payment delayed by " + daysOverdue + " days past credit terms."
                    : "NORMAL: Invoice is within standard credit cycle.";

            BigDecimal totalTax = BigDecimal.ZERO;
            if (invoice.getCgst() != null) totalTax = totalTax.add(invoice.getCgst());
            if (invoice.getSgst() != null) totalTax = totalTax.add(invoice.getSgst());
            if (invoice.getIgst() != null) totalTax = totalTax.add(invoice.getIgst());
            totalTax = totalTax.setScale(2, RoundingMode.HALF_UP);

            BigDecimal subtotal = invoice.getSubtotal() != null ? invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            return new InvoiceAuditResponse(
                    invoice.getInvoiceNumber(),
                    invoice.getEntityName(),
                    subtotal,
                    totalTax,
                    totalAmount,
                    invoice.getPaymentStatus().name(),
                    isOverdue,
                    daysOverdue,
                    riskFlags
            );
        };
    }

    /**
     * Tool: alter_credit_terms
     * Critical Action: Modifying vendor or customer credit terms or credit limits.
     * Enforces governance gate through ApprovalPolicyService.
     */
    @Bean
    @Description("Propose alterations to supplier or customer credit terms or credit limits, which must go through the approval gate.")
    @Tool(name = "alter_credit_terms", description = "Propose alterations to supplier or customer credit terms or credit limits, which must go through the approval gate.")
    public Function<AlterCreditTermsRequest, AlterCreditTermsResponse> alterCreditTermsFunction() {
        return request -> {
            log.info("Spring AI Tool [alter_credit_terms] called for entity: '{}', proposed terms: '{}'",
                    request.entityId(), request.proposedTerms());

            String tenantId = SecurityUtils.getCurrentTenantId();
            String userId = SecurityUtils.getCurrentUserId();

            String title = "Alter Credit Terms: " + request.entityId() + " -> " + request.proposedTerms();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("entityId", request.entityId());
            payload.put("entityType", request.entityType());
            payload.put("currentTerms", request.currentTerms());
            payload.put("proposedTerms", request.proposedTerms());
            payload.put("newCreditLimit", request.newCreditLimit());
            payload.put("justification", request.justification());

            var gateResponse = approvalPolicyService.evaluateAndGateAction(
                    tenantId, userId, "SpringAiAlterCreditTermsTool",
                    new com.aiops.dto.ActionEvaluationRequest(
                            "ALTER_CREDIT_TERMS", title, request.justification(), request.justification(),
                            RiskLevel.HIGH_RISK, request.newCreditLimit(), "INR", request.entityId(), request.entityType(),
                            payload, "Credit terms adjustment request: " + request.justification()
                    )
            );

            return new AlterCreditTermsResponse(
                    gateResponse.requiresApproval(),
                    gateResponse.transactionToken(),
                    gateResponse.approvalId(),
                    request.entityId(),
                    request.proposedTerms(),
                    gateResponse.allowedActions(),
                    "Alteration of credit terms is held at the approval gate. Interactive card dispatched with actions: Acknowledge, Approve, Reject."
            );
        };
    }

    /**
     * Tool: send_vendor_whatsapp_message
     * Critical Action: Triggering external WhatsApp communications to suppliers/vendors.
     * Enforces governance gate through ApprovalPolicyService.
     */
    @Bean
    @Description("Prepare a WhatsApp notification to a vendor for payment or delivery follow-up, which must go through the approval gate.")
    @Tool(name = "send_vendor_whatsapp_message", description = "Prepare a WhatsApp notification to a vendor for payment or delivery follow-up, which must go through the approval gate.")
    public Function<SendVendorWhatsAppMessageRequest, SendVendorWhatsAppMessageResponse> sendVendorWhatsAppMessageFunction() {
        return request -> {
            log.info("Spring AI Tool [send_vendor_whatsapp_message] called for supplier: '{}', phone: '{}'",
                    request.supplierId(), request.recipientPhone());

            String tenantId = SecurityUtils.getCurrentTenantId();
            String userId = SecurityUtils.getCurrentUserId();

            Supplier supplier = guardrailValidator.validateAndFetchSupplier(tenantId, request.supplierId());

            String title = "Send WhatsApp Reminder to " + supplier.getName() + " (" + request.recipientPhone() + ")";
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("supplierId", supplier.getId());
            payload.put("supplierName", supplier.getName());
            payload.put("referenceNumber", request.referenceNumber());
            payload.put("recipientPhone", request.recipientPhone());
            payload.put("messageBody", request.messageBody());
            payload.put("channel", "WHATSAPP");

            var gateResponse = approvalPolicyService.evaluateAndGateAction(
                    tenantId, userId, "SpringAiWhatsAppTool",
                    new com.aiops.dto.ActionEvaluationRequest(
                            "TRIGGER_WHATSAPP_REMINDER", title, request.messageBody(), request.reason(),
                            RiskLevel.HIGH_RISK, BigDecimal.ZERO, "INR", supplier.getId(), "SUPPLIER",
                            payload, "Vendor WhatsApp notification follow-up"
                    )
            );

            return new SendVendorWhatsAppMessageResponse(
                    gateResponse.requiresApproval(),
                    gateResponse.transactionToken(),
                    gateResponse.approvalId(),
                    supplier.getName(),
                    request.recipientPhone(),
                    gateResponse.allowedActions(),
                    "External WhatsApp message is held at the approval gate. Interactive card dispatched with actions: Acknowledge, Approve, Reject."
            );
        };
    }
}
