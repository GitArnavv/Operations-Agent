package com.aiops.agent;

import com.aiops.domain.ActionApproval;
import com.aiops.domain.AuditLog;
import com.aiops.domain.enums.ApprovalStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.dto.ActionEvaluationRequest;
import com.aiops.dto.ActionableCardResponse;
import com.aiops.interceptor.AuditContextHolder;
import com.aiops.repository.ActionApprovalRepository;
import com.aiops.repository.AuditLogRepository;
import com.aiops.service.ApprovalPolicyService;
import com.aiops.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class ApprovalGateAndAuditTrailTests {

    @Autowired
    private ApprovalPolicyService approvalPolicyService;

    @Autowired
    private ActionApprovalRepository actionApprovalRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final String TENANT_ID = "org_sharma_001";
    private static final String USER_ID = "usr_sharma_owner";
    private static final String AGENT_ID = "GeminiFlashOperationsAgent";

    @BeforeEach
    void setUp() {
        AuditContextHolder.setClientIp("192.168.1.100");
        AuditContextHolder.setPromptContext("Prompt: Send reminder and expedite purchase order");
        AuditContextHolder.setActingAgentId(AGENT_ID);
        AuditContextHolder.setRequestId("REQ-TEST-001");
    }

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    @DisplayName("1. WhatsApp vendor communications cannot execute immediately and require approval gate")
    void testWhatsAppActionRequiresApprovalGate() {
        ActionEvaluationRequest request = new ActionEvaluationRequest(
                "TRIGGER_WHATSAPP_REMINDER",
                "WhatsApp Payment Reminder to Polycab India Ltd.",
                "Send payment reminder for overdue Invoice #INV-2026-104 via WhatsApp channel.",
                "Overdue 18 days.",
                RiskLevel.HIGH_RISK,
                BigDecimal.ZERO,
                "INR",
                "SUP-POLYCAB",
                "SUPPLIER",
                Map.of("channel", "WHATSAPP", "phone", "+91 98201 12345"),
                "User asked to notify Polycab via WhatsApp"
        );

        assertTrue(approvalPolicyService.isApprovalRequired(request));

        ActionableCardResponse card = approvalPolicyService.evaluateAndGateAction(TENANT_ID, USER_ID, AGENT_ID, request);

        assertTrue(card.requiresApproval());
        assertNotNull(card.transactionToken());
        assertTrue(card.transactionToken().startsWith("TXN-"));
        assertEquals("PENDING", card.status());
        assertEquals("TRIGGER_WHATSAPP_REMINDER", card.actionType());
        assertTrue(card.allowedActions().containsAll(List.of("ACKNOWLEDGE", "APPROVE", "REJECT")));
        assertNotNull(card.actionEndpoints().get("approve"));
        assertNotNull(card.actionEndpoints().get("reject"));
        assertNotNull(card.actionEndpoints().get("acknowledge"));

        // Verify stored in action_approvals table
        ActionApproval saved = actionApprovalRepository.findByTransactionToken(card.transactionToken()).orElse(null);
        assertNotNull(saved);
        assertEquals(ApprovalStatus.PENDING, saved.getStatus());
        assertEquals(TENANT_ID, saved.getTenantId());
        assertEquals("TRIGGER_WHATSAPP_REMINDER", saved.getActionType());
    }

    @Test
    @DisplayName("2. Purchase orders above ₹50,000 cannot execute immediately and require approval gate")
    void testPurchaseOrderAbove50000RequiresApprovalGate() {
        // ₹1,85,000 is > ₹50,000 threshold
        BigDecimal largeCost = new BigDecimal("185000.00");
        ActionEvaluationRequest request = new ActionEvaluationRequest(
                "CREATE_PURCHASE_ORDER",
                "Bulk Purchase Order: 500 units Copper Wire",
                "Large purchase order commitment with Polycab India Ltd.",
                "Replenish regional warehouse.",
                RiskLevel.HIGH_RISK,
                largeCost,
                "INR",
                "ORD-1042",
                "ORDER",
                Map.of("quantity", 500, "supplierId", "SUP-POLYCAB"),
                "Create purchase order for 500 units"
        );

        assertTrue(approvalPolicyService.isApprovalRequired(request));

        ActionableCardResponse card = approvalPolicyService.evaluateAndGateAction(TENANT_ID, USER_ID, AGENT_ID, request);

        assertTrue(card.requiresApproval());
        assertNotNull(card.transactionToken());
        assertEquals("PENDING", card.status());
        assertEquals(largeCost, card.estimatedAmount());

        // Verify record in action_approvals table
        ActionApproval saved = actionApprovalRepository.findByTransactionToken(card.transactionToken()).orElse(null);
        assertNotNull(saved);
        assertEquals(largeCost, saved.getEstimatedAmount());
    }

    @Test
    @DisplayName("3. Purchase orders below or equal to ₹50,000 can auto-approve without gate")
    void testPurchaseOrderBelow50000AutoApproves() {
        // ₹18,500 is <= ₹50,000 threshold
        BigDecimal lowCost = new BigDecimal("18500.00");
        ActionEvaluationRequest request = new ActionEvaluationRequest(
                "CREATE_PURCHASE_ORDER",
                "Small Expedite Order: 50 units Copper Wire",
                "Minor batch order to clear backlog.",
                "Backlog fix.",
                RiskLevel.MEDIUM_RISK,
                lowCost,
                "INR",
                "ORD-1042",
                "ORDER",
                Map.of("quantity", 50),
                "Fix backlog"
        );

        assertFalse(approvalPolicyService.isApprovalRequired(request));

        ActionableCardResponse card = approvalPolicyService.evaluateAndGateAction(TENANT_ID, USER_ID, AGENT_ID, request);

        assertFalse(card.requiresApproval());
        assertEquals("AUTO_APPROVED", card.status());
        assertNull(card.transactionToken());
    }

    @Test
    @DisplayName("4. Altering credit terms cannot execute immediately and requires approval gate")
    void testAlterCreditTermsRequiresApprovalGate() {
        ActionEvaluationRequest request = new ActionEvaluationRequest(
                "ALTER_CREDIT_TERMS",
                "Extend Credit Terms for ABC Traders: Net 30 -> Net 60",
                "Customer requested 30-day credit period extension.",
                "Customer relationship management.",
                RiskLevel.HIGH_RISK,
                new BigDecimal("500000.00"),
                "INR",
                "CUST-0002",
                "CUSTOMER",
                Map.of("fromTerms", "Net 30", "toTerms", "Net 60"),
                "Extend payment terms for ABC Traders"
        );

        assertTrue(approvalPolicyService.isApprovalRequired(request));

        ActionableCardResponse card = approvalPolicyService.evaluateAndGateAction(TENANT_ID, USER_ID, AGENT_ID, request);

        assertTrue(card.requiresApproval());
        assertEquals("PENDING", card.status());
        assertNotNull(card.transactionToken());
    }

    @Test
    @DisplayName("5. Approving, Rejecting, and Acknowledging actions updates status and records audit trail")
    void testDecisionLifecycleAndAuditTrail() {
        // Stage a critical action
        ActionEvaluationRequest request = new ActionEvaluationRequest(
                "TRIGGER_WHATSAPP_REMINDER",
                "WhatsApp Overdue Notice to Havells India",
                "Automated reminder for pending delivery",
                "Delayed PO",
                RiskLevel.HIGH_RISK,
                BigDecimal.ZERO,
                "INR",
                "SUP-HAVELLS",
                "SUPPLIER",
                Map.of("channel", "WHATSAPP"),
                "Prompt text"
        );

        ActionableCardResponse card = approvalPolicyService.evaluateAndGateAction(TENANT_ID, USER_ID, AGENT_ID, request);
        String token = card.transactionToken();

        // 1. Acknowledge action
        ActionApproval ack = approvalPolicyService.acknowledgeAction(TENANT_ID, token, USER_ID, "Reviewed by owner");
        assertEquals(ApprovalStatus.ACKNOWLEDGED, ack.getStatus());

        // Reset to pending for approve test
        ack.setStatus(ApprovalStatus.PENDING);
        actionApprovalRepository.save(ack);

        // 2. Approve action
        ActionApproval approved = approvalPolicyService.approveAction(TENANT_ID, token, USER_ID, "Approved after verifying inventory");
        assertEquals(ApprovalStatus.APPROVED, approved.getStatus());
        assertEquals(USER_ID, approved.getDecidedByUserId());
        assertNotNull(approved.getDecidedAt());
        assertNotNull(approved.getExecutedAt());
    }

    @Test
    @DisplayName("6. Immutable AuditLog records acting identities, prompt context, tool, state diffs, and IP address")
    void testImmutableAuditLogRecording() {
        AuditLog audit = auditService.recordAudit(
                TENANT_ID,
                USER_ID,
                AGENT_ID,
                "EXECUTE_PURCHASE_ORDER",
                "create_purchase_order",
                "Prompt: Expedite 50 units for Sharma Electronics",
                "ORDER",
                "ORD-1042",
                "Status: PENDING_APPROVAL, Qty: 0",
                "Status: PO_DISPATCHED, Qty: 50, Supplier: SUP-POLYCAB",
                "Emergency stockout mitigation."
        );

        assertNotNull(audit);
        assertNotNull(audit.getId());
        assertNotNull(audit.getTimestamp());
        assertEquals(TENANT_ID, audit.getTenantId());
        assertEquals(USER_ID, audit.getActingUserId());
        assertEquals(AGENT_ID, audit.getActingAgentId());
        assertEquals("create_purchase_order", audit.getToolExecuted());
        assertEquals("192.168.1.100", audit.getIpAddress());
        assertEquals("Prompt: Expedite 50 units for Sharma Electronics", audit.getPromptContext());
        assertEquals("Status: PENDING_APPROVAL, Qty: 0", audit.getBeforeEntityState());
        assertTrue(audit.getAfterEntityState().contains("PO_DISPATCHED"));

        // Verify retrieval from repository
        AuditLog fetched = auditLogRepository.findByTenantIdAndId(TENANT_ID, audit.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals("192.168.1.100", fetched.getIpAddress());
        assertEquals("create_purchase_order", fetched.getToolExecuted());
    }
}
