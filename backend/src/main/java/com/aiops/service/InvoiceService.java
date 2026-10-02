package com.aiops.service;

import com.aiops.domain.Invoice;
import com.aiops.domain.enums.PaymentStatus;
import com.aiops.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> getInvoices(String tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    public Optional<Invoice> getInvoiceByNumber(String tenantId, String invoiceNumber) {
        return invoiceRepository.findByTenantIdAndInvoiceNumber(tenantId, invoiceNumber);
    }

    public List<Invoice> getOverdueInvoices(String tenantId) {
        return invoiceRepository.findByTenantIdAndPaymentStatus(tenantId, PaymentStatus.OVERDUE);
    }
}
