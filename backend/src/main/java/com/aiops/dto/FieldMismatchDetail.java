package com.aiops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detailed discrepancy report for a specific field or line item during three-way matching.
 */
public record FieldMismatchDetail(
    @JsonProperty("fieldName") String fieldName,
    @JsonProperty("itemDescription") String itemDescription,
    @JsonProperty("expectedValue") String expectedValue,
    @JsonProperty("actualValue") String actualValue,
    @JsonProperty("variance") String variance,
    @JsonProperty("severity") String severity, // CRITICAL, WARNING
    @JsonProperty("message") String message
) {}
