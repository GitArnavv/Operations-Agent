package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record InvoiceAuditResponse(
    @JsonPropertyDescription("Verified invoice number.")
    String invoiceNumber,

    @JsonPropertyDescription("Customer or vendor party legal name.")
    String partyName,

    @JsonPropertyDescription("Pre-tax invoice subtotal in INR.")
    BigDecimal subtotalAmount,

    @JsonPropertyDescription("Applicable GST tax amount in INR.")
    BigDecimal taxAmount,

    @JsonPropertyDescription("Total invoice payable amount in INR.")
    BigDecimal totalAmount,

    @JsonPropertyDescription("Payment status: PAID, PENDING, or OVERDUE.")
    String paymentStatus,

    @JsonPropertyDescription("Whether invoice has passed its due date without full settlement.")
    boolean isOverdue,

    @JsonPropertyDescription("Number of elapsed days overdue past payment term SLA.")
    int daysOverdue,

    @JsonPropertyDescription("Audit risk remarks and compliance flags.")
    String riskFlags
) {}
