package com.aiops.repository;

import com.aiops.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<AuditLog> findByTenantIdAndId(String tenantId, String id);
    List<AuditLog> findByTenantIdAndEntityType(String tenantId, String entityType);
    List<AuditLog> findByTenantIdAndAction(String tenantId, String action);
}
