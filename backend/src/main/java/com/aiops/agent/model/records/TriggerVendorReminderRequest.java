package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TriggerVendorReminderRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Verified supplier ID (e.g. SUP-POLYCAB, SUP-HAVELLS).")
    String supplierId,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Verified invoice number with format INV-YYYY-XXX (e.g. INV-2026-104).")
    String invoiceNumber,

    @JsonProperty(required = true)
    @JsonPropertyDescription("Operational rationale for dispatching payment or fulfillment reminder.")
    String reason
) {}
