package com.aiops.agent;

import com.aiops.config.AsyncDocumentProcessingConfig;
import com.aiops.domain.Document;
import com.aiops.domain.Invoice;
import com.aiops.dto.DocumentJobResponse;
import com.aiops.dto.DocumentTrackingDTO;
import com.aiops.dto.InvoiceExtractionDTO;
import com.aiops.repository.DocumentRepository;
import com.aiops.repository.InvoiceRepository;
import com.aiops.service.DocumentService;
import com.aiops.service.GeminiMultimodalOcrService;
import com.aiops.storage.ObjectStorageService;
import com.aiops.worker.DocumentOcrQueueWorker;
import com.aiops.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
@WithMockUser(username = "usr_sharma_ops", roles = {"OPERATIONS_MANAGER", "ADMIN"})
public class DocumentIntelligenceOcrPipelineTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Autowired
    private GeminiMultimodalOcrService geminiOcrService;

    @Autowired
    private DocumentOcrQueueWorker ocrQueueWorker;

    @Autowired
    @Qualifier(AsyncDocumentProcessingConfig.DOCUMENT_OCR_TASK_EXECUTOR)
    private Executor documentOcrTaskExecutor;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TENANT_ID = "tenant_test_ocr";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
        // clean up test tenant artifacts
        documentRepository.findByTenantId(TENANT_ID).forEach(d -> documentRepository.delete(d));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("1. Enforced Target Schema: InvoiceExtractionDTO generates strict JSON schema with HSN and GST breakdowns")
    void shouldGenerateStrictJsonSchemaForInvoiceExtractionDTO() {
        BeanOutputConverter<InvoiceExtractionDTO> converter = geminiOcrService.getOutputConverter();
        String jsonSchema = converter.getJsonSchema();

        assertThat(jsonSchema).isNotNull().isNotEmpty();
        assertThat(jsonSchema).contains("vendorGstin");
        assertThat(jsonSchema).contains("invoiceNumber");
        assertThat(jsonSchema).contains("lineItems");
        assertThat(jsonSchema).contains("hsnCode");
        assertThat(jsonSchema).contains("taxableValue");
        assertThat(jsonSchema).contains("cgstTotal");
        assertThat(jsonSchema).contains("sgstTotal");
        assertThat(jsonSchema).contains("igstTotal");
        assertThat(jsonSchema).contains("totalAmount");
        assertThat(jsonSchema).contains("confidenceScore");
    }

    @Test
    @DisplayName("2. Deterministic JSON parsing: Deserializes Indian GST Invoice with 2-decimal precision into InvoiceExtractionDTO")
    void shouldConvertDeterministicJsonIntoInvoiceExtractionDTO() {
        BeanOutputConverter<InvoiceExtractionDTO> converter = geminiOcrService.getOutputConverter();

        String sampleInvoiceJson = """
                {
                    "vendorGstin": "27AAACP8213J1Z8",
                    "vendorName": "Polycab India Ltd.",
                    "invoiceNumber": "INV-UP-8821",
                    "invoiceDate": "2026-10-01",
                    "poReference": "PO-2381",
                    "lineItems": [
                        {
                            "itemDescription": "Polycab Copper Cable 1.5 sq mm",
                            "hsnCode": "8544",
                            "quantity": 100.00,
                            "unitPrice": 370.00,
                            "taxableAmount": 37000.00,
                            "gstRate": 18.00,
                            "cgstAmount": 3330.00,
                            "sgstAmount": 3330.00,
                            "igstAmount": 0.00,
                            "totalAmount": 43660.00
                        }
                    ],
                    "taxableValue": 37000.00,
                    "cgstTotal": 3330.00,
                    "sgstTotal": 3330.00,
                    "igstTotal": 0.00,
                    "totalAmount": 43660.00,
                    "confidenceScore": 0.98,
                    "discrepancyNotes": "Verified against PO-2381. HSN 8544 tax rate verified."
                }
                """;

        InvoiceExtractionDTO dto = converter.convert(sampleInvoiceJson);

        assertThat(dto).isNotNull();
        assertThat(dto.vendorGstin()).isEqualTo("27AAACP8213J1Z8");
        assertThat(dto.invoiceNumber()).isEqualTo("INV-UP-8821");
        assertThat(dto.taxableValue()).isEqualByComparingTo(new BigDecimal("37000.00"));
        assertThat(dto.cgstTotal()).isEqualByComparingTo(new BigDecimal("3330.00"));
        assertThat(dto.sgstTotal()).isEqualByComparingTo(new BigDecimal("3330.00"));
        assertThat(dto.igstTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.totalAmount()).isEqualByComparingTo(new BigDecimal("43660.00"));
        assertThat(dto.lineItems()).hasSize(1);
        assertThat(dto.lineItems().get(0).hsnCode()).isEqualTo("8544");
    }

    @Test
    @DisplayName("3. S3 Object Storage: Generates AWS SigV4 pre-signed PUT and GET keys/URLs and stores bytes")
    void shouldGeneratePresignedS3KeysAndStoreObject() {
        String bucket = "aiops-documents";
        String key = "test/invoice_sample.pdf";
        byte[] content = "%PDF-1.7 Test Invoice Binary Content".getBytes(StandardCharsets.UTF_8);

        // Upload to S3 buffer
        var uploadResult = objectStorageService.uploadFile(bucket, key, new ByteArrayInputStream(content), content.length, "application/pdf");
        assertThat(uploadResult).isNotNull();
        assertThat(uploadResult.key()).isEqualTo(key);
        assertThat(uploadResult.presignedUrl()).contains("X-Amz-Algorithm=AWS4-HMAC-SHA256");
        assertThat(uploadResult.presignedUrl()).contains("X-Amz-Signature=");

        // Generate Pre-signed Upload URL
        String presignedPutUrl = objectStorageService.generatePresignedUploadUrl(bucket, key, Duration.ofMinutes(30));
        assertThat(presignedPutUrl).contains("X-Amz-Algorithm=AWS4-HMAC-SHA256");
        assertThat(presignedPutUrl).contains("X-Amz-Expires=1800");

        // Fetch back bytes
        byte[] fetched = objectStorageService.fetchFileBytes(bucket, key);
        assertThat(fetched).isEqualTo(content);
    }

    @Test
    @DisplayName("4. Decoupled Upload Controller: Accepts multipart file, uploads to S3, returns status 'PROCESSING' and job ID immediately")
    void shouldDecoupleUploadAndImmediatelyReturnTrackingJobId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "polycab_tax_invoice_8821.pdf",
                "application/pdf",
                "%PDF-1.7 Indian Tax Invoice Content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("title", "Polycab Inbound Invoice #8821")
                        .param("linkedEntityType", "INVOICE"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(org.hamcrest.Matchers.startsWith("JOB-")))
                .andExpect(jsonPath("$.documentId").value(org.hamcrest.Matchers.startsWith("DOC-")))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.fileName").value("polycab_tax_invoice_8821.pdf"))
                .andExpect(jsonPath("$.trackingUrl").value(org.hamcrest.Matchers.containsString("/api/v1/documents/jobs/JOB-")))
                .andExpect(jsonPath("$.presignedDownloadUrl").value(org.hamcrest.Matchers.containsString("X-Amz-Algorithm=")));
    }

    @Test
    @DisplayName("5. Custom ThreadPoolTaskExecutor: documentOcrTaskExecutor is properly configured with ocr-worker- prefix")
    void shouldVerifyCustomThreadPoolExecutorConfiguration() {
        assertThat(documentOcrTaskExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) documentOcrTaskExecutor;

        assertThat(threadPool.getCorePoolSize()).isEqualTo(4);
        assertThat(threadPool.getMaxPoolSize()).isEqualTo(10);
        assertThat(threadPool.getThreadNamePrefix()).isEqualTo("ocr-worker-");
    }

    @Test
    @DisplayName("6. Async Queue Worker & Gemini Flash: Processes S3 document, updates Document status to 'EXTRACTED', and creates Invoice")
    void shouldProcessDocumentAsynchronouslyViaOcrWorkerAndStreamToGemini() throws Exception {
        String testJobId = "JOB-ASYNC-TEST";
        String testDocId = "DOC-ASYNC-TEST";
        String bucket = "aiops-documents";
        String key = "documents/" + TENANT_ID + "/" + testJobId + "/polycab_invoice.pdf";
        byte[] docContent = "Polycab India Ltd. Tax Invoice PO-2381 HSN 8544 1.5 sq mm wire".getBytes(StandardCharsets.UTF_8);

        // Put file in S3 store
        objectStorageService.uploadFile(bucket, key, new ByteArrayInputStream(docContent), docContent.length, "application/pdf");

        // Save initial Document record in PROCESSING state
        Document doc = new Document();
        doc.setId(testDocId);
        doc.setJobId(testJobId);
        doc.setTenantId(TENANT_ID);
        doc.setTitle("Polycab Copper Wire Invoice");
        doc.setFileName("polycab_invoice.pdf");
        doc.setFileType("PDF");
        doc.setFileSize(docContent.length);
        doc.setS3Bucket(bucket);
        doc.setS3Key(key);
        doc.setStatus("PROCESSING");
        documentRepository.save(doc);

        // Trigger Async Worker
        CompletableFuture<InvoiceExtractionDTO> future = ocrQueueWorker.processDocumentAsync(
                testJobId, testDocId, TENANT_ID, bucket, key, "application/pdf"
        );

        InvoiceExtractionDTO result = future.get(5, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.vendorGstin()).isEqualTo("27AAACP8213J1Z8");
        assertThat(result.taxableValue()).isEqualByComparingTo(new BigDecimal("37000.00"));
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("43660.00"));

        // Verify Document entity was transitioned to EXTRACTED
        Document updatedDoc = documentRepository.findById(testDocId).orElseThrow();
        assertThat(updatedDoc.getStatus()).isEqualTo("EXTRACTED");
        assertThat(updatedDoc.getConfidenceScore()).isEqualByComparingTo(new BigDecimal("0.98"));
        assertThat(updatedDoc.getLinkedEntityType()).isEqualTo("INVOICE");
        assertThat(updatedDoc.getLinkedEntityId()).isNotNull();

        // Verify Invoice entity was saved
        Invoice savedInvoice = invoiceRepository.findById(updatedDoc.getLinkedEntityId()).orElseThrow();
        assertThat(savedInvoice.getGstin()).isEqualTo("27AAACP8213J1Z8");
        assertThat(savedInvoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("43660.00"));
        assertThat(savedInvoice.getCgst()).isEqualByComparingTo(new BigDecimal("3330.00"));
        assertThat(savedInvoice.getSgst()).isEqualByComparingTo(new BigDecimal("3330.00"));
    }

    @Test
    @DisplayName("7. Job Tracking Endpoint: GET /api/v1/documents/jobs/{jobId} returns DocumentTrackingDTO with status and extracted payload")
    void shouldTrackJobStatusViaGetJobEndpoint() throws Exception {
        // Upload decoupled document via service
        DocumentJobResponse job = documentService.uploadAndQueueDocument(
                TENANT_ID, "Havells Inbound Batch Invoice", "invoice_havells_cable.pdf", "PDF", 450000
        );

        assertThat(job.jobId()).isNotNull();
        assertThat(job.status()).isEqualTo("PROCESSING");

        // Wait briefly for asynchronous executor to finish
        Thread.sleep(800);

        mockMvc.perform(get("/api/v1/documents/jobs/" + job.jobId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(job.jobId()))
                .andExpect(jsonPath("$.status").value("EXTRACTED"))
                .andExpect(jsonPath("$.extractedInvoice.vendorGstin").value("07AAACH1234F1Z5"))
                .andExpect(jsonPath("$.extractedInvoice.totalAmount").value(70800.00))
                .andExpect(jsonPath("$.extractedInvoice.igstTotal").value(10800.00));
    }
}
