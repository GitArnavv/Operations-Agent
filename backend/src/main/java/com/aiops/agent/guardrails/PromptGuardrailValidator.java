package com.aiops.agent.guardrails;

import com.aiops.domain.Invoice;
import com.aiops.domain.Product;
import com.aiops.domain.SalesOrder;
import com.aiops.domain.Supplier;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.ProductRepository;
import com.aiops.service.InvoiceService;
import com.aiops.service.OrderService;
import com.aiops.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Prompt guardrails and business scope verification.
 * Enforces strict entity ID patterns, prompt injection defense, and tenant isolation.
 */
@Component
public class PromptGuardrailValidator {

    private static final Logger log = LoggerFactory.getLogger(PromptGuardrailValidator.class);

    // Business Entity ID Patterns
    public static final Pattern ORDER_ID_PATTERN = Pattern.compile("^ORD-[0-9]{3,}$");
    public static final Pattern INVOICE_ID_PATTERN = Pattern.compile("^INV-[0-9]{4}-[0-9]{2,}$");
    public static final Pattern SUPPLIER_ID_PATTERN = Pattern.compile("^SUP-[A-Z0-9_-]+$");
    public static final Pattern WAREHOUSE_ID_PATTERN = Pattern.compile("^WAR-[A-Z0-9_-]+$");
    public static final Pattern SKU_OR_PROD_PATTERN = Pattern.compile("^(PROD-[A-Z0-9_-]+|[A-Z0-9_-]{3,})$");

    // Prompt Injection & Malicious Pattern Heuristics
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)(ignore|disregard|forget)\\s+(all\\s+)?(previous|prior|system)\\s+(instructions|directives|prompts)"),
            Pattern.compile("(?i)(system\\s+prompt|reveal\\s+prompt|developer\\s+mode|jailbreak)"),
            Pattern.compile("(?i)(drop\\s+(table|database)|delete\\s+from|truncate\\s+table|grant\\s+all|insert\\s+into\\s+users)"),
            Pattern.compile("(?i)(export|dump|print|reveal)\\s+.*?(passwords?|credentials?|api_?keys?|secrets?)"),
            Pattern.compile("(?i)(bypass\\s+(approval|auth|security|guardrails|authorization))"),
            Pattern.compile("(?i)(force\\s+(transfer|debit|payment)\\s+without\\s+approval)")
    );

    private final OrderService orderService;
    private final SupplierService supplierService;
    private final InvoiceService invoiceService;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public PromptGuardrailValidator(OrderService orderService,
                                    SupplierService supplierService,
                                    InvoiceService invoiceService,
                                    ProductRepository productRepository,
                                    InventoryItemRepository inventoryItemRepository) {
        this.orderService = orderService;
        this.supplierService = supplierService;
        this.invoiceService = invoiceService;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Sanitizes raw user prompt, removes invisible control characters, and blocks prompt injection attempts.
     */
    public String sanitizeUserPrompt(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            throw new GuardrailViolationException("EMPTY_PROMPT", "Prompt must not be empty or blank.");
        }

        // Strip non-printable ASCII/Unicode control characters except common line feeds and tabs
        String cleaned = rawPrompt.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();

        // Check length bounds
        if (cleaned.length() > 4000) {
            throw new GuardrailViolationException("PROMPT_TOO_LONG", "Prompt exceeds maximum allowed length of 4000 characters.");
        }

        // Test against injection patterns
        for (Pattern p : INJECTION_PATTERNS) {
            if (p.matcher(cleaned).find()) {
                log.warn("Blocked prompt injection or unauthorized system command matching pattern: {}", p.pattern());
                throw new GuardrailViolationException("PROMPT_INJECTION_DETECTED",
                        "Security Guardrail Violation: Input contains prohibited prompt override or injection keywords.");
            }
        }

        return cleaned;
    }

    /**
     * Enforces tenant isolation scope.
     */
    public void validateTenantScope(String authenticatedTenantId, String requestedTenantId) {
        if (authenticatedTenantId == null || requestedTenantId == null
                || !Objects.equals(authenticatedTenantId.trim(), requestedTenantId.trim())) {
            throw new GuardrailViolationException("TENANT_SCOPE_MISMATCH",
                    "Access denied: Requested entity does not belong to the active authenticated tenant.");
        }
    }

    /**
     * Validates and verifies a sales order exists within the caller tenant scope.
     */
    public SalesOrder validateAndFetchOrder(String tenantId, String orderNumber) {
        if (orderNumber == null || !ORDER_ID_PATTERN.matcher(orderNumber.trim()).matches()) {
            throw new GuardrailViolationException("INVALID_ORDER_FORMAT",
                    "Invalid order number format: '" + orderNumber + "'. Expected pattern like ORD-1042.");
        }

        return orderService.getOrderByNumber(tenantId, orderNumber.trim())
                .orElseThrow(() -> new GuardrailViolationException("ORDER_NOT_FOUND",
                        "Order '" + orderNumber + "' not found within verified tenant scope."));
    }

    /**
     * Validates and verifies a supplier exists within the caller tenant scope.
     */
    public Supplier validateAndFetchSupplier(String tenantId, String supplierId) {
        if (supplierId == null || !SUPPLIER_ID_PATTERN.matcher(supplierId.trim()).matches()) {
            throw new GuardrailViolationException("INVALID_SUPPLIER_FORMAT",
                    "Invalid supplier ID format: '" + supplierId + "'. Expected pattern like SUP-POLYCAB.");
        }

        return supplierService.getSupplierById(tenantId, supplierId.trim())
                .orElseThrow(() -> new GuardrailViolationException("SUPPLIER_NOT_FOUND",
                        "Supplier '" + supplierId + "' not found within verified tenant scope."));
    }

    /**
     * Validates and verifies an invoice exists within the caller tenant scope.
     */
    public Invoice validateAndFetchInvoice(String tenantId, String invoiceNumber) {
        if (invoiceNumber == null || !INVOICE_ID_PATTERN.matcher(invoiceNumber.trim()).matches()) {
            throw new GuardrailViolationException("INVALID_INVOICE_FORMAT",
                    "Invalid invoice number format: '" + invoiceNumber + "'. Expected pattern like INV-2026-104.");
        }

        return invoiceService.getInvoiceByNumber(tenantId, invoiceNumber.trim())
                .orElseThrow(() -> new GuardrailViolationException("INVOICE_NOT_FOUND",
                        "Invoice '" + invoiceNumber + "' not found within verified tenant scope."));
    }

    /**
     * Validates product SKU or identifier and ensures it exists in the tenant's catalog or inventory.
     */
    public Product validateAndFetchProduct(String tenantId, String skuOrIdentifier) {
        if (skuOrIdentifier == null || skuOrIdentifier.trim().isBlank()) {
            throw new GuardrailViolationException("INVALID_PRODUCT_FORMAT", "Product identifier cannot be blank.");
        }

        String trimmed = skuOrIdentifier.trim();
        List<Product> products = productRepository.findByTenantId(tenantId);
        return products.stream()
                .filter(p -> p.getSku().equalsIgnoreCase(trimmed)
                        || p.getId().equalsIgnoreCase(trimmed)
                        || p.getName().toLowerCase().contains(trimmed.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new GuardrailViolationException("PRODUCT_NOT_FOUND",
                        "Product or SKU '" + skuOrIdentifier + "' not found in catalog."));
    }
}
