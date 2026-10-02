package com.aiops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Structured line item extracted from an Indian GST invoice.
 * Enforces validation on description, HSN code, quantity, unit price, and amounts.
 */
public record InvoiceLineItemDTO(
    @NotBlank(message = "Line item description is required")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Description of goods or services.")
    String itemDescription,

    @NotBlank(message = "HSN or SAC code is required")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Harmonized System of Nomenclature (HSN) or Service Accounting Code (SAC).")
    String hsnCode,

    @NotNull(message = "Item quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Quantity of units supplied.")
    BigDecimal quantity,

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Unit price / rate in INR.")
    BigDecimal unitPrice,

    @NotNull(message = "Taxable amount is required")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Taxable amount for this line item in INR (quantity * unitPrice).")
    BigDecimal taxableAmount,

    @NotNull(message = "GST rate percentage is required")
    @JsonProperty(required = true)
    @JsonPropertyDescription("GST rate percentage applicable, e.g. 18.00, 12.00, 5.00, 28.00.")
    BigDecimal gstRate,

    @JsonProperty(required = false)
    @JsonPropertyDescription("CGST tax amount for this item in INR.")
    BigDecimal cgstAmount,

    @JsonProperty(required = false)
    @JsonPropertyDescription("SGST tax amount for this item in INR.")
    BigDecimal sgstAmount,

    @JsonProperty(required = false)
    @JsonPropertyDescription("IGST tax amount for this item in INR.")
    BigDecimal igstAmount,

    @NotNull(message = "Line item total amount is required")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Total amount for this line item including taxes in INR.")
    BigDecimal totalAmount
) {}
