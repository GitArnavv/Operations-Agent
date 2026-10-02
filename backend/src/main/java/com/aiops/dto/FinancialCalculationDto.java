package com.aiops.dto;

import com.aiops.config.BigDecimalPrecisionSerializer;
import com.aiops.config.InrCurrencySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Sample DTO demonstrating refactored financial, tax, inventory cover, and rate calculations
 * using {@link BigDecimal}, {@link RoundingMode#HALF_UP}, and Indian Rupee (INR) conventions.
 */
public class FinancialCalculationDto {

    private String invoiceNumber;
    private String customerGstin;
    private boolean intraStateTransaction; // true: CGST + SGST, false: IGST

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal subtotal;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal discountRate; // e.g. 5.00 for 5%

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal discountAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal taxableAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal gstRate; // e.g. 18.00 for 18%

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal cgstAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal sgstAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal igstAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal totalTaxAmount;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal totalPayableInr;

    @JsonSerialize(using = InrCurrencySerializer.class)
    private BigDecimal formattedPayableInr;

    // Operational/Inventory & SLA rate calculation fields
    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal daysOfStockRemaining;

    @JsonSerialize(using = BigDecimalPrecisionSerializer.class)
    private BigDecimal supplierOnTimeRate;

    public FinancialCalculationDto() {}

    /**
     * Factory method computing tax and totals using pure BigDecimal arithmetic.
     */
    public static FinancialCalculationDto calculate(String invoiceNumber,
                                                    String customerGstin,
                                                    boolean intraState,
                                                    BigDecimal subtotal,
                                                    BigDecimal discountRate,
                                                    BigDecimal gstRate,
                                                    BigDecimal daysOfStockRemaining,
                                                    BigDecimal supplierOnTimeRate) {
        FinancialCalculationDto dto = new FinancialCalculationDto();
        dto.invoiceNumber = invoiceNumber;
        dto.customerGstin = customerGstin;
        dto.intraStateTransaction = intraState;

        BigDecimal hundred = new BigDecimal("100.00");

        // Scale inputs
        dto.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        dto.discountRate = (discountRate != null ? discountRate : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        dto.gstRate = (gstRate != null ? gstRate : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // Discount calculation
        dto.discountAmount = dto.subtotal.multiply(dto.discountRate)
                .divide(hundred, 2, RoundingMode.HALF_UP);
        dto.taxableAmount = dto.subtotal.subtract(dto.discountAmount).setScale(2, RoundingMode.HALF_UP);

        // GST Tax calculation according to Indian GST law
        if (intraState) {
            // Half for CGST and half for SGST
            BigDecimal halfRate = dto.gstRate.divide(new BigDecimal("2.00"), 2, RoundingMode.HALF_UP);
            dto.cgstAmount = dto.taxableAmount.multiply(halfRate).divide(hundred, 2, RoundingMode.HALF_UP);
            dto.sgstAmount = dto.taxableAmount.multiply(halfRate).divide(hundred, 2, RoundingMode.HALF_UP);
            dto.igstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            dto.totalTaxAmount = dto.cgstAmount.add(dto.sgstAmount);
        } else {
            // Full rate for Inter-state IGST
            dto.cgstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            dto.sgstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            dto.igstAmount = dto.taxableAmount.multiply(dto.gstRate).divide(hundred, 2, RoundingMode.HALF_UP);
            dto.totalTaxAmount = dto.igstAmount;
        }

        dto.totalPayableInr = dto.taxableAmount.add(dto.totalTaxAmount).setScale(2, RoundingMode.HALF_UP);
        dto.formattedPayableInr = dto.totalPayableInr;

        dto.daysOfStockRemaining = daysOfStockRemaining != null ? daysOfStockRemaining.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        dto.supplierOnTimeRate = supplierOnTimeRate != null ? supplierOnTimeRate.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return dto;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerGstin() { return customerGstin; }
    public void setCustomerGstin(String customerGstin) { this.customerGstin = customerGstin; }

    public boolean isIntraStateTransaction() { return intraStateTransaction; }
    public void setIntraStateTransaction(boolean intraStateTransaction) { this.intraStateTransaction = intraStateTransaction; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }

    public BigDecimal getTotalPayableInr() { return totalPayableInr; }
    public void setTotalPayableInr(BigDecimal totalPayableInr) { this.totalPayableInr = totalPayableInr; }

    @JsonSerialize(using = InrCurrencySerializer.class)
    public BigDecimal getFormattedPayableInr() { return formattedPayableInr; }
    public void setFormattedPayableInr(BigDecimal formattedPayableInr) { this.formattedPayableInr = formattedPayableInr; }

    public BigDecimal getDaysOfStockRemaining() { return daysOfStockRemaining; }
    public void setDaysOfStockRemaining(BigDecimal daysOfStockRemaining) { this.daysOfStockRemaining = daysOfStockRemaining; }

    public BigDecimal getSupplierOnTimeRate() { return supplierOnTimeRate; }
    public void setSupplierOnTimeRate(BigDecimal supplierOnTimeRate) { this.supplierOnTimeRate = supplierOnTimeRate; }
}
