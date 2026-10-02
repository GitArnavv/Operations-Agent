package com.aiops.domain;

import com.aiops.domain.enums.OrderStatus;
import com.aiops.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String orderNumber; // e.g. ORD-1042

    @Column(nullable = false)
    private String customerId;

    private String customerName;
    private LocalDate orderDate;
    private LocalDate promisedDeliveryDate;
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal subtotal;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal taxAmount;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal totalAmount;

    private String deliveryRisk; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private int delayDays;

    @Column(length = 1000)
    private String delayRootCause;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SalesOrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SalesOrder() {}

    public SalesOrder(String id, String tenantId, String orderNumber, String customerId, String customerName, LocalDate orderDate, LocalDate promisedDeliveryDate, LocalDate expectedDeliveryDate, OrderStatus status, PaymentStatus paymentStatus, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount, String deliveryRisk, int delayDays, String delayRootCause) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.promisedDeliveryDate = promisedDeliveryDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.deliveryRisk = deliveryRisk;
        this.delayDays = delayDays;
        this.delayRootCause = delayRootCause;
        this.createdAt = Instant.now();
    }

    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getPromisedDeliveryDate() { return promisedDeliveryDate; }
    public void setPromisedDeliveryDate(LocalDate promisedDeliveryDate) { this.promisedDeliveryDate = promisedDeliveryDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryRisk() { return deliveryRisk; }
    public void setDeliveryRisk(String deliveryRisk) { this.deliveryRisk = deliveryRisk; }

    public int getDelayDays() { return delayDays; }
    public void setDelayDays(int delayDays) { this.delayDays = delayDays; }

    public String getDelayRootCause() { return delayRootCause; }
    public void setDelayRootCause(String delayRootCause) { this.delayRootCause = delayRootCause; }

    public List<SalesOrderItem> getItems() { return items; }
    public void setItems(List<SalesOrderItem> items) { this.items = items; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
