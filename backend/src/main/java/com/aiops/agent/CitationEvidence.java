package com.aiops.agent;

import java.math.BigDecimal;

public class CitationEvidence {
    private String entityType; // ORDER, INVOICE, PURCHASE_ORDER, INVENTORY, SUPPLIER, DOCUMENT
    private String entityId;
    private String label;      // e.g. "Order #ORD-1042", "PO #PO-2381"
    private String summary;    // e.g. "Promised Oct 2, delayed 2 days due to missing stock"
    private BigDecimal confidence; // 0.00 - 1.00
    private String deepLink;   // e.g. "/orders/ORD-1042"

    public CitationEvidence() {}

    public CitationEvidence(String entityType, String entityId, String label, String summary, BigDecimal confidence, String deepLink) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.label = label;
        this.summary = summary;
        this.confidence = confidence;
        this.deepLink = deepLink;
    }

    public CitationEvidence(String entityType, String entityId, String label, String summary, double confidence, String deepLink) {
        this(entityType, entityId, label, summary, BigDecimal.valueOf(confidence), deepLink);
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getDeepLink() { return deepLink; }
    public void setDeepLink(String deepLink) { this.deepLink = deepLink; }
}
