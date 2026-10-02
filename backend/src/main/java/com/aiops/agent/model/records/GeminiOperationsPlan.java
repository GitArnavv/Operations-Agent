package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record GeminiOperationsPlan(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Classified operations intent: INVENTORY_QUERY, DELAY_ANALYSIS, EXPEDITE_ORDER, SUPPLIER_BENCHMARK, INVOICE_AUDIT, or GENERAL_QUERY.")
    String intent,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Step-by-step reasoning summary citing operational root causes and evidence.")
    String reasoningSummary,

    @JsonProperty(required = true)
    @JsonPropertyDescription("List of tool/function bean names executed during plan fulfillment.")
    List<String> toolCallsRequired,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Deterministic, executive-grade markdown response for the operations dashboard.")
    String finalResponseMarkdown,

    @JsonProperty(required = false)
    @JsonPropertyDescription("Optional proposed action card requiring human management sign-off.")
    ProposedActionRecord proposedAction,

    @JsonProperty(required = true)
    @JsonPropertyDescription("List of verified evidence citations supporting the findings.")
    List<EvidenceCitationRecord> citations
) {}
