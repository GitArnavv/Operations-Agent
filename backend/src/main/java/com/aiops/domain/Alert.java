package com.aiops.domain;

import com.aiops.domain.enums.AlertSeverity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    private String entityType; // PRODUCT, ORDER, INVOICE, SUPPLIER
    private String entityId;
    private String metric;
    private String recommendedAction;
    private String deepLink;
    private boolean acknowledged = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Alert() {}

    public Alert(String id, String tenantId, String title, String message, AlertSeverity severity, String entityType, String entityId, String metric, String recommendedAction, String deepLink) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.entityType = entityType;
        this.entityId = entityId;
        this.metric = metric;
        this.recommendedAction = recommendedAction;
        this.deepLink = deepLink;
        this.acknowledged = false;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getDeepLink() { return deepLink; }
    public void setDeepLink(String deepLink) { this.deepLink = deepLink; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
