package com.aiops.domain.enums;

/**
 * Status of document validation and three-way reconciliation.
 */
public enum ReconciliationStatus {
    MATCHED,
    FLAGGED_FOR_REVIEW,
    REJECTED
}
