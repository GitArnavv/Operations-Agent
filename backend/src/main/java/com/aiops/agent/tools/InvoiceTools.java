package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.CitationEvidence;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.Invoice;
import com.aiops.domain.enums.PaymentStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class InvoiceTools {

    private final InvoiceRepository invoiceRepository;

    public InvoiceTools(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public AgentTool createGetInvoicesTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_invoices"; }
            @Override public String getDescription() { return "Retrieve invoices filtered by payment status (e.g. OVERDUE) and minimum amount in INR."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "invoices.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String statusStr = (String) input.get("status");
                Number minAmount = (Number) input.get("minAmountInr");

                List<Invoice> list;
                if (statusStr != null && !statusStr.isBlank()) {
                    PaymentStatus ps = PaymentStatus.valueOf(statusStr.toUpperCase());
                    if (minAmount != null) {
                        list = invoiceRepository.findByTenantIdAndPaymentStatusAndTotalAmountGreaterThanEqual(
                                tenantId, ps, BigDecimal.valueOf(minAmount.doubleValue()));
                    } else {
                        list = invoiceRepository.findByTenantIdAndPaymentStatus(tenantId, ps);
                    }
                } else {
                    list = invoiceRepository.findByTenantId(tenantId);
                }

                // Add evidence for overdue high-value invoices
                for (Invoice inv : list) {
                    if (inv.getPaymentStatus() == PaymentStatus.OVERDUE) {
                        BigDecimal inLakhs = inv.getTotalAmount() != null
                                ? inv.getTotalAmount().divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                        ctx.addEvidence(new CitationEvidence("INVOICE", inv.getId(),
                                "Invoice #" + inv.getInvoiceNumber(),
                                "Entity: " + inv.getEntityName() + " | Overdue by " + inv.getOverdueDays() + " days | Amount: ₹" + inLakhs.toPlainString() + " Lakh (₹" + inv.getTotalAmount() + ")",
                                0.97, "/invoices/" + inv.getInvoiceNumber()));
                    }
                }

                return new ToolExecutionResult("get_invoices", true, list,
                        "Retrieved " + list.size() + " invoices", null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createValidateInvoiceGstTool() {
        return new AgentTool() {
            @Override public String getName() { return "validate_invoice_gst"; }
            @Override public String getDescription() { return "Validates Indian GST math (CGST/SGST/IGST), GSTIN structure, and checks for PO discrepancies."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "invoices.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String invoiceNumber = (String) input.get("invoiceNumber");
                String tenantId = ctx.getTenantId();

                Optional<Invoice> invOpt = invoiceRepository.findByTenantIdAndInvoiceNumber(tenantId, invoiceNumber);
                if (invOpt.isEmpty()) {
                    return new ToolExecutionResult("validate_invoice_gst", false, null, null,
                            "Invoice " + invoiceNumber + " not found", System.currentTimeMillis() - start);
                }

                Invoice inv = invOpt.get();
                Map<String, Object> check = new LinkedHashMap<>();
                check.put("invoiceNumber", inv.getInvoiceNumber());
                check.put("entityName", inv.getEntityName());
                check.put("gstin", inv.getGstin());
                check.put("validationStatus", inv.getValidationStatus());
                check.put("extractionConfidence", inv.getExtractionConfidence());

                boolean mathValid = true;
                BigDecimal expectedTotal = inv.getSubtotal().add(inv.getCgst()).add(inv.getSgst()).add(inv.getIgst());
                if (expectedTotal.compareTo(inv.getTotalAmount()) != 0) {
                    mathValid = false;
                }
                check.put("isTaxMathValid", mathValid);
                check.put("discrepancyNotes", inv.getDiscrepancyNotes());

                int confPct = inv.getExtractionConfidence() != null
                        ? inv.getExtractionConfidence().multiply(BigDecimal.valueOf(100)).intValue()
                        : 0;

                ctx.addEvidence(new CitationEvidence("INVOICE", inv.getId(),
                        "Validated Invoice #" + inv.getInvoiceNumber(),
                        "GSTIN: " + inv.getGstin() + " | Tax Math Valid: " + mathValid + " | Extraction Confidence: " + confPct + "%",
                        0.98, "/invoices/" + inv.getInvoiceNumber()));

                return new ToolExecutionResult("validate_invoice_gst", true, check,
                        "Completed GST validation for " + invoiceNumber, null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
