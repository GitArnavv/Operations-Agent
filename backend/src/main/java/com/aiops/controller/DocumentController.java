package com.aiops.controller;

import com.aiops.domain.Document;
import com.aiops.dto.DocumentJobResponse;
import com.aiops.dto.DocumentTrackingDTO;
import com.aiops.security.SecurityUtils;
import com.aiops.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Document Intelligence", description = "Decoupled Document Upload & Gemini Flash Multimodal OCR Pipeline")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "List all documents for the current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<Document>> getDocuments() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(documentService.getDocuments(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by unique ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<Document> getDocumentById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return documentService.getDocumentById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Track asynchronous OCR processing job status and fetch extracted invoice data")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<DocumentTrackingDTO> getJobStatus(@PathVariable String jobId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return documentService.getJobStatus(tenantId, jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Decoupled Multipart Upload:
     * Accepts a multipart file, uploads to S3-compatible object store using pre-signed keys,
     * persists record with status 'PROCESSING', and immediately returns a tracking job ID.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document multipart file to S3 store and trigger async Gemini Flash OCR pipeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<DocumentJobResponse> uploadMultipart(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "linkedEntityType", required = false) String linkedEntityType,
            @RequestParam(value = "linkedEntityId", required = false) String linkedEntityId) {

        String tenantId = SecurityUtils.getCurrentTenantId();
        DocumentJobResponse jobResponse = documentService.uploadAndQueueDocument(
                tenantId, title, file, linkedEntityType, linkedEntityId
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobResponse);
    }

    /**
     * JSON Upload for API consumers and frontend compatibility:
     * Queues document processing and immediately returns tracking job ID with status 'PROCESSING'.
     */
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload document metadata / simulated payload and trigger async OCR pipeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<DocumentJobResponse> uploadJson(@RequestBody Map<String, Object> request) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String title = (String) request.getOrDefault("title", "Vendor Invoice");
        String fileName = (String) request.getOrDefault("fileName", "invoice_polycab_oct2026.pdf");
        String fileType = (String) request.getOrDefault("fileType", "PDF");
        long fileSize = ((Number) request.getOrDefault("fileSize", 245000)).longValue();

        DocumentJobResponse jobResponse = documentService.uploadAndQueueDocument(
                tenantId, title, fileName, fileType, fileSize
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobResponse);
    }
}
