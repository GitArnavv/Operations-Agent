package com.aiops.repository;

import com.aiops.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByTenantId(String tenantId);
    Optional<Document> findByTenantIdAndId(String tenantId, String id);
    List<Document> findByTenantIdAndStatus(String tenantId, String status);
    List<Document> findByTenantIdAndLinkedEntityId(String tenantId, String linkedEntityId);
    Optional<Document> findByJobId(String jobId);
    Optional<Document> findByTenantIdAndJobId(String tenantId, String jobId);
}
