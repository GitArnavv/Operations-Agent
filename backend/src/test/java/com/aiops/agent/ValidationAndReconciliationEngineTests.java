package com.aiops.agent;

import com.aiops.domain.GoodsReceiptNote;
import com.aiops.domain.PurchaseOrder;
import com.aiops.domain.enums.ReconciliationStatus;
import com.aiops.dto.FieldMismatchDetail;
import com.aiops.dto.InvoiceExtractionDTO;
import com.aiops.dto.InvoiceLineItemDTO;
import com.aiops.dto.ReconciliationResultDTO;
import com.aiops.repository.GoodsReceiptNoteRepository;
import com.aiops.repository.PurchaseOrderRepository;
import com.aiops.service.ThreeWayMatchingService;
import com.aiops.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
@WithMockUser(username = "usr_sharma_ops", roles = {"OPERATIONS_MANAGER", "ADMIN"})
public class ValidationAndReconciliationEngineTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Validator validator;

    @Autowired
    private ThreeWayMatchingService threeWayMatchingService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private GoodsReceiptNoteRepository goodsReceiptNoteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Matches seeded organization tenant and supplier in DemoDataSeeder
    private static final String TENANT_ID = "org_sharma_001";
    private static final String POLYCAB_GSTIN = "27AAACP8000J1Z0";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Jakarta Validation: Valid GSTIN regex format accepted")
    void testValidGstinRegex() {
        InvoiceLineItemDTO item = createLineItem(
                "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43660.00")
        );

        InvoiceExtractionDTO validDto = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-VAL-01", "PO-2381",
                List.of(item),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.00"),
                new BigDecimal("0.98"), "Valid GSTIN check"
        );

        Set<ConstraintViolation<InvoiceExtractionDTO>> violations = validator.validate(validDto);
        boolean gstinViolation = violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("vendorGstin"));
        assertThat(gstinViolation).isFalse();
    }

    @Test
    @DisplayName("Jakarta Validation: Invalid GSTIN regex format rejected")
    void testInvalidGstinRegex() {
        String[] invalidGstins = {
                "27aaach7409r1zz",   // lowercase letters
                "27AAACH7409R1Z",    // 14 chars
                "27AAACH7409R1ZZZ",  // 16 chars
                "27AAACH7409R11Z",   // 14th char not Z
                "INVALID_GSTIN_123"  // arbitrary string
        };

        for (String invalidGstin : invalidGstins) {
            InvoiceLineItemDTO item = createLineItem(
                    "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                    new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                    new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43660.00")
            );

            InvoiceExtractionDTO invalidDto = createSampleInvoice(
                    invalidGstin, "Polycab India Ltd.", "INV-VAL-02", "PO-2381",
                    List.of(item),
                    new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                    BigDecimal.ZERO, new BigDecimal("43660.00"),
                    new BigDecimal("0.98"), "Invalid GSTIN check"
            );

            Set<ConstraintViolation<InvoiceExtractionDTO>> violations = validator.validate(invalidDto);
            boolean gstinViolation = violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("vendorGstin"));
            assertThat(gstinViolation)
                    .as("GSTIN '" + invalidGstin + "' should fail regex validation")
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Jakarta Validation: Line-item math validation within ₹0.01 tolerance")
    void testLineItemMathTolerance() {
        // Exact match: 37000 + 3330 + 3330 = 43660.00
        InvoiceLineItemDTO exactItem = createLineItem(
                "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43660.00")
        );
        InvoiceExtractionDTO exactMath = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-MATH-01", "PO-2381",
                List.of(exactItem),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.00"),
                new BigDecimal("0.98"), "Exact match"
        );
        Set<ConstraintViolation<InvoiceExtractionDTO>> exactViolations = validator.validate(exactMath);
        boolean mathError = exactViolations.stream().anyMatch(v -> v.getMessage().contains("tolerance") || v.getPropertyPath().toString().contains("lineItemMath"));
        assertThat(mathError).isFalse();

        // 1 paisa (₹0.01) rounding discrepancy: passes tolerance
        InvoiceLineItemDTO onePaisaItem = createLineItem(
                "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43660.01")
        );
        InvoiceExtractionDTO onePaisaDiff = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-MATH-02", "PO-2381",
                List.of(onePaisaItem),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.01"), // +0.01 paisa difference
                new BigDecimal("0.98"), "One paisa difference"
        );
        Set<ConstraintViolation<InvoiceExtractionDTO>> onePaisaViolations = validator.validate(onePaisaDiff);
        boolean onePaisaError = onePaisaViolations.stream().anyMatch(v -> v.getMessage().contains("tolerance") || v.getPropertyPath().toString().contains("lineItemMath"));
        assertThat(onePaisaError).isFalse();

        // Discrepancy > ₹0.01 (e.g. ₹50.00 discrepancy): fails validation
        InvoiceLineItemDTO badItem = createLineItem(
                "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43710.00")
        );
        InvoiceExtractionDTO mathDiscrepancy = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-MATH-03", "PO-2381",
                List.of(badItem),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43710.00"), // Differs by ₹50.00
                new BigDecimal("0.98"), "Discrepancy test"
        );
        Set<ConstraintViolation<InvoiceExtractionDTO>> failedViolations = validator.validate(mathDiscrepancy);
        boolean failedMathError = failedViolations.stream().anyMatch(v -> v.getMessage().contains("tolerance") || v.getPropertyPath().toString().contains("lineItemMath") || v.getMessage().contains("Grand total does not match"));
        assertThat(failedMathError).isTrue();
    }

    @Test
    @DisplayName("ThreeWayMatchingService: Perfect 3-way match achieves 100% confidence without review flag")
    void testThreeWayMatching_PerfectMatch() {
        InvoiceLineItemDTO item = createLineItem(
                "Polycab Flame Retardant Wire 1.5 sq mm (90m)", "8544",
                new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"),
                new BigDecimal("3330.00"), new BigDecimal("3330.00"), BigDecimal.ZERO,
                new BigDecimal("43660.00")
        );

        InvoiceExtractionDTO invoice = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-TEST-PERFECT-01", "PO-2381",
                List.of(item),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.00"),
                new BigDecimal("0.99"), "Original e-invoice"
        );

        ReconciliationResultDTO result = threeWayMatchingService.reconcile(invoice, TENANT_ID);

        assertThat(result.confidenceScore()).isGreaterThanOrEqualTo(new BigDecimal("95.00"));
        assertThat(result.status()).isEqualTo(ReconciliationStatus.MATCHED);
        assertThat(result.requiresHumanReview()).isFalse();
        assertThat(result.poMatched()).isTrue();
        assertThat(result.grnMatched()).isTrue();
        assertThat(result.priceMatched()).isTrue();
        assertThat(result.quantityMatched()).isTrue();
        assertThat(result.mismatches()).isEmpty();
    }

    @Test
    @DisplayName("ThreeWayMatchingService: Price discrepancy flags record for human review with exact details")
    void testThreeWayMatching_PriceDiscrepancy_FlagsForReview() {
        // Invoiced unit price is ₹385.00 vs PO unit price ₹370.00 (+₹15.00 variance)
        BigDecimal invoicedUnitPrice = new BigDecimal("385.00");
        BigDecimal invoicedTaxable = invoicedUnitPrice.multiply(new BigDecimal("100")); // 38500.00
        BigDecimal cgst = invoicedTaxable.multiply(new BigDecimal("0.09")); // 3465.00
        BigDecimal sgst = invoicedTaxable.multiply(new BigDecimal("0.09")); // 3465.00
        BigDecimal total = invoicedTaxable.add(cgst).add(sgst); // 45430.00

        InvoiceLineItemDTO item = createLineItem(
                "Polycab Flame Retardant Wire 1.5 sq mm (90m)", "8544",
                new BigDecimal("100"), invoicedUnitPrice,
                invoicedTaxable, new BigDecimal("18.00"),
                cgst, sgst, BigDecimal.ZERO, total
        );

        InvoiceExtractionDTO invoice = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-TEST-PRICE-DIFF-02", "PO-2381",
                List.of(item),
                invoicedTaxable, cgst, sgst, BigDecimal.ZERO, total,
                new BigDecimal("0.98"), "Price increase on invoice"
        );

        ReconciliationResultDTO result = threeWayMatchingService.reconcile(invoice, TENANT_ID);

        assertThat(result.confidenceScore()).isLessThan(new BigDecimal("95.00"));
        assertThat(result.status()).isEqualTo(ReconciliationStatus.FLAGGED_FOR_REVIEW);
        assertThat(result.requiresHumanReview()).isTrue();
        assertThat(result.priceMatched()).isFalse();

        // Exact mismatch details
        Optional<FieldMismatchDetail> priceMismatch = result.mismatches().stream()
                .filter(m -> m.fieldName().equals("unitPrice"))
                .findFirst();

        assertThat(priceMismatch).isPresent();
        assertThat(priceMismatch.get().expectedValue()).contains("370.00");
        assertThat(priceMismatch.get().actualValue()).contains("385.00");
        assertThat(priceMismatch.get().variance()).contains("+₹15.00");
        assertThat(priceMismatch.get().severity()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("ThreeWayMatchingService: Quantity over-invoicing flags record for review against GRN intake")
    void testThreeWayMatching_QuantityDiscrepancy_FlagsForReview() {
        // Invoiced quantity is 130 units vs GRN intake of 100 units
        BigDecimal quantity = new BigDecimal("130");
        BigDecimal unitPrice = new BigDecimal("370.00");
        BigDecimal taxable = quantity.multiply(unitPrice); // 48100.00
        BigDecimal cgst = taxable.multiply(new BigDecimal("0.09")); // 4329.00
        BigDecimal sgst = taxable.multiply(new BigDecimal("0.09")); // 4329.00
        BigDecimal total = taxable.add(cgst).add(sgst); // 56758.00

        InvoiceLineItemDTO item = createLineItem(
                "Polycab Flame Retardant Wire 1.5 sq mm (90m)", "8544",
                quantity, unitPrice,
                taxable, new BigDecimal("18.00"),
                cgst, sgst, BigDecimal.ZERO, total
        );

        InvoiceExtractionDTO invoice = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-TEST-QTY-DIFF-03", "PO-2381",
                List.of(item),
                taxable, cgst, sgst, BigDecimal.ZERO, total,
                new BigDecimal("0.98"), "Over-invoiced quantity"
        );

        ReconciliationResultDTO result = threeWayMatchingService.reconcile(invoice, TENANT_ID);

        assertThat(result.confidenceScore()).isLessThan(new BigDecimal("95.00"));
        assertThat(result.status()).isEqualTo(ReconciliationStatus.FLAGGED_FOR_REVIEW);
        assertThat(result.requiresHumanReview()).isTrue();
        assertThat(result.quantityMatched()).isFalse();

        // Exact mismatch details
        Optional<FieldMismatchDetail> qtyMismatch = result.mismatches().stream()
                .filter(m -> m.fieldName().equals("quantity"))
                .findFirst();

        assertThat(qtyMismatch).isPresent();
        assertThat(qtyMismatch.get().expectedValue()).contains("100");
        assertThat(qtyMismatch.get().actualValue()).contains("130");
        assertThat(qtyMismatch.get().variance()).contains("+30 units over-invoiced");
    }

    @Test
    @DisplayName("ThreeWayMatchingService: Unknown Purchase Order flags critical review")
    void testThreeWayMatching_UnknownPO_FlagsForReview() {
        InvoiceLineItemDTO item = createLineItem(
                "Copper Wire 2.5mm", "8544", new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"), new BigDecimal("3330.00"),
                new BigDecimal("3330.00"), BigDecimal.ZERO, new BigDecimal("43660.00")
        );

        InvoiceExtractionDTO invoice = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-TEST-UNKNOWN-PO-04", "PO-NON-EXISTENT-9999",
                List.of(item),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.00"),
                new BigDecimal("0.98"), "Unregistered PO"
        );

        ReconciliationResultDTO result = threeWayMatchingService.reconcile(invoice, TENANT_ID);

        assertThat(result.confidenceScore()).isLessThan(new BigDecimal("95.00"));
        assertThat(result.status()).isEqualTo(ReconciliationStatus.FLAGGED_FOR_REVIEW);
        assertThat(result.requiresHumanReview()).isTrue();
        assertThat(result.poMatched()).isFalse();

        Optional<FieldMismatchDetail> poMismatch = result.mismatches().stream()
                .filter(m -> m.fieldName().equals("poReference"))
                .findFirst();
        assertThat(poMismatch).isPresent();
        assertThat(poMismatch.get().variance()).isEqualTo("PO Not Found");
    }

    @Test
    @DisplayName("REST API: POST /api/v1/reconciliation/validate executes 3-way reconciliation")
    void testReconciliationApi_ValidateEndpoint() throws Exception {
        InvoiceLineItemDTO item = createLineItem(
                "Polycab Flame Retardant Wire 1.5 sq mm (90m)", "8544",
                new BigDecimal("100"), new BigDecimal("370.00"),
                new BigDecimal("37000.00"), new BigDecimal("18.00"),
                new BigDecimal("3330.00"), new BigDecimal("3330.00"), BigDecimal.ZERO,
                new BigDecimal("43660.00")
        );

        InvoiceExtractionDTO validDto = createSampleInvoice(
                POLYCAB_GSTIN, "Polycab India Ltd.", "INV-API-TEST-01", "PO-2381",
                List.of(item),
                new BigDecimal("37000.00"), new BigDecimal("3330.00"), new BigDecimal("3330.00"),
                BigDecimal.ZERO, new BigDecimal("43660.00"),
                new BigDecimal("0.98"), "API test invoice"
        );

        mockMvc.perform(post("/api/v1/reconciliation/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHED"))
                .andExpect(jsonPath("$.requiresHumanReview").value(false))
                .andExpect(jsonPath("$.confidenceScore").value(100.00))
                .andExpect(jsonPath("$.mathValid").value(true))
                .andExpect(jsonPath("$.poMatched").value(true));
    }

    private InvoiceLineItemDTO createLineItem(
            String description, String hsn, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal taxable, BigDecimal gstRate, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal total) {
        return new InvoiceLineItemDTO(
                description, hsn, quantity, unitPrice, taxable, gstRate, cgst, sgst, igst, total
        );
    }

    private InvoiceExtractionDTO createSampleInvoice(
            String gstin, String vendorName, String invoiceNumber, String poRef,
            List<InvoiceLineItemDTO> items, BigDecimal taxable, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal total,
            BigDecimal confidence, String notes) {
        return new InvoiceExtractionDTO(
                gstin,
                vendorName,
                invoiceNumber,
                "2026-10-02",
                poRef,
                items,
                taxable,
                cgst,
                sgst,
                igst,
                total,
                confidence,
                notes
        );
    }
}
