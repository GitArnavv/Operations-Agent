package com.aiops.repository;

import com.aiops.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByTenantId(String tenantId);
    Optional<Supplier> findByTenantIdAndId(String tenantId, String id);
    List<Supplier> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);
    Optional<Supplier> findByTenantIdAndGstin(String tenantId, String gstin);
}
