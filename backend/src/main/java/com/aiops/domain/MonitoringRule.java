package com.aiops.domain;

import com.aiops.domain.enums.MetricType;
import com.aiops.domain.enums.RuleOperator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "monitoring_rules")
public class MonitoringRule {

    @Id
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String ruleName;

    @Column(length = 1000)
    private String naturalLanguagePrompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleOperator operator;

    @Column(precision = 15, scale = 2, columnDefinition = "DECIMAL(15, 2)")
    private BigDecimal threshold;
    private String frequency; // HOURLY, DAILY, REALTIME
    private String scope;     // ALL_PRODUCTS, SPECIFIC_CATEGORY, ALL_ORDERS, HIGH_VALUE_INVOICES
    private boolean active = true;

    private Instant lastEvaluatedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public MonitoringRule() {}

    public MonitoringRule(String id, String tenantId, String ruleName, String naturalLanguagePrompt, MetricType metricType, RuleOperator operator, BigDecimal threshold, String frequency, String scope) {
        this.id = id;
        this.tenantId = tenantId;
        this.ruleName = ruleName;
        this.naturalLanguagePrompt = naturalLanguagePrompt;
        this.metricType = metricType;
        this.operator = operator;
        this.threshold = threshold;
        this.frequency = frequency;
        this.scope = scope;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public MonitoringRule(String id, String tenantId, String ruleName, String naturalLanguagePrompt, MetricType metricType, RuleOperator operator, double threshold, String frequency, String scope) {
        this(id, tenantId, ruleName, naturalLanguagePrompt, metricType, operator, BigDecimal.valueOf(threshold), frequency, scope);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getNaturalLanguagePrompt() { return naturalLanguagePrompt; }
    public void setNaturalLanguagePrompt(String naturalLanguagePrompt) { this.naturalLanguagePrompt = naturalLanguagePrompt; }

    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) { this.metricType = metricType; }

    public RuleOperator getOperator() { return operator; }
    public void setOperator(RuleOperator operator) { this.operator = operator; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastEvaluatedAt() { return lastEvaluatedAt; }
    public void setLastEvaluatedAt(Instant lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
