package com.aiops.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private String description;
    private String hsnCode;
    private int quantity;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal taxRate;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal amount;

    public InvoiceItem() {}

    public InvoiceItem(String id, String description, String hsnCode, int quantity, BigDecimal unitPrice, BigDecimal taxRate, BigDecimal amount) {
        this.id = id;
        this.description = description;
        this.hsnCode = hsnCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.amount = amount;
    }

    public InvoiceItem(String id, String description, String hsnCode, int quantity, BigDecimal unitPrice, double taxRate, BigDecimal amount) {
        this(id, description, hsnCode, quantity, unitPrice, BigDecimal.valueOf(taxRate), amount);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
