package com.aiops.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String gstin;
    private String city;
    private String state;
    private String contactPerson;
    private String phone;
    private String email;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal creditLimit;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal outstandingAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Customer() {}

    public Customer(String id, String tenantId, String name, String gstin, String city, String state, String contactPerson, String phone, String email, BigDecimal creditLimit, BigDecimal outstandingAmount) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.gstin = gstin;
        this.city = city;
        this.state = state;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.creditLimit = creditLimit;
        this.outstandingAmount = outstandingAmount;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
