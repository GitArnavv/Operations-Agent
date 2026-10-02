package com.aiops.controller;

import com.aiops.domain.Invoice;
import com.aiops.security.SecurityUtils;
import com.aiops.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getInvoices() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(invoiceService.getInvoices(tenantId));
    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return invoiceService.getInvoiceByNumber(tenantId, invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(invoiceService.getOverdueInvoices(tenantId));
    }
}
