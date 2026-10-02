package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record CompareSuppliersRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Material or product category to benchmark (e.g. 'Electrical Wiring', 'Switches', 'MCB').")
    String category,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional list of specific supplier IDs to compare. Leave empty or null to compare all suppliers in this category.")
    List<String> supplierIds
) {}
