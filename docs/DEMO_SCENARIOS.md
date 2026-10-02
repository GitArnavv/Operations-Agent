# Walkthrough Guide: 10 Operational Demo Scenarios

This guide details how to reproduce, test, and demonstrate all 10 core operational scenarios supported by **AIOps India** for **Sharma Electricals Pvt. Ltd.**.

---

## Scenario 1: Operational Attention Center (Morning Briefing)

### Prompt
> *"What needs my attention today?"*

### Execution Flow
1. **Specialist Agent**: `DataAgent`
2. **Tools Executed**: `analytics.getAttentionSummary`
3. **Agent Action**: Scans all active orders, warehouse inventory levels, and unpaid customer invoices.
4. **Expected Output**:
   - **Delayed Orders**: Order `#ORD-1042` (Sharma Electronics, ₹1.45 Lakh) is past its promised delivery date.
   - **Low Stock Risk**: 3 fast-moving SKUs have < 7 days of supply remaining (e.g., Havells 32A MCB).
   - **Overdue Receivables**: 2 invoices totaling ₹4.79 Lakh are past due > 30 days (ABC Traders & Sunrise Infra).
5. **Citations Attached**: Direct links to Order `#ORD-1042`, Product `PRD-102`, and Invoices `INV-2024-001` & `INV-2024-002`.

---

## Scenario 2: Multi-Hop Order Delay Investigation

### Prompt
> *"Why is Sharma Electronics' order delayed?"*

### Execution Flow
1. **Specialist Agent**: `OrderAgent`
2. **Tools Executed**: `order.investigateDelay`, `inventory.checkStock`, `supplier.getOpenPurchaseOrders`
3. **Investigation Logic**:
   - Order `#ORD-1042` placed by Sharma Electronics contains 50 coils of **Polycab FR-LSH 2.5mm² Copper Wire**.
   - Available inventory in Bhiwandi Central Warehouse is **0 coils** (allocated: 50, physical: 0).
   - Upstream Purchase Order `#PO-2381` was issued to **Polycab Wires Ltd** with expected delivery on Sept 28, 2026.
   - Polycab delayed dispatch to **Oct 4, 2026** due to raw material copper coil transit delays from their Halol plant.
4. **Expected Output**:
   - Full diagnostic chain presented with root cause identified as upstream supplier delay.
   - Suggested remediation: Alert customer with revised delivery date of Oct 6, or rebalance 20 coils from Pune Hadapsar warehouse.
5. **Citations Attached**: `ORD-1042` (Sales Order), `PRD-101` (Inventory), `PO-2381` (Purchase Order).

---

## Scenario 3: Inventory Days-of-Supply Forecast

### Prompt
> *"Which products are likely to run out this week?"*

### Execution Flow
1. **Specialist Agent**: `InventoryAgent`
2. **Tools Executed**: `inventory.getLowStockForecast`
3. **Forecasting Logic**:
   - Computes average daily consumption rate over the trailing 30 days.
   - Divides current available stock by daily burn rate to determine **Days of Cover (DoC)**.
4. **Expected Output**:
   - **Havells 32A Double Pole MCB**: 18 units in stock, burn rate 4.2 units/day &rarr; **4.2 days of supply**.
   - **Philips Smart LED 18W Panel**: 32 units in stock, burn rate 5.8 units/day &rarr; **5.5 days of supply**.
   - **Polycab 2.5mm² FR-LSH Red**: 0 units available &rarr; **0 days of supply (Stockout)**.
5. **Action Proposed**: Automated reorder recommendations with suggested quantities.

---

## Scenario 4: Supplier Reliability & SLA Benchmarking

### Prompt
> *"Which supplier has the worst on-time delivery rate?"*

### Execution Flow
1. **Specialist Agent**: `SupplierAgent`
2. **Tools Executed**: `supplier.benchmarkReliability`
3. **Benchmarking Metrics**:
   - **Polycab Wires Ltd**: 68.4% On-Time Delivery Rate | Avg Delay: 4.2 Days | Defect Rate: 0.8%
   - **Havells India Ltd**: 88.5% On-Time Delivery Rate | Avg Delay: 1.1 Days | Defect Rate: 0.2%
   - **Schneider Electric India**: 96.2% On-Time Delivery Rate | Avg Delay: 0.4 Days | Defect Rate: 0.1%
4. **Expected Output**:
   - Identifies Polycab Wires Ltd as the lowest-performing critical supplier.
   - Highlights ₹4.8 Lakh of delayed shipments over the last quarter.

