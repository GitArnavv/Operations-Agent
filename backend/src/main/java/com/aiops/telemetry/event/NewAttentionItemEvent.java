package com.aiops.telemetry.event;

import java.time.Instant;

public class NewAttentionItemEvent implements TelemetryEvent {
    private final String tenantId;
    private final String attentionId;
    private final String type;
    private final String message;
    private final Instant timestamp;

    public NewAttentionItemEvent(String tenantId, String attentionId, String type, String message) {
        this.tenantId = tenantId;
        this.attentionId = attentionId;
        this.type = type;
        this.message = message;
        this.timestamp = Instant.now();
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    @Override
    public String eventType() {
        return "NEW_ATTENTION_ITEM";
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    public String getAttentionId() { return attentionId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
}
