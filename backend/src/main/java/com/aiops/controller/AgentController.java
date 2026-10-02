package com.aiops.controller;

import com.aiops.agent.AgentMessage;
import com.aiops.agent.AgentOrchestrator;
import com.aiops.agent.GeminiAgentOrchestratorService;
import com.aiops.agent.model.records.GeminiOperationsPlan;
import com.aiops.domain.AgentRun;
import com.aiops.repository.AgentRunRepository;
import com.aiops.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;
    private final GeminiAgentOrchestratorService geminiOrchestrator;
    private final AgentRunRepository agentRunRepository;

    public AgentController(AgentOrchestrator agentOrchestrator,
                           GeminiAgentOrchestratorService geminiOrchestrator,
                           AgentRunRepository agentRunRepository) {
        this.agentOrchestrator = agentOrchestrator;
        this.geminiOrchestrator = geminiOrchestrator;
        this.agentRunRepository = agentRunRepository;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<AgentMessage> chat(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String conversationId = request.getOrDefault("conversationId", "conv_default");
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();

        AgentMessage response = geminiOrchestrator.processUserRequest(prompt, tenantId, userId, conversationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<com.aiops.agent.model.records.GeminiOperationsPlan> plan(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String conversationId = request.getOrDefault("conversationId", "conv_default");
        String tenantId = SecurityUtils.getCurrentTenantId();
        String userId = SecurityUtils.getCurrentUserId();

        com.aiops.agent.model.records.GeminiOperationsPlan plan = geminiOrchestrator.orchestrateWithGemini(prompt, tenantId, userId, conversationId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<List<AgentRun>> getRuns() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(agentRunRepository.findByTenantIdOrderByStartedAtDesc(tenantId));
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATIONS_MANAGER', 'AUDITOR')")
    public ResponseEntity<AgentRun> getRunById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return agentRunRepository.findByTenantIdAndId(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
