package com.aiops.agent.model.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;

public record ExpediteOrderResponse(
    @JsonPropertyDescription("Whether the expedite action request was successfully processed.")
    boolean success,

    @JsonPropertyDescription("Generated Approval Request ID (e.g. APP-XXXXXXXX) awaiting human approval.")
    String approvalRequestId,

    @JsonPropertyDescription("Pending transaction token (e.g. TXN-XXXXXXXX) for the approval gate.")
    String transactionToken,

    @JsonPropertyDescription("Sales order reference being expedited.")
    String orderNumber,

    @JsonPropertyDescription("Verified supplier legal name.")
    String supplierName,

    @JsonPropertyDescription("Quantity of units ordered.")
    int quantity,

    @JsonPropertyDescription("Total estimated procurement cost in INR formatted to 2 decimals.")
    BigDecimal estimatedCostInr,

    @JsonPropertyDescription("Assessed operational risk classification: LOW_RISK, MEDIUM_RISK, or HIGH_RISK.")
    String riskLevel,

    @JsonPropertyDescription("Detailed explanation of the staged action and required human sign-off.")
    String message
) {
    public ExpediteOrderResponse(boolean success,
                                 String approvalRequestId,
                                 String orderNumber,
                                 String supplierName,
                                 int quantity,
                                 BigDecimal estimatedCostInr,
                                 String riskLevel,
                                 String message) {
        this(success, approvalRequestId, "TXN-" + approvalRequestId, orderNumber, supplierName, quantity, estimatedCostInr, riskLevel, message);
    }
}
