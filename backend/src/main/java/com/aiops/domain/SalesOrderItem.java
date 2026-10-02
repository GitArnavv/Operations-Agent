package com.aiops.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {

    @Id
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(nullable = false)
    private String productId;

    private String productName;
    private String sku;
    private int quantity;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal gstRate;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal lineTotal;

    public SalesOrderItem() {}

    public SalesOrderItem(String id, String productId, String productName, String sku, int quantity, BigDecimal unitPrice, BigDecimal gstRate, BigDecimal lineTotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.gstRate = gstRate;
        this.lineTotal = lineTotal;
    }

    public SalesOrderItem(String id, String productId, String productName, String sku, int quantity, BigDecimal unitPrice, double gstRate, BigDecimal lineTotal) {
        this(id, productId, productName, sku, quantity, unitPrice, BigDecimal.valueOf(gstRate), lineTotal);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
