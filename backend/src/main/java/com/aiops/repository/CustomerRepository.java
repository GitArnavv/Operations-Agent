package com.aiops.repository;

import com.aiops.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    List<Customer> findByTenantId(String tenantId);
    Optional<Customer> findByTenantIdAndId(String tenantId, String id);
    List<Customer> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);
}
