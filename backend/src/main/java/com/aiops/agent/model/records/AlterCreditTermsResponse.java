package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record AlterCreditTermsResponse(
        @JsonPropertyDescription("Whether the action is held at the approval gate.")
        boolean requiresApproval,

        @JsonPropertyDescription("Generated pending transaction token (TXN-XXXX).")
        String transactionToken,

        @JsonPropertyDescription("Approval request reference ID (APP-XXXX).")
        String approvalId,

        @JsonPropertyDescription("Target entity identifier.")
        String entityId,

        @JsonPropertyDescription("Proposed new terms.")
        String proposedTerms,

        @JsonPropertyDescription("Allowed actions for the interactive card: ACKNOWLEDGE, APPROVE, REJECT.")
        List<String> allowedActions,

        @JsonPropertyDescription("Policy enforcement summary.")
        String statusMessage
) {}
