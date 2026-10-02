package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CheckStockRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The SKU code (e.g. POL-CU-15-RED) or product name/identifier to check stock for.")
    String skuOrProductName,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional warehouse ID (e.g. WAR-BHI-01) or null to query across all warehouse facilities.")
    String warehouseId
) {}
