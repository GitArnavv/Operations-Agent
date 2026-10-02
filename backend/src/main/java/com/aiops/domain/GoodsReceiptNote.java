package com.aiops.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Goods Receipt Note (GRN) representing warehouse inventory intake
 * for three-way invoice matching.
 */
@Entity
@Table(name = "goods_receipt_notes", indexes = {
    @Index(name = "idx_grn_po_number", columnList = "tenant_id, po_number"),
    @Index(name = "idx_grn_number", columnList = "tenant_id, grn_number", unique = true)
})
public class GoodsReceiptNote {

    @Id
    private String id;

    @org.hibernate.annotations.TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "grn_number", nullable = false, length = 64)
    private String grnNumber; // e.g. GRN-2026-081

    @Column(name = "po_number", nullable = false, length = 64)
    private String poNumber;  // e.g. PO-2381

    @Column(name = "warehouse_id", length = 64)
    private String warehouseId;

    private String warehouseName;
    private String supplierId;
    private String supplierName;

    private LocalDate receivedDate;
    private String status; // ACCEPTED, PARTIALLY_ACCEPTED, REJECTED
    private String inspectedBy;
    private String notes;

    @OneToMany(mappedBy = "goodsReceiptNote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GoodsReceiptNoteItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public GoodsReceiptNote() {}

    public GoodsReceiptNote(String id, String tenantId, String grnNumber, String poNumber, String warehouseId, String warehouseName, String supplierId, String supplierName, LocalDate receivedDate, String status, String inspectedBy, String notes) {
        this.id = id;
        this.tenantId = tenantId;
        this.grnNumber = grnNumber;
        this.poNumber = poNumber;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.receivedDate = receivedDate;
        this.status = status;
        this.inspectedBy = inspectedBy;
        this.notes = notes;
        this.createdAt = Instant.now();
    }

    public void addItem(GoodsReceiptNoteItem item) {
        items.add(item);
        item.setGoodsReceiptNote(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getGrnNumber() { return grnNumber; }
    public void setGrnNumber(String grnNumber) { this.grnNumber = grnNumber; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<GoodsReceiptNoteItem> getItems() { return items; }
    public void setItems(List<GoodsReceiptNoteItem> items) { this.items = items; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
