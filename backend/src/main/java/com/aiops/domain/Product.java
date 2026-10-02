package com.aiops.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String category;
    private String hsnCode; // Indian GST HSN code, e.g. 8544
    private String unit;    // MTR, PCS, BOX, SET

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal gstRate; // 5%, 12%, 18%, 28%

    private int reorderPoint;
    private int safetyStock;
    private int leadTimeDays;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal avgDailyDemand;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Product() {}

    public Product(String id, String tenantId, String sku, String name, String category, String hsnCode, String unit, BigDecimal unitPrice, BigDecimal gstRate, int reorderPoint, int safetyStock, int leadTimeDays, BigDecimal avgDailyDemand, String description) {
        this.id = id;
        this.tenantId = tenantId;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.hsnCode = hsnCode;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.gstRate = gstRate;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.leadTimeDays = leadTimeDays;
        this.avgDailyDemand = avgDailyDemand;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public Product(String id, String tenantId, String sku, String name, String category, String hsnCode, String unit, BigDecimal unitPrice, double gstRate, int reorderPoint, int safetyStock, int leadTimeDays, double avgDailyDemand, String description) {
        this(id, tenantId, sku, name, category, hsnCode, unit, unitPrice, BigDecimal.valueOf(gstRate), reorderPoint, safetyStock, leadTimeDays, BigDecimal.valueOf(avgDailyDemand), description);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHSNCode() { return hsnCode; }
    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

    public int getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }

    public int getSafetyStock() { return safetyStock; }
    public void setSafetyStock(int safetyStock) { this.safetyStock = safetyStock; }

    public int getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(int leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public BigDecimal getAvgDailyDemand() { return avgDailyDemand; }
    public void setAvgDailyDemand(BigDecimal avgDailyDemand) { this.avgDailyDemand = avgDailyDemand; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
