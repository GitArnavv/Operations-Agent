package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record TriggerVendorReminderResponse(
    @JsonPropertyDescription("Whether the reminder dispatch was logged and triggered successfully.")
    boolean success,

    @JsonPropertyDescription("System generated audit reminder ID.")
    String reminderId,

    @JsonPropertyDescription("Verified supplier legal name.")
    String supplierName,

    @JsonPropertyDescription("Target invoice number.")
    String invoiceNumber,

    @JsonPropertyDescription("Outstanding balance in INR formatted to exact 2 decimal places.")
    BigDecimal overdueAmountInr,

    @JsonPropertyDescription("Supplier contact email address.")
    String contactEmail,

    @JsonPropertyDescription("Outcome summary for audit history.")
    String statusMessage
) {}
