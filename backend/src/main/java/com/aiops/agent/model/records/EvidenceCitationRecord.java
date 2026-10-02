package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EvidenceCitationRecord(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Entity domain type: ORDER, INVENTORY, INVOICE, or SUPPLIER.")
    String entityType,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Unique primary entity identifier (e.g. ORD-1042, POL-CU-15-RED).")
    String entityId,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Descriptive citation heading.")
    String title,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Specific metric or fact snippet extracted from internal systems.")
    String snippet,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Grounded confidence score between 0.0 and 1.0.")
    double confidenceScore
) {}
