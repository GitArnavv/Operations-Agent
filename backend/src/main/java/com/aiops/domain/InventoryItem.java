package com.aiops.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "productId", "warehouseId"})
})
public class InventoryItem {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String productId;

    private String productName;
    private String sku;

    @Column(nullable = false)
    private String warehouseId;

    private String warehouseName;

    private int currentStock;
    private int reservedStock;
    private int incomingStock;
    private int availableStock; // current - reserved

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal daysOfStockRemaining;
    private String stockoutRisk; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false)
    private Instant lastUpdated = Instant.now();

    public InventoryItem() {}

    public InventoryItem(String id, String tenantId, String productId, String productName, String sku, String warehouseId, String warehouseName, int currentStock, int reservedStock, int incomingStock, BigDecimal daysOfStockRemaining, String stockoutRisk) {
        this.id = id;
        this.tenantId = tenantId;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.currentStock = currentStock;
        this.reservedStock = reservedStock;
        this.incomingStock = incomingStock;
        this.availableStock = Math.max(0, currentStock - reservedStock);
        this.daysOfStockRemaining = daysOfStockRemaining;
        this.stockoutRisk = stockoutRisk;
        this.lastUpdated = Instant.now();
    }

    public InventoryItem(String id, String tenantId, String productId, String productName, String sku, String warehouseId, String warehouseName, int currentStock, int reservedStock, int incomingStock, double daysOfStockRemaining, String stockoutRisk) {
        this(id, tenantId, productId, productName, sku, warehouseId, warehouseName, currentStock, reservedStock, incomingStock, BigDecimal.valueOf(daysOfStockRemaining), stockoutRisk);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
        this.availableStock = Math.max(0, this.currentStock - this.reservedStock);
    }

    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
        this.availableStock = Math.max(0, this.currentStock - this.reservedStock);
    }

    public int getIncomingStock() { return incomingStock; }
    public void setIncomingStock(int incomingStock) { this.incomingStock = incomingStock; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public BigDecimal getDaysOfStockRemaining() { return daysOfStockRemaining; }
    public void setDaysOfStockRemaining(BigDecimal daysOfStockRemaining) { this.daysOfStockRemaining = daysOfStockRemaining; }

    public String getStockoutRisk() { return stockoutRisk; }
    public void setStockoutRisk(String stockoutRisk) { this.stockoutRisk = stockoutRisk; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
