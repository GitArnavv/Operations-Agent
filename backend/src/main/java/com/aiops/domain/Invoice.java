package com.aiops.domain;

import com.aiops.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    private String id;

    @org.hibernate.annotations.TenantId
    @Column(nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String invoiceNumber; // e.g. INV-923, INV-2026-104

    private String poNumber;

    @Column(nullable = false)
    private String entityType; // "CUSTOMER" or "SUPPLIER"

    private String entityId;
    private String entityName;
    private String gstin;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal subtotal;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal cgst;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal sgst;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal igst;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private int overdueDays;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal extractionConfidence; // e.g. 0.96

    private String validationStatus;     // EXTRACTED, VALIDATED, USER_CONFIRMED
    private String discrepancyNotes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> lineItems = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Invoice() {}

    public Invoice(String id, String tenantId, String invoiceNumber, String poNumber, String entityType, String entityId, String entityName, String gstin, LocalDate invoiceDate, LocalDate dueDate, BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal totalAmount, PaymentStatus paymentStatus, int overdueDays, BigDecimal extractionConfidence, String validationStatus, String discrepancyNotes) {
        this.id = id;
        this.tenantId = tenantId;
        this.invoiceNumber = invoiceNumber;
        this.poNumber = poNumber;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.gstin = gstin;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.cgst = cgst;
        this.sgst = sgst;
        this.igst = igst;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.overdueDays = overdueDays;
        this.extractionConfidence = extractionConfidence;
        this.validationStatus = validationStatus;
        this.discrepancyNotes = discrepancyNotes;
        this.createdAt = Instant.now();
    }

    public Invoice(String id, String tenantId, String invoiceNumber, String poNumber, String entityType, String entityId, String entityName, String gstin, LocalDate invoiceDate, LocalDate dueDate, BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal totalAmount, PaymentStatus paymentStatus, int overdueDays, double extractionConfidence, String validationStatus, String discrepancyNotes) {
        this(id, tenantId, invoiceNumber, poNumber, entityType, entityId, entityName, gstin, invoiceDate, dueDate, subtotal, cgst, sgst, igst, totalAmount, paymentStatus, overdueDays, BigDecimal.valueOf(extractionConfidence), validationStatus, discrepancyNotes);
    }

    public void addLineItem(InvoiceItem item) {
        lineItems.add(item);
        item.setInvoice(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getCgst() { return cgst; }
    public void setCgst(BigDecimal cgst) { this.cgst = cgst; }

    public BigDecimal getSgst() { return sgst; }
    public void setSgst(BigDecimal sgst) { this.sgst = sgst; }

    public BigDecimal getIgst() { return igst; }
    public void setIgst(BigDecimal igst) { this.igst = igst; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public int getOverdueDays() { return overdueDays; }
    public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }

    public BigDecimal getExtractionConfidence() { return extractionConfidence; }
    public void setExtractionConfidence(BigDecimal extractionConfidence) { this.extractionConfidence = extractionConfidence; }

    public String getValidationStatus() { return validationStatus; }
    public void setValidationStatus(String validationStatus) { this.validationStatus = validationStatus; }

    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }

    public List<InvoiceItem> getLineItems() { return lineItems; }
    public void setLineItems(List<InvoiceItem> lineItems) { this.lineItems = lineItems; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
