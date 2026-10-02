package com.aiops.repository;

import com.aiops.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByTenantId(String tenantId);
    Optional<Product> findByTenantIdAndId(String tenantId, String id);
    Optional<Product> findByTenantIdAndSku(String tenantId, String sku);
    List<Product> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);
    List<Product> findByTenantIdAndCategory(String tenantId, String category);
}
