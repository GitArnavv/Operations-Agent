package com.aiops.repository;

import com.aiops.domain.AgentRun;
import com.aiops.domain.enums.AgentRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRun, String> {
    List<AgentRun> findByTenantIdOrderByStartedAtDesc(String tenantId);
    Optional<AgentRun> findByTenantIdAndId(String tenantId, String id);
    List<AgentRun> findByTenantIdAndStatus(String tenantId, AgentRunStatus status);
}
