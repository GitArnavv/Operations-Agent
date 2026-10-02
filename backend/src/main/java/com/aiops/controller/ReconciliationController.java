package com.aiops.controller;

import com.aiops.dto.InvoiceExtractionDTO;
import com.aiops.dto.InvoiceLineItemDTO;
import com.aiops.dto.ReconciliationResultDTO;
import com.aiops.domain.Invoice;
import com.aiops.repository.InvoiceRepository;
import com.aiops.security.SecurityUtils;
import com.aiops.service.ThreeWayMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Invoice Reconciliation", description = "Three-Way Matching across Extracted Invoices, Purchase Orders, and Warehouse GRN Intake")
public class ReconciliationController {

    private final ThreeWayMatchingService matchingService;
    private final InvoiceRepository invoiceRepository;

    public ReconciliationController(
            ThreeWayMatchingService matchingService,
            InvoiceRepository invoiceRepository) {
        this.matchingService = matchingService;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate extracted invoice data and perform 3-way matching against PO and Warehouse GRN intake")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<ReconciliationResultDTO> validateAndReconcile(@RequestBody InvoiceExtractionDTO invoice) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ReconciliationResultDTO result = matchingService.reconcile(invoice, tenantId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @Operation(summary = "Reconcile stored invoice against its referenced Purchase Order and Warehouse GRN")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<ReconciliationResultDTO> reconcileStoredInvoice(@PathVariable String invoiceNumber) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return invoiceRepository.findByTenantIdAndInvoiceNumber(tenantId, invoiceNumber)
                .map(inv -> {
                    InvoiceExtractionDTO dto = convertToDto(inv);
                    return ResponseEntity.ok(matchingService.reconcile(dto, tenantId));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private InvoiceExtractionDTO convertToDto(Invoice inv) {
        List<InvoiceLineItemDTO> items = new ArrayList<>();
        if (inv.getLineItems() != null) {
            inv.getLineItems().forEach(item -> items.add(new InvoiceLineItemDTO(
                    item.getDescription(),
                    item.getHsnCode() != null ? item.getHsnCode() : "8544",
                    BigDecimal.valueOf(item.getQuantity()),
                    item.getUnitPrice(),
                    item.getAmount(),
                    item.getTaxRate() != null ? item.getTaxRate() : new BigDecimal("18.00"),
                    inv.getCgst(),
                    inv.getSgst(),
                    inv.getIgst(),
                    item.getAmount()
            )));
        }

        return new InvoiceExtractionDTO(
                inv.getGstin(),
                inv.getEntityName(),
                inv.getInvoiceNumber(),
                inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : null,
                inv.getPoNumber(),
                items,
                inv.getSubtotal(),
                inv.getCgst(),
                inv.getSgst(),
                inv.getIgst(),
                inv.getTotalAmount(),
                inv.getExtractionConfidence(),
                inv.getDiscrepancyNotes()
        );
    }
}
