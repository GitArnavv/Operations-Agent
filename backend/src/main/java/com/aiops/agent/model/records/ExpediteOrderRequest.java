package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExpediteOrderRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Verified sales order number in format ORD-XXXX (e.g. ORD-1042).")
    String orderNumber,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Verified supplier ID for the expedited purchase (e.g. SUP-POLYCAB, SUP-HAVELLS).")
    String supplierId,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Quantity of units to expedite.")
    int quantity,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Requested target delivery date in ISO format (YYYY-MM-DD).")
    String expeditedDeliveryDate,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Operational justification and impact analysis for the expedite action.")
    String rationale
) {}
