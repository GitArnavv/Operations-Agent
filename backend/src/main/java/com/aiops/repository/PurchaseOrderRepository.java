package com.aiops.repository;

import com.aiops.domain.PurchaseOrder;
import com.aiops.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    List<PurchaseOrder> findByTenantId(String tenantId);
    Optional<PurchaseOrder> findByTenantIdAndId(String tenantId, String id);
    Optional<PurchaseOrder> findByTenantIdAndPoNumber(String tenantId, String poNumber);
    List<PurchaseOrder> findByTenantIdAndSupplierId(String tenantId, String supplierId);
    List<PurchaseOrder> findByTenantIdAndStatus(String tenantId, OrderStatus status);
}