---

## Scenario 5: Overdue Invoices & Cash Flow Protection

### Prompt
> *"Show me all overdue invoices above ₹1 lakh"*

### Execution Flow
1. **Specialist Agent**: `InvoiceAgent`
2. **Tools Executed**: `invoice.getOverdueInvoices(minAmount=100000)`
3. **Expected Output**:
   - **ABC Traders**: Invoice `INV-2024-001` | Balance: **₹2,84,000** | Overdue by: **38 Days** | Credit Limit: ₹5,00,000
   - **Sunrise Infra**: Invoice `INV-2024-002` | Balance: **₹1,95,000** | Overdue by: **45 Days** | Credit Limit: ₹3,00,000
4. **Action Proposed**: One-click generation of professional GST-compliant payment reminder letter and WhatsApp template with UPI/RTGS details.

---

## Scenario 6: Document OCR & Metadata Extraction

### Walkthrough
1. Navigate to **Document Vault (OCR)** (`/documents`).
2. Click **"Upload Document"** and select any invoice PDF or lorry receipt (demo documents pre-loaded: `LR-BHW-8921.pdf` and `INV-POL-2026-904.pdf`).
3. Agent extracts:
   - **Supplier GSTIN**: `27AAACP0123M1ZQ`
   - **Invoice Date & Number**: `POL-98124`, dated Sept 24, 2026
   - **Taxable Value**: ₹1,22,881.36
   - **CGST (9%)**: ₹11,059.32
   - **SGST (9%)**: ₹11,059.32
   - **Total Invoice Value**: ₹1,45,000.00
   - **HSN Codes**: `8544` (Insulated Wire/Cables)
4. Displays confidence score (98.4%) and automatically cross-checks against PO `#PO-2381`.

---

## Scenario 7: Autonomous Monitoring Rules

### Walkthrough
1. Navigate to **Monitoring Rules** (`/monitoring`).
2. Review active operational rules:
   - *Rule 1*: "Alert when any Fast-Moving SKU has < 5 days of inventory cover"
   - *Rule 2*: "Alert when customer outstanding exceeds 80% of credit limit"
   - *Rule 3*: "Alert when vendor PO dispatch is delayed by > 48 hours"
3. Click **"Test Rule Evaluation"**: The agent scans live inventory and triggers an instant in-app alert for Havells 32A MCBs.

---

## Scenario 8: Action Proposal & Risk Assessment

### Prompt
> *"Create a purchase order for 200 units of Havells 32A MCBs"*

### Execution Flow
1. **Specialist Agent**: `ActionAgent`
2. **Tools Executed**: `action.proposePurchaseOrder`
3. **Risk Analysis**:
   - Estimated Cost: 200 units &times; ₹420/unit = **₹84,000 + 18% GST (₹15,120) = ₹99,120**.
   - Risk Level: `HIGH_RISK` (Financial procurement action).
   - Auto-Execution: **Blocked**.
4. **Proposal Generated**: Creates `ApprovalRequest #APR-7721` with supplier Havells India Ltd, itemized costs, and justification.

---

## Scenario 9: Human-in-the-Loop Approval & Execution

### Walkthrough
1. Navigate to **Pending Approvals** (`/approvals`).
2. Notice the pending card for **Purchase Order Proposal for Havells India Ltd (₹99,120)**.
3. Review line items, warehouse destination (Bhiwandi), and risk level badge (`HIGH_RISK`).
4. Click **"Approve & Execute"**.
5. The system:
   - Verifies the user's role (`OPERATIONS_MANAGER` or `ADMIN`).
   - Generates official Purchase Order in database.
   - Updates projected stock cover.
   - Appends an entry into the immutable **Audit Trail** (`/audit-logs`).

---

## Scenario 10: Voice-First Operations (Hindi & English)

### Walkthrough
1. Navigate to **AI Assistant** (`/agent`).
2. Click the microphone icon or select language **"Hindi (हिन्दी)"**.
3. Speak clearly:
   > *"Sharma Electronics ka order delay kyu hai?"*
4. Web Speech STT transcribes the audio into Hindi text.
5. The agent parses the intent, executes `order.investigateDelay`, and streams back:
   > *"Sharma Electronics ka order #ORD-1042 delay hai kyunki Polycab 2.5mm² wire ka stock Bhiwandi warehouse me khatam ho chuka hai. Vendor Polycab Wires Ltd ne delivery 4 October tak extend ki hai."*
6. Audio synthesis plays back the summary naturally using Web Speech TTS.
