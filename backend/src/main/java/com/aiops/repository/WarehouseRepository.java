package com.aiops.repository;

import com.aiops.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    List<Warehouse> findByTenantId(String tenantId);
    Optional<Warehouse> findByTenantIdAndId(String tenantId, String id);
}
