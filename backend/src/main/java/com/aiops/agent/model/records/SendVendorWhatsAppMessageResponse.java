package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record SendVendorWhatsAppMessageResponse(
        @JsonPropertyDescription("Whether the action is held at the approval gate.")
        boolean requiresApproval,

        @JsonPropertyDescription("Generated pending transaction token (TXN-XXXX).")
        String transactionToken,

        @JsonPropertyDescription("Approval request reference ID (APP-XXXX).")
        String approvalId,

        @JsonPropertyDescription("Recipient supplier name.")
        String supplierName,

        @JsonPropertyDescription("Target phone number.")
        String recipientPhone,

        @JsonPropertyDescription("Interactive actions available: ACKNOWLEDGE, APPROVE, REJECT.")
        List<String> allowedActions,

        @JsonPropertyDescription("Status message detailing approval requirements.")
        String statusMessage
) {}
