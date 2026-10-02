package com.aiops.service;

import com.aiops.domain.Document;
import com.aiops.dto.DocumentJobResponse;
import com.aiops.dto.DocumentTrackingDTO;
import com.aiops.dto.InvoiceExtractionDTO;
import com.aiops.repository.DocumentRepository;
import com.aiops.repository.InvoiceRepository;
import com.aiops.storage.ObjectStorageService;
import com.aiops.storage.S3StorageProperties;
import com.aiops.storage.S3UploadResult;
import com.aiops.worker.DocumentOcrQueueWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ObjectStorageService objectStorageService;
    private final S3StorageProperties s3Properties;
    private final DocumentOcrQueueWorker ocrQueueWorker;
    private final ObjectMapper objectMapper;

    public DocumentService(
            DocumentRepository documentRepository,
            InvoiceRepository invoiceRepository,
            ObjectStorageService objectStorageService,
            S3StorageProperties s3Properties,
            DocumentOcrQueueWorker ocrQueueWorker,
            ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.invoiceRepository = invoiceRepository;
        this.objectStorageService = objectStorageService;
        this.s3Properties = s3Properties;
        this.ocrQueueWorker = ocrQueueWorker;
        this.objectMapper = objectMapper;
    }

    public List<Document> getDocuments(String tenantId) {
        return documentRepository.findByTenantId(tenantId);
    }

    public Optional<Document> getDocumentById(String tenantId, String id) {
        return documentRepository.findByTenantIdAndId(tenantId, id);
    }

    public Optional<Document> getDocumentByJobId(String tenantId, String jobId) {
        return documentRepository.findByTenantIdAndJobId(tenantId, jobId);
    }

    /**
     * Decoupled document upload:
     * 1. Accepts multipart file.
     * 2. Uploads to S3-compatible object store using pre-signed keys and standard pathing.
     * 3. Persists Document record with initial status 'PROCESSING'.
     * 4. Dispatches asynchronous worker to stream file to Gemini Flash multimodal API.
     * 5. Immediately returns tracking job ID.
     */
    public DocumentJobResponse uploadAndQueueDocument(
            String tenantId, String title, MultipartFile file, String linkedEntityType, String linkedEntityId) {

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/pdf";
        long fileSize = file.getSize();

        try {
            return uploadAndQueueInternal(
                    tenantId,
                    title != null && !title.isBlank() ? title : originalFilename,
                    originalFilename,
                    inferFileType(originalFilename, contentType),
                    fileSize,
                    contentType,
                    file.getInputStream(),
                    linkedEntityType,
                    linkedEntityId
            );
        } catch (Exception e) {
            log.error("Failed to read multipart file for upload: {}", originalFilename, e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }

    /**
     * Overload for JSON/simulated payload ingestion (backwards compatibility).
     */
    public DocumentJobResponse uploadAndQueueDocument(
            String tenantId, String title, String fileName, String fileType, long fileSize) {

        String safeFileName = fileName != null ? fileName : "invoice_document.pdf";
        String safeFileType = fileType != null ? fileType : "PDF";
        byte[] dummyContent = ("%PDF-1.7 Tax Invoice document: " + safeFileName).getBytes(StandardCharsets.UTF_8);

        return uploadAndQueueInternal(
                tenantId,
                title != null ? title : safeFileName,
                safeFileName,
                safeFileType,
                fileSize > 0 ? fileSize : dummyContent.length,
                "application/pdf",
                new ByteArrayInputStream(dummyContent),
                "INVOICE",
                null
        );
    }

    private DocumentJobResponse uploadAndQueueInternal(
            String tenantId, String title, String fileName, String fileType,
            long fileSize, String contentType, InputStream inputStream,
            String linkedEntityType, String linkedEntityId) {

        String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String docId = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String bucket = s3Properties.getBucket();
        String s3Key = String.format("documents/%s/%s/%s", tenantId, jobId, sanitizeFileName(fileName));

        // 1. Upload to S3-compatible object store
        S3UploadResult uploadResult = objectStorageService.uploadFile(bucket, s3Key, inputStream, fileSize, contentType);
        String presignedDownloadUrl = uploadResult.presignedUrl();

        // 2. Persist Document record with status 'PROCESSING'
        Document doc = new Document();
        doc.setId(docId);
        doc.setJobId(jobId);
        doc.setTenantId(tenantId);
        doc.setTitle(title);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(fileSize);
        doc.setS3Bucket(bucket);
        doc.setS3Key(s3Key);
        doc.setUrl(presignedDownloadUrl);
        doc.setStatus("PROCESSING");
        doc.setExtractionSummary("Document uploaded to S3 storage. Asynchronous Gemini Flash OCR extraction in progress.");
        doc.setConfidenceScore(BigDecimal.ZERO);
        doc.setLinkedEntityType(linkedEntityType != null ? linkedEntityType : "INVOICE");
        doc.setLinkedEntityId(linkedEntityId);
        documentRepository.save(doc);

        log.info("Persisted Document [{}] with status 'PROCESSING' for jobId: {}", docId, jobId);

        // 3. Dispatch asynchronous queue worker to stream document to Gemini Flash
        ocrQueueWorker.processDocumentAsync(jobId, docId, tenantId, bucket, s3Key, contentType);

        // 4. Immediately return tracking job ID
        String trackingUrl = "/api/v1/documents/jobs/" + jobId;
        return new DocumentJobResponse(
                jobId,
                docId,
                tenantId,
                title,
                fileName,
                fileType,
                fileSize,
                "PROCESSING",
                bucket,
                s3Key,
                presignedDownloadUrl,
                trackingUrl,
                "Document uploaded successfully. Background multimodal OCR queued.",
                doc.getCreatedAt()
        );
    }

    /**
     * Retrieves tracking status and extraction results for an asynchronous job.
     */
    public Optional<DocumentTrackingDTO> getJobStatus(String tenantId, String jobId) {
        Optional<Document> docOpt = documentRepository.findByTenantIdAndJobId(tenantId, jobId);
        if (docOpt.isEmpty()) {
            docOpt = documentRepository.findByJobId(jobId);
        }
        return docOpt.map(doc -> {
                    InvoiceExtractionDTO extractedInvoice = null;
                    if (doc.getExtractedPayloadJson() != null && !doc.getExtractedPayloadJson().isBlank()) {
                        try {
                            extractedInvoice = objectMapper.readValue(doc.getExtractedPayloadJson(), InvoiceExtractionDTO.class);
                        } catch (Exception e) {
                            log.warn("Failed to parse extractedPayloadJson for docId: {}", doc.getId(), e);
                        }
                    }

                    return new DocumentTrackingDTO(
                            doc.getJobId(),
                            doc.getId(),
                            doc.getTenantId(),
                            doc.getStatus(),
                            doc.getTitle(),
                            doc.getFileName(),
                            doc.getFileType(),
                            doc.getFileSize(),
                            doc.getS3Bucket(),
                            doc.getS3Key(),
                            doc.getUrl(),
                            doc.getExtractionSummary(),
                            doc.getConfidenceScore(),
                            doc.getLinkedEntityType(),
                            doc.getLinkedEntityId(),
                            extractedInvoice,
                            doc.getErrorMessage(),
                            doc.getCreatedAt()
                    );
                });
    }

    /**
     * Legacy method preserved for backwards compatibility with existing seeders and tests.
     */
    public Document processDocumentUpload(String tenantId, String title, String fileName, String fileType, long fileSize) {
        DocumentJobResponse response = uploadAndQueueDocument(tenantId, title, fileName, fileType, fileSize);
        return documentRepository.findById(response.documentId()).orElseThrow();
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String inferFileType(String fileName, String contentType) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf") || contentType.contains("pdf")) return "PDF";
        if (lower.endsWith(".png") || contentType.contains("png")) return "PNG";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || contentType.contains("jpeg")) return "JPEG";
        if (lower.endsWith(".csv")) return "CSV";
        if (lower.endsWith(".xlsx")) return "XLSX";
        return "PDF";
    }
}
