package com.aiops.storage;

import java.time.Instant;

/**
 * Result of an S3 object upload operation.
 */
public record S3UploadResult(
    String bucket,
    String key,
    String eTag,
    String presignedUrl,
    String locationUrl,
    long contentLength,
    String contentType,
    Instant uploadedAt
) {}
