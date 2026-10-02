package com.aiops;

import com.aiops.agent.AgentMessage;
import com.aiops.agent.AgentOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class AiOpsApplicationTests {

    @Autowired
    private AgentOrchestrator orchestrator;

    @Test
    void contextLoads() {
        assertNotNull(orchestrator);
    }

    @Test
    void testAttentionQueryScenario() {
        AgentMessage msg = orchestrator.processUserRequest(
                "What needs my attention today?", "org_sharma_001", "usr_sharma_owner", "conv_test"
        );
        assertNotNull(msg);
        assertNotNull(msg.getContent());
        assertTrue(msg.getContent().contains("Sharma Electronics") || msg.getContent().contains("ABC Traders") || msg.getContent().contains("attention"));
        assertFalse(msg.getCitations().isEmpty());
    }

    @Test
    void testOrderDelayInvestigationScenario() {
        AgentMessage msg = orchestrator.processUserRequest(
                "Why is Sharma Electronics order delayed?", "org_sharma_001", "usr_sharma_owner", "conv_test"
        );
        assertNotNull(msg);
        assertTrue(msg.getContent().contains("ORD-1042") || msg.getContent().contains("delayed"));
        assertFalse(msg.getCitations().isEmpty());
    }

    @Test
    void testActionFixProposalScenario() {
        AgentMessage msg = orchestrator.processUserRequest(
                "Fix the delayed order", "org_sharma_001", "usr_sharma_owner", "conv_test"
        );
        assertNotNull(msg);
        assertNotNull(msg.getProposedAction());
        assertEquals("CREATE_PURCHASE_ORDER", msg.getProposedAction().getActionType());
        assertTrue(msg.getProposedAction().isRequiresApproval());
    }
}
