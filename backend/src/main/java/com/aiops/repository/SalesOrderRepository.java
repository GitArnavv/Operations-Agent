package com.aiops.repository;

import com.aiops.domain.SalesOrder;
import com.aiops.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, String> {
    List<SalesOrder> findByTenantId(String tenantId);
    Optional<SalesOrder> findByTenantIdAndId(String tenantId, String id);
    Optional<SalesOrder> findByTenantIdAndOrderNumber(String tenantId, String orderNumber);
    List<SalesOrder> findByTenantIdAndStatus(String tenantId, OrderStatus status);
    List<SalesOrder> findByTenantIdAndDeliveryRisk(String tenantId, String deliveryRisk);
    List<SalesOrder> findByTenantIdAndCustomerNameContainingIgnoreCase(String tenantId, String customerName);
}
