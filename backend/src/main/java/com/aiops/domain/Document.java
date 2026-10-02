package com.aiops.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_job_id", columnList = "job_id"),
    @Index(name = "idx_documents_tenant_status", columnList = "tenant_id, status")
})
public class Document {

    @Id
    private String id;

    @Column(name = "job_id", length = 64)
    private String jobId;

    @org.hibernate.annotations.TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false)
    private String title;

    private String fileName;
    private String fileType; // PDF, CSV, XLSX, DOCX, IMAGE
    private long fileSize;

    @Column(name = "s3_bucket", length = 128)
    private String s3Bucket;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Column(name = "url", length = 2048)
    private String url;
    private String status;   // UPLOADED, PROCESSING, EXTRACTED, FAILED

    @Column(length = 2000)
    private String extractionSummary;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal confidenceScore;

    private String linkedEntityType; // INVOICE, PURCHASE_ORDER, DELIVERY
    private String linkedEntityId;

    @Lob
    @Column(name = "extracted_payload_json", columnDefinition = "TEXT")
    private String extractedPayloadJson;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Document() {}

    public Document(String id, String tenantId, String title, String fileName, String fileType, long fileSize, String url, String status, String extractionSummary, BigDecimal confidenceScore, String linkedEntityType, String linkedEntityId) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.url = url;
        this.status = status;
        this.extractionSummary = extractionSummary;
        this.confidenceScore = confidenceScore;
        this.linkedEntityType = linkedEntityType;
        this.linkedEntityId = linkedEntityId;
        this.createdAt = Instant.now();
    }

    public Document(String id, String tenantId, String title, String fileName, String fileType, long fileSize, String url, String status, String extractionSummary, double confidenceScore, String linkedEntityType, String linkedEntityId) {
        this(id, tenantId, title, fileName, fileType, fileSize, url, status, extractionSummary, BigDecimal.valueOf(confidenceScore), linkedEntityType, linkedEntityId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }

    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExtractionSummary() { return extractionSummary; }
    public void setExtractionSummary(String extractionSummary) { this.extractionSummary = extractionSummary; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getLinkedEntityType() { return linkedEntityType; }
    public void setLinkedEntityType(String linkedEntityType) { this.linkedEntityType = linkedEntityType; }

    public String getLinkedEntityId() { return linkedEntityId; }
    public void setLinkedEntityId(String linkedEntityId) { this.linkedEntityId = linkedEntityId; }

    public String getExtractedPayloadJson() { return extractedPayloadJson; }
    public void setExtractedPayloadJson(String extractedPayloadJson) { this.extractedPayloadJson = extractedPayloadJson; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
