package com.aiops.repository;

import com.aiops.domain.MonitoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoringRuleRepository extends JpaRepository<MonitoringRule, String> {
    List<MonitoringRule> findByTenantId(String tenantId);
    Optional<MonitoringRule> findByTenantIdAndId(String tenantId, String id);
    List<MonitoringRule> findByTenantIdAndActive(String tenantId, boolean active);
}
