package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record AlterCreditTermsRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Identifier of supplier or customer (e.g. SUP-POLYCAB or CUST-0002).")
        String entityId,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Entity type: 'SUPPLIER' or 'CUSTOMER'.")
        String entityType,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Current payment terms (e.g. 'Net 30').")
        String currentTerms,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Proposed new payment terms (e.g. 'Net 60', 'Advance 50%', 'Net 15').")
        String proposedTerms,

        @JsonProperty(required = false)
        @JsonPropertyDescription("Proposed revised credit limit in INR.")
        BigDecimal newCreditLimit,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Business justification for credit modification.")
        String justification
) {}
