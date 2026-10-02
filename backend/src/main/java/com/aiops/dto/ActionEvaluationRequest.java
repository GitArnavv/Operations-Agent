package com.aiops.dto;

import com.aiops.domain.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Request payload when evaluating whether an autonomous action requires an approval gate.
 */
public record ActionEvaluationRequest(
        @JsonProperty(required = true)
        String actionType,

        @JsonProperty(required = true)
        String title,

        @JsonProperty(required = true)
        String description,

        @JsonProperty(required = false)
        String reason,

        @JsonProperty(required = false)
        RiskLevel riskLevel,

        @JsonProperty(required = false)
        BigDecimal estimatedAmount,

        @JsonProperty(required = false)
        String currency,

        @JsonProperty(required = false)
        String targetEntityId,

        @JsonProperty(required = false)
        String targetEntityType,

        @JsonProperty(required = false)
        Map<String, Object> payload,

        @JsonProperty(required = false)
        String promptContext
) {}
