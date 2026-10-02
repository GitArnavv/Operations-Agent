package com.aiops.telemetry.event;

import java.time.Instant;

public class StockBreachEvent implements TelemetryEvent {
    private final String tenantId;
    private final String itemId;
    private final String itemName;
    private final int currentStock;
    private final int threshold;
    private final Instant timestamp;

    public StockBreachEvent(String tenantId, String itemId, String itemName, int currentStock, int threshold) {
        this.tenantId = tenantId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.threshold = threshold;
        this.timestamp = Instant.now();
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    @Override
    public String eventType() {
        return "STOCK_BREACH";
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getCurrentStock() { return currentStock; }
    public int getThreshold() { return threshold; }
}
