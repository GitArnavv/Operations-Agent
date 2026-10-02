package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SendVendorWhatsAppMessageRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Verified supplier ID (e.g. SUP-POLYCAB).")
        String supplierId,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Reference entity (e.g. INV-2026-104 or PO-2381).")
        String referenceNumber,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Recipient phone number with country code (e.g. +91 98201 12345).")
        String recipientPhone,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Proposed WhatsApp message body or template parameters.")
        String messageBody,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Operational reason for sending the WhatsApp message.")
        String reason
) {}
