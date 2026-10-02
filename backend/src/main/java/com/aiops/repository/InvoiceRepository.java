package com.aiops.repository;

import com.aiops.domain.Invoice;
import com.aiops.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByTenantId(String tenantId);
    Optional<Invoice> findByTenantIdAndId(String tenantId, String id);
    Optional<Invoice> findByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);
    List<Invoice> findByTenantIdAndPaymentStatus(String tenantId, PaymentStatus status);
    List<Invoice> findByTenantIdAndPaymentStatusAndTotalAmountGreaterThanEqual(String tenantId, PaymentStatus status, BigDecimal amount);
    List<Invoice> findByTenantIdAndOverdueDaysGreaterThan(String tenantId, int days);
    List<Invoice> findByTenantIdAndEntityNameContainingIgnoreCase(String tenantId, String name);
}
