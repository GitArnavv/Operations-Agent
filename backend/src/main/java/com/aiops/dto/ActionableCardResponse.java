package com.aiops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Actionable approval card returned to frontend/chat UI for sensitive operations.
 * Displays interactive options: Acknowledge, Approve, or Reject.
 */
public record ActionableCardResponse(
        @JsonProperty("requiresApproval")
        boolean requiresApproval,

        @JsonProperty("transactionToken")
        String transactionToken,

        @JsonProperty("approvalId")
        String approvalId,

        @JsonProperty("status")
        String status,

        @JsonProperty("actionType")
        String actionType,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("reason")
        String reason,

        @JsonProperty("riskLevel")
        String riskLevel,

        @JsonProperty("estimatedAmount")
        BigDecimal estimatedAmount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("targetEntityId")
        String targetEntityId,

        @JsonProperty("targetEntityType")
        String targetEntityType,

        @JsonProperty("allowedActions")
        List<String> allowedActions,

        @JsonProperty("actionEndpoints")
        Map<String, String> actionEndpoints,

        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("expiresAt")
        Instant expiresAt
) {}
