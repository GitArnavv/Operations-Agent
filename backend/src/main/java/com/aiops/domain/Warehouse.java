package com.aiops.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code; // e.g. WH-BHIWANDI, WH-PUNE

    private String city;
    private String state;
    private String address;
    private int capacityUnits;
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Warehouse() {}

    public Warehouse(String id, String tenantId, String name, String code, String city, String state, String address, int capacityUnits) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.code = code;
        this.city = city;
        this.state = state;
        this.address = address;
        this.capacityUnits = capacityUnits;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getCapacityUnits() { return capacityUnits; }
    public void setCapacityUnits(int capacityUnits) { this.capacityUnits = capacityUnits; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
