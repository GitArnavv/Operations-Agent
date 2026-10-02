package com.aiops.repository;

import com.aiops.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findByTenantId(String tenantId);
    Optional<InventoryItem> findByTenantIdAndId(String tenantId, String id);
    List<InventoryItem> findByTenantIdAndProductId(String tenantId, String productId);
    List<InventoryItem> findByTenantIdAndWarehouseId(String tenantId, String warehouseId);
    List<InventoryItem> findByTenantIdAndDaysOfStockRemainingLessThanEqual(String tenantId, BigDecimal days);
    List<InventoryItem> findByTenantIdAndStockoutRiskIn(String tenantId, List<String> risks);
}
