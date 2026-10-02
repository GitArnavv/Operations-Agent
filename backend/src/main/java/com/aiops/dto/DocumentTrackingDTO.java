package com.aiops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Detailed status and extraction result for a tracked document processing job.
 */
public record DocumentTrackingDTO(
    @JsonProperty("jobId") String jobId,
    @JsonProperty("documentId") String documentId,
    @JsonProperty("id") String id, // Compatibility alias
    @JsonProperty("tenantId") String tenantId,
    @JsonProperty("status") String status, // PROCESSING, EXTRACTED, FAILED
    @JsonProperty("title") String title,
    @JsonProperty("fileName") String fileName,
    @JsonProperty("fileType") String fileType,
    @JsonProperty("fileSize") long fileSize,
    @JsonProperty("s3Bucket") String s3Bucket,
    @JsonProperty("s3Key") String s3Key,
    @JsonProperty("presignedDownloadUrl") String presignedDownloadUrl,
    @JsonProperty("url") String url,
    @JsonProperty("extractionSummary") String extractionSummary,
    @JsonProperty("confidenceScore") BigDecimal confidenceScore,
    @JsonProperty("linkedEntityType") String linkedEntityType,
    @JsonProperty("linkedEntityId") String linkedEntityId,
    @JsonProperty("extractedInvoice") InvoiceExtractionDTO extractedInvoice,
    @JsonProperty("errorMessage") String errorMessage,
    @JsonProperty("createdAt") Instant createdAt
) {
    public DocumentTrackingDTO(
            String jobId, String documentId, String tenantId, String status,
            String title, String fileName, String fileType, long fileSize,
            String s3Bucket, String s3Key, String presignedDownloadUrl,
            String extractionSummary, BigDecimal confidenceScore,
            String linkedEntityType, String linkedEntityId,
            InvoiceExtractionDTO extractedInvoice, String errorMessage, Instant createdAt) {
        this(jobId, documentId, documentId, tenantId, status, title, fileName,
             fileType, fileSize, s3Bucket, s3Key, presignedDownloadUrl,
             presignedDownloadUrl, extractionSummary, confidenceScore,
             linkedEntityType, linkedEntityId, extractedInvoice, errorMessage, createdAt);
    }
}
