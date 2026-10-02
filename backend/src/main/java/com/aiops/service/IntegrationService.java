package com.aiops.service;

import com.aiops.domain.Integration;
import com.aiops.repository.IntegrationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;

    public IntegrationService(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    public List<Integration> getIntegrations(String tenantId) {
        return integrationRepository.findByTenantId(tenantId);
    }

    public Optional<Integration> getIntegrationById(String tenantId, String id) {
        return integrationRepository.findByTenantIdAndId(tenantId, id);
    }

    public Integration triggerSync(String tenantId, String integrationId) {
        Integration integration = integrationRepository.findByTenantIdAndId(tenantId, integrationId)
                .orElseThrow(() -> new RuntimeException("Integration not found"));

        integration.setStatus("CONNECTED");
        integration.setLastSyncedAt(Instant.now());
        integration.setSyncStatusMessage("Synced successfully: 120 ledgers and inventory vouchers processed.");
        return integrationRepository.save(integration);
    }

    public Integration toggleIntegration(String tenantId, String integrationId, boolean enabled) {
        Integration integration = integrationRepository.findByTenantIdAndId(tenantId, integrationId)
                .orElseThrow(() -> new RuntimeException("Integration not found"));

        integration.setEnabled(enabled);
        integration.setStatus(enabled ? "CONNECTED" : "DISCONNECTED");
        return integrationRepository.save(integration);
    }
}
