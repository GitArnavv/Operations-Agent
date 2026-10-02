package com.aiops.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "integrations")
public class Integration {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String providerName; // Tally Prime, Zoho Books, IndiaMART, WhatsApp Business, Shopify, Razorpay

    private String category;     // ERP, ACCOUNTING, MARKETPLACE, MESSAGING, ECOMMERCE, PAYMENTS
    private String status;       // CONNECTED, SYNCING, FAILED, DISCONNECTED
    private boolean enabled;
    private Instant lastSyncedAt;
    private String syncStatusMessage;

    @Column(length = 1000)
    private String configSummary;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Integration() {}

    public Integration(String id, String tenantId, String providerName, String category, String status, boolean enabled, Instant lastSyncedAt, String syncStatusMessage, String configSummary) {
        this.id = id;
        this.tenantId = tenantId;
        this.providerName = providerName;
        this.category = category;
        this.status = status;
        this.enabled = enabled;
        this.lastSyncedAt = lastSyncedAt;
        this.syncStatusMessage = syncStatusMessage;
        this.configSummary = configSummary;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public String getSyncStatusMessage() { return syncStatusMessage; }
    public void setSyncStatusMessage(String syncStatusMessage) { this.syncStatusMessage = syncStatusMessage; }

    public String getConfigSummary() { return configSummary; }
    public void setConfigSummary(String configSummary) { this.configSummary = configSummary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
