package com.aiops.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * Contract for S3-compatible object storage operations (AWS S3 or MinIO).
 * Supports upload with pre-signed keys, secure download URL generation,
 * and streaming document retrieval for OCR processing.
 */
public interface ObjectStorageService {

    /**
     * Uploads a file stream directly to the target S3 bucket and key.
     */
    S3UploadResult uploadFile(String bucket, String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * Generates a pre-signed URL for direct client upload (PUT).
     */
    String generatePresignedUploadUrl(String bucket, String key, Duration duration);

    /**
     * Generates a pre-signed URL for secure client download (GET).
     */
    String generatePresignedDownloadUrl(String bucket, String key, Duration duration);

    /**
     * Fetches the object as an open InputStream for streaming to multimodal AI APIs.
     */
    InputStream fetchFileStream(String bucket, String key);

    /**
     * Fetches the object bytes directly into memory.
     */
    byte[] fetchFileBytes(String bucket, String key);

    /**
     * Checks if the object exists in the bucket.
     */
    boolean fileExists(String bucket, String key);

    /**
     * Deletes an object from the bucket.
     */
    void deleteFile(String bucket, String key);
}
