package com.aiops.repository;

import com.aiops.domain.ApprovalRequest;
import com.aiops.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {
    List<ApprovalRequest> findByTenantId(String tenantId);
    Optional<ApprovalRequest> findByTenantIdAndId(String tenantId, String id);
    List<ApprovalRequest> findByTenantIdAndStatus(String tenantId, ApprovalStatus status);
}
