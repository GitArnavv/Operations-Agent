package com.aiops.repository;

import com.aiops.domain.Alert;
import com.aiops.domain.enums.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {
    List<Alert> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<Alert> findByTenantIdAndId(String tenantId, String id);
    List<Alert> findByTenantIdAndSeverity(String tenantId, AlertSeverity severity);
    List<Alert> findByTenantIdAndAcknowledged(String tenantId, boolean acknowledged);
}
