package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AuditInvoiceRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Verified invoice reference number with pattern INV-YYYY-XXX (e.g. INV-2026-104).")
    String invoiceNumber
) {}
