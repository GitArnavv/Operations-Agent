package com.aiops.dto;

import com.aiops.domain.enums.ReconciliationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Result of three-way matching and reconciliation across Invoice, Purchase Order, and GRN.
 * Returns reconciliation confidence score and detailed field mismatches.
 */
public record ReconciliationResultDTO(
    @JsonProperty("reconciliationId") String reconciliationId,
    @JsonProperty("tenantId") String tenantId,
    @JsonProperty("invoiceNumber") String invoiceNumber,
    @JsonProperty("poNumber") String poNumber,
    @JsonProperty("grnNumber") String grnNumber,
    @JsonProperty("confidenceScore") BigDecimal confidenceScore, // Scale 2, e.g. 98.00
    @JsonProperty("status") ReconciliationStatus status,
    @JsonProperty("requiresHumanReview") boolean requiresHumanReview,
    @JsonProperty("mathValid") boolean mathValid,
    @JsonProperty("poMatched") boolean poMatched,
    @JsonProperty("grnMatched") boolean grnMatched,
    @JsonProperty("priceMatched") boolean priceMatched,
    @JsonProperty("quantityMatched") boolean quantityMatched,
    @JsonProperty("mismatches") List<FieldMismatchDetail> mismatches,
    @JsonProperty("reconciliationSummary") String reconciliationSummary,
    @JsonProperty("reconciledAt") Instant reconciledAt
) {}
