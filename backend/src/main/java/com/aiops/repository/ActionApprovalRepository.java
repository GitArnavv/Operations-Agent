package com.aiops.repository;

import com.aiops.domain.ActionApproval;
import com.aiops.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionApprovalRepository extends JpaRepository<ActionApproval, String> {
    List<ActionApproval> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<ActionApproval> findByTenantIdAndId(String tenantId, String id);
    Optional<ActionApproval> findByTenantIdAndTransactionToken(String tenantId, String transactionToken);
    Optional<ActionApproval> findByTransactionToken(String transactionToken);
    List<ActionApproval> findByTenantIdAndStatus(String tenantId, ApprovalStatus status);
}
