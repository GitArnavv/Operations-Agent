package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record SupplierMetricRecord(
    @JsonPropertyDescription("Supplier unique system identifier.")
    String supplierId,

    @JsonPropertyDescription("Registered legal supplier name.")
    String supplierName,

    @JsonPropertyDescription("Historical on-time delivery percentage rate (e.g. 94.20%).")
    BigDecimal onTimeDeliveryRate,

    @JsonPropertyDescription("Average turnaround lead time in days.")
    int avgLeadTimeDays,

    @JsonPropertyDescription("Historical defect rate percentage (e.g. 0.80%).")
    BigDecimal defectRate,

    @JsonPropertyDescription("Composite reliability score on a 5.0 scale.")
    BigDecimal reliabilityScore
) {}
