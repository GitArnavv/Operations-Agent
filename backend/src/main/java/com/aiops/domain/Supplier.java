package com.aiops.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "suppliers")
public class Supplier {

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

    private int leadTimeDays;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal onTimeDeliveryRate; // e.g. 91.50%

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal defectRate;         // e.g. 1.20%

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal reliabilityScore;   // e.g. 4.70 / 5.00

    private String paymentTerms;      // e.g. "Net 30"
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Supplier() {}

    public Supplier(String id, String tenantId, String name, String gstin, String city, String state, String contactPerson, String phone, String email, int leadTimeDays, BigDecimal onTimeDeliveryRate, BigDecimal defectRate, BigDecimal reliabilityScore, String paymentTerms) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.gstin = gstin;
        this.city = city;
        this.state = state;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.leadTimeDays = leadTimeDays;
        this.onTimeDeliveryRate = onTimeDeliveryRate;
        this.defectRate = defectRate;
        this.reliabilityScore = reliabilityScore;
        this.paymentTerms = paymentTerms;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Supplier(String id, String tenantId, String name, String gstin, String city, String state, String contactPerson, String phone, String email, int leadTimeDays, double onTimeDeliveryRate, double defectRate, double reliabilityScore, String paymentTerms) {
        this(id, tenantId, name, gstin, city, state, contactPerson, phone, email, leadTimeDays, BigDecimal.valueOf(onTimeDeliveryRate), BigDecimal.valueOf(defectRate), BigDecimal.valueOf(reliabilityScore), paymentTerms);
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

    public int getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(int leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public BigDecimal getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
    public void setOnTimeDeliveryRate(BigDecimal onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }

    public BigDecimal getDefectRate() { return defectRate; }
    public void setDefectRate(BigDecimal defectRate) { this.defectRate = defectRate; }

    public BigDecimal getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(BigDecimal reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
