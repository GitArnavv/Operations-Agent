package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record ProposedActionRecord(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Action category code: CREATE_PURCHASE_ORDER, TRIGGER_REMINDER, EXPEDITE_DELIVERY, or ADJUST_INVENTORY.")
    String actionType,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Concise title for the manager approval card.")
    String title,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Full operational rationale, financial impact, and execution details.")
    String description,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Risk classification: LOW_RISK, MEDIUM_RISK, or HIGH_RISK.")
    String riskLevel,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Estimated procurement commitment in INR, restricted to 2 decimal places.")
    BigDecimal estimatedCostInr,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Identifier of primary entity impacted (e.g. ORD-1042, INV-2026-104).")
    String affectedEntityId,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Supplier ID assigned to execute the order (e.g. SUP-POLYCAB).")
    String targetSupplierId
) {}
