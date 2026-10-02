package com.aiops.dto;

import com.aiops.validation.ValidInvoiceMath;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Enforced target schema for Gemini Flash Multimodal OCR extraction.
 * Enforces:
 * 1. Indian GSTIN regex validation: '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$'
 * 2. Line-item math validation: sum of taxable amounts + GST matches total amount within ₹0.01 tolerance
 */
@ValidInvoiceMath
public record InvoiceExtractionDTO(
    @NotBlank(message = "Vendor GSTIN is mandatory")
    @Pattern(
        regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
        message = "Invalid GSTIN format. Expected format: 2 state digits + 10 PAN chars + 1 entity char + 'Z' + 1 checksum char (e.g. 27AAACP8213J1Z8)"
    )
    @JsonProperty(required = true)
    @JsonPropertyDescription("15-digit Goods and Services Tax Identification Number (GSTIN) of the vendor/supplier.")
    String vendorGstin,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Legal trade or registered name of the vendor/supplier.")
    String vendorName,

    @NotBlank(message = "Invoice reference number is mandatory")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Unique invoice reference number identified on the tax document.")
    String invoiceNumber,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Invoice date in standard ISO format YYYY-MM-DD.")
    String invoiceDate,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Purchase Order (PO) number referenced on the invoice.")
    String poReference,

    @NotNull(message = "Line items list cannot be null")
    @NotEmpty(message = "Invoice must contain at least one line item")
    @Valid
    @JsonProperty(required = true)
    @JsonPropertyDescription("Itemized line items from the invoice including description, HSN codes, quantities, rates, and tax.")
    List<InvoiceLineItemDTO> lineItems,

    @NotNull(message = "Total taxable value is required")
    @DecimalMin(value = "0.00", message = "Taxable value cannot be negative")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Total taxable value before GST (INR).")
    BigDecimal taxableValue,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Total Central GST (CGST) amount (INR).")
    BigDecimal cgstTotal,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Total State GST (SGST) amount (INR).")
    BigDecimal sgstTotal,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Total Integrated GST (IGST) amount for interstate supplies (INR).")
    BigDecimal igstTotal,

    @NotNull(message = "Grand total invoice amount is required")
    @DecimalMin(value = "0.00", message = "Total invoice amount cannot be negative")
    @JsonProperty(required = true)
    @JsonPropertyDescription("Grand total invoice amount including all taxes and charges (INR).")
    BigDecimal totalAmount,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Extraction confidence score between 0.00 and 1.00.")
    BigDecimal confidenceScore,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Audit and discrepancy notes, e.g. tax calculations check or PO matching.")
    String discrepancyNotes
) {

    /**
     * Programmatic check ensuring the sum of line items + tax matches total invoice amount
     * within a tolerance of ₹0.01.
     */
    @AssertTrue(message = "Line-item math mismatch: sum of line-item taxable amounts and GST taxes must match total invoice amount within ₹0.01 tolerance")
    public boolean isLineItemMathValid() {
        if (totalAmount == null) return false;

        BigDecimal sumOfLineItems = BigDecimal.ZERO;
        if (lineItems != null && !lineItems.isEmpty()) {
            for (InvoiceLineItemDTO item : lineItems) {
                BigDecimal taxable = item.taxableAmount() != null ? item.taxableAmount()
                        : (item.quantity() != null && item.unitPrice() != null
                            ? item.quantity().multiply(item.unitPrice()).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                BigDecimal cgst = item.cgstAmount() != null ? item.cgstAmount() : BigDecimal.ZERO;
                BigDecimal sgst = item.sgstAmount() != null ? item.sgstAmount() : BigDecimal.ZERO;
                BigDecimal igst = item.igstAmount() != null ? item.igstAmount() : BigDecimal.ZERO;

                BigDecimal itemTotal = item.totalAmount() != null ? item.totalAmount()
                        : taxable.add(cgst).add(sgst).add(igst);

                sumOfLineItems = sumOfLineItems.add(itemTotal);
            }
            BigDecimal diff = totalAmount.subtract(sumOfLineItems).abs();
            if (diff.compareTo(new BigDecimal("0.01")) > 0) {
                return false;
            }
        }

        if (taxableValue != null) {
            BigDecimal cgst = cgstTotal != null ? cgstTotal : BigDecimal.ZERO;
            BigDecimal sgst = sgstTotal != null ? sgstTotal : BigDecimal.ZERO;
            BigDecimal igst = igstTotal != null ? igstTotal : BigDecimal.ZERO;
            BigDecimal headerTotal = taxableValue.add(cgst).add(sgst).add(igst);
            BigDecimal diff = totalAmount.subtract(headerTotal).abs();
            return diff.compareTo(new BigDecimal("0.01")) <= 0;
        }

        return true;
    }
}
