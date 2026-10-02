package com.aiops.repository;

import com.aiops.domain.Integration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, String> {
    List<Integration> findByTenantId(String tenantId);
    Optional<Integration> findByTenantIdAndId(String tenantId, String id);
    Optional<Integration> findByTenantIdAndProviderName(String tenantId, String providerName);
}
