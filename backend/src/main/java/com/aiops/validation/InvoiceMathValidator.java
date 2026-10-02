package com.aiops.validation;

import com.aiops.dto.InvoiceExtractionDTO;
import com.aiops.dto.InvoiceLineItemDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validates that the sum of line-item taxable amounts + GST taxes matches
 * the total invoice amount within a tolerance of ₹0.01.
 */
public class InvoiceMathValidator implements ConstraintValidator<ValidInvoiceMath, InvoiceExtractionDTO> {

    public static final BigDecimal TOLERANCE_INR = new BigDecimal("0.01");

    @Override
    public boolean isValid(InvoiceExtractionDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // null checks handled by @NotNull
        }

        if (dto.totalAmount() == null) {
            return false;
        }

        BigDecimal calculatedTotalFromLineItems = BigDecimal.ZERO;
        if (dto.lineItems() != null && !dto.lineItems().isEmpty()) {
            for (InvoiceLineItemDTO item : dto.lineItems()) {
                BigDecimal taxable = item.taxableAmount() != null ? item.taxableAmount()
                        : (item.quantity() != null && item.unitPrice() != null
                            ? item.quantity().multiply(item.unitPrice()).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);

                BigDecimal cgst = item.cgstAmount() != null ? item.cgstAmount() : BigDecimal.ZERO;
                BigDecimal sgst = item.sgstAmount() != null ? item.sgstAmount() : BigDecimal.ZERO;
                BigDecimal igst = item.igstAmount() != null ? item.igstAmount() : BigDecimal.ZERO;

                BigDecimal itemTotal = item.totalAmount() != null ? item.totalAmount()
                        : taxable.add(cgst).add(sgst).add(igst);

                calculatedTotalFromLineItems = calculatedTotalFromLineItems.add(itemTotal);
            }

            BigDecimal lineDiff = dto.totalAmount().subtract(calculatedTotalFromLineItems).abs();
            if (lineDiff.compareTo(TOLERANCE_INR) > 0) {
                if (context != null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            String.format("Line-item math mismatch: sum of line items (₹%s) differs from invoice total (₹%s) by ₹%s, exceeding tolerance of ₹0.01",
                                    calculatedTotalFromLineItems, dto.totalAmount(), lineDiff)
                    ).addPropertyNode("totalAmount").addConstraintViolation();
                }
                return false;
            }
        }

        // Also check header breakdown if provided
        if (dto.taxableValue() != null) {
            BigDecimal headerTaxable = dto.taxableValue();
            BigDecimal headerCgst = dto.cgstTotal() != null ? dto.cgstTotal() : BigDecimal.ZERO;
            BigDecimal headerSgst = dto.sgstTotal() != null ? dto.sgstTotal() : BigDecimal.ZERO;
            BigDecimal headerIgst = dto.igstTotal() != null ? dto.igstTotal() : BigDecimal.ZERO;
            BigDecimal headerTotal = headerTaxable.add(headerCgst).add(headerSgst).add(headerIgst);

            BigDecimal headerDiff = dto.totalAmount().subtract(headerTotal).abs();
            if (headerDiff.compareTo(TOLERANCE_INR) > 0) {
                if (context != null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            String.format("Tax breakdown mismatch: taxable + taxes (₹%s) differs from invoice total (₹%s) by ₹%s, exceeding tolerance of ₹0.01",
                                    headerTotal, dto.totalAmount(), headerDiff)
                    ).addPropertyNode("totalAmount").addConstraintViolation();
                }
                return false;
            }
        }

        return true;
    }
}
