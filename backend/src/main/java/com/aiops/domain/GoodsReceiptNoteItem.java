package com.aiops.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Line item within a Goods Receipt Note recording accepted and rejected quantities.
 */
@Entity
@Table(name = "goods_receipt_note_items")
public class GoodsReceiptNoteItem {

    @Id
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GoodsReceiptNote goodsReceiptNote;

    private String productId;
    private String productName;
    private String sku;
    private String hsnCode;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal quantityReceived;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal quantityAccepted;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal quantityRejected;

    private String unitOfMeasure;
    private String conditionNotes;

    public GoodsReceiptNoteItem() {}

    public GoodsReceiptNoteItem(String id, String productId, String productName, String sku, String hsnCode, BigDecimal quantityReceived, BigDecimal quantityAccepted, BigDecimal quantityRejected, String unitOfMeasure, String conditionNotes) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.hsnCode = hsnCode;
        this.quantityReceived = quantityReceived;
        this.quantityAccepted = quantityAccepted;
        this.quantityRejected = quantityRejected;
        this.unitOfMeasure = unitOfMeasure;
        this.conditionNotes = conditionNotes;
    }

    public GoodsReceiptNoteItem(String id, String productId, String productName, String sku, String hsnCode, int quantityReceived, int quantityAccepted, int quantityRejected) {
        this(id, productId, productName, sku, hsnCode, BigDecimal.valueOf(quantityReceived), BigDecimal.valueOf(quantityAccepted), BigDecimal.valueOf(quantityRejected), "Units", "Good Condition");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public GoodsReceiptNote getGoodsReceiptNote() { return goodsReceiptNote; }
    public void setGoodsReceiptNote(GoodsReceiptNote goodsReceiptNote) { this.goodsReceiptNote = goodsReceiptNote; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getQuantityAccepted() { return quantityAccepted; }
    public void setQuantityAccepted(BigDecimal quantityAccepted) { this.quantityAccepted = quantityAccepted; }

    public BigDecimal getQuantityRejected() { return quantityRejected; }
    public void setQuantityRejected(BigDecimal quantityRejected) { this.quantityRejected = quantityRejected; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getConditionNotes() { return conditionNotes; }
    public void setConditionNotes(String conditionNotes) { this.conditionNotes = conditionNotes; }
}
