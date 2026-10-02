package com.aiops.service;

import com.aiops.domain.AuditLog;
import com.aiops.interceptor.AuditContextHolder;
import com.aiops.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> getAuditLogs(String tenantId) {
        return auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public Optional<AuditLog> getAuditLogById(String tenantId, String id) {
        return auditLogRepository.findByTenantIdAndId(tenantId, id);
    }

    /**
     * Records an immutable audit log entry. Telemetry like IP address and prompt context
     * are automatically extracted from AuditContextHolder if not explicitly provided.
     */
    @Transactional
    public AuditLog recordAudit(String tenantId,
                                String actingUserId,
                                String actingAgentId,
                                String action,
                                String toolExecuted,
                                String promptContext,
                                String entityType,
                                String entityId,
                                String beforeEntityState,
                                String afterEntityState,
                                String reason) {
        String logId = "AUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String clientIp = AuditContextHolder.getClientIp();
        String resolvedPrompt = (promptContext != null && !promptContext.isBlank())
                ? promptContext
                : AuditContextHolder.getPromptContext();
        String requestId = AuditContextHolder.getRequestId();
        String agentId = (actingAgentId != null) ? actingAgentId : AuditContextHolder.getActingAgentId();

        AuditLog auditLog = new AuditLog(
                logId,
                tenantId,
                actingUserId,
                agentId,
                actingUserId != null ? actingUserId : agentId,
                action,
                toolExecuted,
                resolvedPrompt,
                entityType,
                entityId,
                beforeEntityState,
                afterEntityState,
                reason,
                requestId,
                null,
                clientIp
        );

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Recorded immutable audit entry: ID={}, action={}, tool={}, actor={}, entity={}:{}, ip={}",
                saved.getId(), saved.getAction(), saved.getToolExecuted(), saved.getActingUserId(),
                saved.getEntityType(), saved.getEntityId(), saved.getIpAddress());
        return saved;
    }

    @Transactional
    public AuditLog recordAudit(String tenantId,
                                String actingUserId,
                                String actingAgentId,
                                String action,
                                String toolExecuted,
                                String beforeEntityState,
                                String afterEntityState,
                                String promptContext) {
        return recordAudit(tenantId, actingUserId, actingAgentId, action, toolExecuted, promptContext,
                "DOCUMENT", null, beforeEntityState, afterEntityState, action);
    }
}
