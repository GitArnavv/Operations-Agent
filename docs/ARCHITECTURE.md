# System Architecture & Technical Specifications

## 1. Architectural Philosophy

AIOps India is designed as an **Autonomous Enterprise Nervous System** tailored to the operational realities of Indian mid-market businesses. 

Unlike generic "chat-with-data" wrappers or fragile text-to-SQL utilities, AIOps India adopts a **Tool-Mediated Agentic Architecture** governed by three non-negotiable architectural invariants:

1. **Zero Raw SQL Execution**: The agent never generates or executes unstructured SQL queries. All interactions with enterprise databases occur through audited, strongly typed Java tools.
2. **Strict Multi-Tenant Isolation**: Tenant boundaries are enforced at the transport and thread level via `TenantContext`. No query can access records belonging to another tenant ID.
3. **Risk-Gated State Mutation**: High-risk business actions (reordering, payments, status overrides) require formal Human-in-the-Loop approval workflows before any database change is committed.

---

## 2. Multi-Tenancy & Security Model

```mermaid
sequenceDiagram
    autonumber
    actor User as Operations Manager
    participant Client as React 18 Frontend
    participant Security as JwtAuthenticationFilter
    participant Context as TenantContext (ThreadLocal)
    participant Controller as REST Controller
    participant Service as Business Service
    participant Repo as Spring Data JPA
    participant DB as Multi-Tenant Database

    User->>Client: Send Request with Bearer JWT
    Client->>Security: HTTP Request + Authorization Header
    Security->>Security: Validate JWT Signature & Claims
    Security->>Context: Set TenantContext(tenantId, userId, role)
    Security->>Controller: Forward to Request Handler
    Controller->>Service: Call Business Logic
    Service->>Context: Read Current TenantId
    Service->>Repo: Query with WHERE tenant_id = :tenantId
    Repo->>DB: Execute Tenant-Scoped Query
    DB-->>Repo: Tenant Isolated Dataset
    Repo-->>Service: Domain Entities
    Service-->>Controller: DTO Response
    Controller-->>Client: JSON Response
    Security->>Context: Clear ThreadLocal (finally block)
```

### Key Security Components
- **`TenantContext`**: A thread-safe `ThreadLocal<String>` wrapper that holds the authenticated tenant ID throughout the HTTP request lifecycle. Always cleared in a `finally` block to prevent thread pool contamination.
- **`JwtTokenProvider`**: Signs and parses HS256 JWT tokens containing `tenantId`, `userId`, `role`, and `sub`.
- **`JwtAuthenticationFilter`**: Intercepts all incoming requests to `/api/**`, verifies the JWT token, extracts user details, sets Spring Security's `SecurityContextHolder`, and populates `TenantContext`.
- **`SecurityConfig`**: Permits public access only to `/api/auth/**`, while requiring authentication for all operational, inventory, agent, and reporting endpoints.

---

## 3. Data Model & Entity Relationship

The domain model captures all critical operational facets of Indian distribution businesses:

```mermaid
erDiagram
    ORGANIZATION ||--o{ USER : employs
    ORGANIZATION ||--o{ WAREHOUSE : operates
    ORGANIZATION ||--o{ PRODUCT : catalogs
    ORGANIZATION ||--o{ SUPPLIER : procures_from
    ORGANIZATION ||--o{ CUSTOMER : sells_to
    ORGANIZATION ||--o{ SALES_ORDER : receives
    ORGANIZATION ||--o{ PURCHASE_ORDER : issues
    ORGANIZATION ||--o{ INVOICE : generates
    ORGANIZATION ||--o{ APPROVAL_REQUEST : authorizes
    ORGANIZATION ||--o{ MONITORING_RULE : configures
    ORGANIZATION ||--o{ AUDIT_LOG : tracks

    SALES_ORDER ||--|{ SALES_ORDER_ITEM : contains
    PURCHASE_ORDER ||--|{ PURCHASE_ORDER_ITEM : contains
    INVOICE ||--|{ INVOICE_ITEM : contains
    PRODUCT ||--o{ INVENTORY_ITEM : stocked_as
    WAREHOUSE ||--o{ INVENTORY_ITEM : houses
    CUSTOMER ||--o{ SALES_ORDER : places
    SUPPLIER ||--o{ PURCHASE_ORDER : fulfills
```

### Core Entities
- **`Organization`**: Tenant root entity holding GSTIN, PAN, registered state, and industry tier.
- **`SalesOrder` & `SalesOrderItem`**: Tracks dealer orders, delivery addresses, dispatch statuses, and estimated delivery dates.
- **`PurchaseOrder` & `PurchaseOrderItem`**: Tracks vendor procurement, promised delivery dates, and receiving status.
- **`InventoryItem`**: Real-time stock levels, allocated units, available units, and safety reorder thresholds per warehouse.
- **`Invoice`**: GST tax invoices with taxable value, CGST, SGST, IGST, payment due dates, and payment status (`PAID`, `PARTIAL`, `OVERDUE`).
- **`ApprovalRequest`**: Formal proposal object containing action type, risk level, payload diff, requested by, approved by, and execution status.
- **`AuditLog`**: Immutable ledger recording timestamp, actor, tenant ID, action, target entity, and IP address.

---

## 4. Dual-Speed Agent Architecture

To provide an instantaneous conversational experience while protecting enterprise state, AIOps India implements a **Dual-Speed Pattern**:

1. **Fast-Path (Read & Diagnostics)**:
   - Tool calls with `RiskLevel.READ_ONLY` execute immediately in the request thread.
   - Summaries, correlations, and multi-hop investigations complete in < 250ms.
   - Evidence is attached to responses as structured citations (`CitationEvidence`).

2. **Gated-Path (Actions & Mutations)**:
   - Tool calls with `RiskLevel.HIGH_RISK` or `RiskLevel.MEDIUM_RISK` generate an `AgentActionProposal`.
   - If user confirmation is required, an `ApprovalRequest` is persisted in the database.
   - Execution is deferred until an authorized user approves the proposal via UI or authenticated API.

---

## 5. Deployment Topology

AIOps India supports cloud-native deployment across single-node Docker Compose or multi-node Kubernetes clusters:

```
[ Ingress Controller (Nginx) ]
        |
        +---> [ / (Frontend SPA) - Nginx / React 18 Pods ]
        |
        +---> [ /api/* & /ws - Spring Boot Backend Pods ]
                    |
                    +---> [ PostgreSQL 16 (StatefulSet) ]
                    |
                    +---> [ Redis 7 Cache & Session Pod ]
```
