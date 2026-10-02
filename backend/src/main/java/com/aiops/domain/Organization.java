package com.aiops.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 15)
    private String gstin;

    private String pan;
    private String state;
    private String city;
    private String industry;
    private String tier; // TRIAL, GROWTH, BUSINESS, ENTERPRISE
    private String currency; // INR
    private String address;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Organization() {}

    public Organization(String id, String name, String gstin, String pan, String state, String city, String industry, String tier, String currency, String address) {
        this.id = id;
        this.name = name;
        this.gstin = gstin;
        this.pan = pan;
        this.state = state;
        this.city = city;
        this.industry = industry;
        this.tier = tier;
        this.currency = currency != null ? currency : "INR";
        this.address = address;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
