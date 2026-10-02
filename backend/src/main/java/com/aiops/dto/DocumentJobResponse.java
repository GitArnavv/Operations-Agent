package com.aiops.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Immediate response returned upon decoupled document upload.
 * Contains tracking job ID, initial 'PROCESSING' status, and S3 pre-signed location keys.
 */
public record DocumentJobResponse(
    @JsonProperty("jobId") String jobId,
    @JsonProperty("documentId") String documentId,
    @JsonProperty("id") String id, // Backwards-compatible alias for frontend DocumentRecord.id
    @JsonProperty("tenantId") String tenantId,
    @JsonProperty("title") String title,
    @JsonProperty("fileName") String fileName,
    @JsonProperty("fileType") String fileType,
    @JsonProperty("fileSize") long fileSize,
    @JsonProperty("status") String status,
    @JsonProperty("s3Bucket") String s3Bucket,
    @JsonProperty("s3Key") String s3Key,
    @JsonProperty("presignedDownloadUrl") String presignedDownloadUrl,
    @JsonProperty("url") String url,
    @JsonProperty("trackingUrl") String trackingUrl,
    @JsonProperty("message") String message,
    @JsonProperty("createdAt") Instant createdAt
) {
    public DocumentJobResponse(
            String jobId, String documentId, String tenantId, String title,
            String fileName, String fileType, long fileSize, String status,
            String s3Bucket, String s3Key, String presignedDownloadUrl,
            String trackingUrl, String message, Instant createdAt) {
        this(jobId, documentId, documentId, tenantId, title, fileName, fileType,
             fileSize, status, s3Bucket, s3Key, presignedDownloadUrl,
             presignedDownloadUrl, trackingUrl, message, createdAt);
    }
}
