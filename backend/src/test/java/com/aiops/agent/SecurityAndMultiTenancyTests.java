package com.aiops.agent;

import com.aiops.domain.ActionApproval;
import com.aiops.domain.PurchaseOrder;
import com.aiops.domain.enums.OrderStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.domain.enums.UserRole;
import com.aiops.repository.ActionApprovalRepository;
import com.aiops.repository.PurchaseOrderRepository;
import com.aiops.security.*;
import com.aiops.tenant.HibernateTenantIdentifierResolver;
import com.aiops.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class SecurityAndMultiTenancyTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private HibernateTenantIdentifierResolver tenantIdentifierResolver;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ActionApprovalRepository actionApprovalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String TENANT_SHARMA = "org_sharma_001";
    private static final String TENANT_COMPETITOR = "tenant_competitor_999";

    private String adminToken;
    private String opsToken;
    private String auditorToken;

    @BeforeEach
    void setUp() {
        UserPrincipal adminPrincipal = new UserPrincipal(
                "usr_sharma_admin", "admin@sharmaelectricals.in", "Vikram Admin", "pass", TENANT_SHARMA, UserRole.ADMIN
        );
        UserPrincipal opsPrincipal = new UserPrincipal(
                "usr_sharma_ops", "amit.patel@sharmaelectricals.in", "Amit Ops", "pass", TENANT_SHARMA, UserRole.OPERATIONS_MANAGER
        );
        UserPrincipal auditorPrincipal = new UserPrincipal(
                "usr_sharma_auditor", "auditor@sharmaelectricals.in", "Pooja Auditor", "pass", TENANT_SHARMA, UserRole.AUDITOR
        );

        adminToken = jwtTokenProvider.generateToken(adminPrincipal);
        opsToken = jwtTokenProvider.generateToken(opsPrincipal);
        auditorToken = jwtTokenProvider.generateToken(auditorPrincipal);

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("JWT Validator: Accurately validates valid token and rejects expired/tampered tokens")
    void testJwtValidator_Comprehensive() {
        // 1. Valid token passes validation
        assertThat(jwtTokenValidator.validateToken(adminToken)).isTrue();

        // 2. Tampered token fails validation
        String tamperedToken = adminToken.substring(0, adminToken.length() - 6) + "XXXXXX";
        assertThat(jwtTokenValidator.validateToken(tamperedToken)).isFalse();

        // 3. Null or blank token fails
        assertThat(jwtTokenValidator.validateToken(null)).isFalse();
        assertThat(jwtTokenValidator.validateToken("   ")).isFalse();

        // 4. Expired token fails validation
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("usr_expired")
                .claim("email", "expired@test.com")
                .claim("tenantId", TENANT_SHARMA)
                .claim("role", "ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
                .expiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(key)
                .compact();

        assertThat(jwtTokenValidator.validateToken(expiredToken)).isFalse();

        // 5. Token missing tenantId fails validation
        String missingTenantToken = Jwts.builder()
                .subject("usr_no_tenant")
                .claim("email", "notenant@test.com")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();

        assertThat(jwtTokenValidator.validateToken(missingTenantToken)).isFalse();
    }

    @Test
    @DisplayName("Security 6 & Stateless Auth: Unauthenticated request returns 401 JSON error")
    void testUnauthenticatedAccess_Returns401Json() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("RBAC: Auditor can view audit logs but is forbidden from approving actions")
    void testRbac_AuditorPermissions() throws Exception {
        // Auditor CAN read audit logs (200 OK)
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk());

        // Auditor CANNOT approve actions (403 Forbidden)
        mockMvc.perform(post("/api/v1/action-approvals/TXN-DEMO-TEST/approve")
                        .header("Authorization", "Bearer " + auditorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Auditor attempt to approve\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("RBAC: Operations Manager can evaluate action but cannot execute approval")
    void testRbac_OperationsManagerPermissions() throws Exception {
        // Operations Manager CAN evaluate action (200 OK)
        String evaluatePayload = objectMapper.writeValueAsString(Map.of(
                "actionType", "APPROVE_PURCHASE_ORDER",
                "title", "Order approval review for Polycab",
                "description", "Exceeds 50000 threshold",
                "targetEntityType", "PURCHASE_ORDER",
                "targetEntityId", "PO-2381",
                "estimatedAmount", 65490.00
        ));

        mockMvc.perform(post("/api/v1/action-approvals/evaluate")
                        .header("Authorization", "Bearer " + opsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evaluatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionType").value("APPROVE_PURCHASE_ORDER"));

        // Operations Manager CANNOT approve high-impact actions (requires ROLE_ADMIN -> 403 Forbidden)
        mockMvc.perform(post("/api/v1/action-approvals/TXN-DEMO-TEST/approve")
                        .header("Authorization", "Bearer " + opsToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Ops manager attempting executive approval\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: Admin has full authority to approve pending transactions")
    void testRbac_AdminApprovalAuthority() throws Exception {
        // Create pending action approval
        ActionApproval pending = new ActionApproval(
                "APP-SEC-TEST-01",
                "TXN-SEC-TEST-01",
                TENANT_SHARMA,
                "APPROVE_PURCHASE_ORDER",
                "Approve PO-2381",
                "Purchase order exceeding limit",
                "Exceeds 50000 threshold",
                RiskLevel.HIGH_RISK,
                new BigDecimal("65490.00"),
                "INR",
                "{\"poNumber\":\"PO-2381\"}",
                "PO-2381",
                "PURCHASE_ORDER",
                "usr_sharma_ops",
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        actionApprovalRepository.save(pending);

        // Admin CAN approve (200 OK)
        mockMvc.perform(post("/api/v1/action-approvals/TXN-SEC-TEST-01/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Executive authorized by Admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decidedByUserId").value("usr_sharma_admin"));
    }

    @Test
    @DisplayName("Multi-Tenancy: Hibernate CurrentTenantIdentifierResolver binds TenantContext")
    void testHibernateTenantIdentifierResolver() {
        // Unauthenticated defaults to default tenant
        TenantContext.clear();
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo(TENANT_SHARMA);

        // Explicit tenant set in TenantContext
        TenantContext.setTenantId("org_custom_123");
        assertThat(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).isEqualTo("org_custom_123");
    }

    @Test
    @DisplayName("Cross-Tenant Data Leakage Prevention: AOP aspect rejects repository query with foreign tenantId")
    void testCrossTenantLeakagePrevention_Query() {
        // Authenticate context as TENANT_SHARMA
        TenantContext.setTenantId(TENANT_SHARMA);

        // Querying own tenant works without exception
        purchaseOrderRepository.findByTenantIdAndPoNumber(TENANT_SHARMA, "PO-2381");

        // Attempting to query another tenant's data throws CrossTenantAccessDeniedException
        assertThatThrownBy(() -> purchaseOrderRepository.findByTenantIdAndPoNumber(TENANT_COMPETITOR, "PO-2381"))
                .isInstanceOf(CrossTenantAccessDeniedException.class)
                .hasMessageContaining("Cross-tenant data leakage prevented");
    }

    @Test
    @DisplayName("Cross-Tenant Data Leakage Prevention: AOP aspect blocks saving entity belonging to another tenant")
    void testCrossTenantLeakagePrevention_Mutation() {
        // Authenticate context as TENANT_SHARMA
        TenantContext.setTenantId(TENANT_SHARMA);

        // Attempting to persist entity with TENANT_COMPETITOR throws CrossTenantAccessDeniedException
        PurchaseOrder foreignPo = new PurchaseOrder(
                "po_foreign_99",
                TENANT_COMPETITOR,
                "PO-COMPETITOR-99",
                "SUP-001",
                "Foreign Supplier",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                null,
                OrderStatus.PROCESSING,
                new BigDecimal("10000.00"),
                new BigDecimal("1800.00"),
                new BigDecimal("11800.00"),
                0,
                "Unauthorized entity"
        );

        assertThatThrownBy(() -> purchaseOrderRepository.save(foreignPo))
                .isInstanceOf(CrossTenantAccessDeniedException.class)
                .hasMessageContaining("Cannot persist or update entity belonging to tenant");
    }
}
