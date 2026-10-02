package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record CheckStockResponse(
    @JsonPropertyDescription("Verified product SKU code.")
    String sku,

    @JsonPropertyDescription("Verified product name.")
    String productName,

    @JsonPropertyDescription("Warehouse facility name or 'All Facilities'.")
    String warehouseName,

    @JsonPropertyDescription("Total physical quantity in warehouse.")
    int physicalStock,

    @JsonPropertyDescription("Quantity allocated/reserved for active customer orders.")
    int reservedStock,

    @JsonPropertyDescription("Net available stock for immediate fulfillment.")
    int availableStock,

    @JsonPropertyDescription("Estimated days of stock remaining at current consumption velocity.")
    BigDecimal daysOfStockRemaining,

    @JsonPropertyDescription("Risk classification: LOW, MEDIUM, HIGH, or CRITICAL.")
    String stockoutRisk,

    @JsonPropertyDescription("Flag indicating if inventory is below safety threshold.")
    boolean isCritical
) {}
