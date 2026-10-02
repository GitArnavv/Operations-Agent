package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record SupplierComparisonResponse(
    @JsonPropertyDescription("The queried category.")
    String category,

    @JsonPropertyDescription("List of benchmarked supplier metrics.")
    List<SupplierMetricRecord> suppliers,

    @JsonPropertyDescription("Top recommended supplier identifier.")
    String topRecommendedSupplier,

    @JsonPropertyDescription("Analytical justification explaining why the top supplier was selected.")
    String recommendationRationale
) {}
