package com.aiops.seed;

import com.aiops.domain.*;
import com.aiops.domain.enums.*;
import com.aiops.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final MonitoringRuleRepository monitoringRuleRepository;
    private final AlertRepository alertRepository;
    private final IntegrationRepository integrationRepository;
    private final AuditLogRepository auditLogRepository;
    private final GoodsReceiptNoteRepository goodsReceiptNoteRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(OrganizationRepository organizationRepository,
                          UserRepository userRepository,
                          CustomerRepository customerRepository,
                          SupplierRepository supplierRepository,
                          ProductRepository productRepository,
                          WarehouseRepository warehouseRepository,
                          InventoryItemRepository inventoryItemRepository,
                          SalesOrderRepository salesOrderRepository,
                          PurchaseOrderRepository purchaseOrderRepository,
                          InvoiceRepository invoiceRepository,
                          DocumentRepository documentRepository,
                          ApprovalRequestRepository approvalRequestRepository,
                          MonitoringRuleRepository monitoringRuleRepository,
                          AlertRepository alertRepository,
                          IntegrationRepository integrationRepository,
                          AuditLogRepository auditLogRepository,
                          GoodsReceiptNoteRepository goodsReceiptNoteRepository,
                          PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.documentRepository = documentRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.monitoringRuleRepository = monitoringRuleRepository;
        this.alertRepository = alertRepository;
        this.integrationRepository = integrationRepository;
        this.auditLogRepository = auditLogRepository;
        this.goodsReceiptNoteRepository = goodsReceiptNoteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String tenantId = "org_sharma_001";
        if (organizationRepository.findById(tenantId).isPresent()) {
            return;
        }

        // 1. Organization: Sharma Electricals Pvt. Ltd.
        Organization org = new Organization(
                tenantId, "Sharma Electricals Pvt. Ltd.", "27AABCS1429B1Z2", "AABCS1429B",
                "Maharashtra", "Mumbai", "Electrical Distribution & Industrial Equipment",
                "BUSINESS", "INR", "Gala 4-B, Shree Rajlaxmi Commercial Complex, Kalher, Bhiwandi, Thane 421302"
        );
        organizationRepository.save(org);

        // 2. Users
        String passHash = passwordEncoder.encode("demo123");
        User owner = new User("usr_sharma_owner", tenantId, "rajesh@sharmaelectricals.in", passHash, "Rajesh Sharma", UserRole.OWNER, "+91 98201 12345");
        User admin = new User("usr_sharma_admin", tenantId, "admin@sharmaelectricals.in", passHash, "Vikram Malhotra (Admin)", UserRole.ADMIN, "+91 98200 11111");
        User ops = new User("usr_sharma_ops", tenantId, "amit.patel@sharmaelectricals.in", passHash, "Amit Patel", UserRole.OPERATIONS_MANAGER, "+91 98202 23456");
        User auditor = new User("usr_sharma_auditor", tenantId, "auditor@sharmaelectricals.in", passHash, "Pooja Mehta (Statutory Auditor)", UserRole.AUDITOR, "+91 98204 44444");
        User finance = new User("usr_sharma_fin", tenantId, "priya.deshmukh@sharmaelectricals.in", passHash, "Priya Deshmukh", UserRole.FINANCE_MANAGER, "+91 98203 34567");
        userRepository.saveAll(List.of(owner, admin, ops, auditor, finance));

        // 3. Warehouses
        Warehouse wh1 = new Warehouse("wh_bhiwandi", tenantId, "Bhiwandi Central Logistics Hub", "WH-BHIWANDI", "Bhiwandi", "Maharashtra", "Bldg A2, Kalher Logistics Park", 100000);
        Warehouse wh2 = new Warehouse("wh_pune", tenantId, "Pune Chakan Fulfillment Center", "WH-PUNE", "Pune", "Maharashtra", "MIDC Phase 2, Chakan", 60000);
        warehouseRepository.saveAll(List.of(wh1, wh2));

        // 4. 50+ Suppliers
        List<Supplier> suppliers = new ArrayList<>();
        String[] supplierNames = {
                "Polycab India Ltd.", "Havells India Ltd.", "RR Kabel Ltd.", "Bajaj Electricals Ltd.",
                "Anchor Electricals (Panasonic)", "Finolex Cables Ltd.", "Schneider Electric India",
                "Legrand India Pvt. Ltd.", "Crompton Greaves Consumer Electricals", "L&T Electrical & Automation",
                "Syska LED Lights", "V-Guard Industries Ltd.", "Orient Electric Ltd.", "Goldmedal Electricals",
                "Wipro Consumer Care and Lighting", "HPL Electric & Power Ltd.", "KEI Industries Ltd.",
                "Philips Lighting India", "Siemens India Ltd.", "ABB India Ltd.", "Honeywell Electrical",
                "Microtek International", "Luminous Power Technologies", "Exide Industries Ltd.",
                "Eaton Power Quality", "C&S Electric Ltd.", "Salzer Electronics", "Anchor Switches Ltd.",
                "GM Modular Pvt. Ltd.", "GreatWhite Global Pvt. Ltd.", "Cona Electricals",
                "Prestige Cables Ltd.", "Universal Cables Ltd.", "Paramount Communications",
                "Finolex J-Power Systems", "Anchor Wires Ltd.", "Bonton Cables", "Gemini Wires & Cables",
                "Veto Switchgears Ltd.", "Havells Sylvania", "Indo Asian Fusegear", "Standard Electricals",
                "Standard Copper & Metals", "Maharashtra Electrical Distributors", "Apex Switchgears",
                "Western India Power Equipments", "Sterling Power GenSys", "Balaji Cables & Conduits",
                "Rupal Electricals", "Shree Ganesh Electrical Wholesale", "Arihant Electric Trading",
                "Kalyan Industrial Supplies"
        };

        for (int i = 0; i < supplierNames.length; i++) {
            String supId = "SUP-" + String.format("%03d", i + 1);
            if (i == 0) supId = "SUP-POLYCAB";
            if (i == 1) supId = "SUP-HAVELLS";
            if (i == 2) supId = "SUP-RRKABEL";

            int leadTime = (i % 4 == 0) ? 3 : (i % 4 == 1) ? 5 : (i % 4 == 2) ? 6 : 7;
            BigDecimal onTime = BigDecimal.valueOf(80.0 + (i % 19)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal defect = BigDecimal.valueOf(0.5 + (i % 4) * 0.4).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal rating = BigDecimal.valueOf(Math.min(5.0, 4.0 + (i % 10) * 0.1)).setScale(2, java.math.RoundingMode.HALF_UP);
            String terms = (i % 2 == 0) ? "Net 30" : "Net 45";

            suppliers.add(new Supplier(
                    supId, tenantId, supplierNames[i], "27AAACP" + (8000 + i) + "J1Z" + (i % 9),
                    (i % 3 == 0) ? "Mumbai" : (i % 3 == 1) ? "Pune" : "Nashik",
                    "Maharashtra", "Manager " + (i + 1), "+91 98190 " + String.format("%05d", 10000 + i),
                    "sales@" + supplierNames[i].toLowerCase().replaceAll("[^a-z]", "") + ".com",
                    leadTime, onTime, defect, rating, terms
            ));
        }
        supplierRepository.saveAll(suppliers);

        // 5. 200+ Customers
        List<Customer> customers = new ArrayList<>();
        String[] customerNames = {
                "Sharma Electronics", "ABC Traders", "Apex Infrastructures", "Shree Ram Electric Works",
                "Metropolis Builders & Developers", "Mahalaxmi Electrical Contractors", "Sunshine Realty Projects",
                "Kiran Power Systems", "Om Sai Enterprise", "Balaji Industrial Works", "Navkar Commercial Hub",
                "Pooja Electricals & Hardware", "Surat Diamond Park Contractors", "Ahmedabad Metro Electric Sub",
                "Thane Smart City Projects", "Kalyan Housing Corporation", "Godrej Properties Subcontractor",
                "L&T Construction Site 4", "Tata Housing Project 12", "Hiranandani Estate Maintenance"
        };

        for (int i = 0; i < 200; i++) {
            String custId = "CUST-" + String.format("%04d", i + 1);
            String name = (i < customerNames.length) ? customerNames[i] : "Contractor & Associates " + (i + 1);
            String city = (i % 5 == 0) ? "Mumbai" : (i % 5 == 1) ? "Pune" : (i % 5 == 2) ? "Thane" : (i % 5 == 3) ? "Nashik" : "Nagpur";
            BigDecimal credit = BigDecimal.valueOf(500000 + (i * 25000L));
            BigDecimal outstanding = (i == 1) ? BigDecimal.valueOf(284500.0) : BigDecimal.valueOf((i % 8) * 45000L);

            customers.add(new Customer(
                    custId, tenantId, name, "27AABCT" + (5000 + i) + "K1Z" + (i % 9),
                    city, "Maharashtra", "Proprietor " + (i + 1),
                    "+91 98200 " + String.format("%05d", 20000 + i),
                    "contact@" + name.toLowerCase().replaceAll("[^a-z]", "") + ".in",
                    credit, outstanding
            ));
        }
        customerRepository.saveAll(customers);

        // 6. 500+ Products & Inventory Items
        List<Product> products = new ArrayList<>();
        List<InventoryItem> inventoryItems = new ArrayList<>();

        String[] categories = {
                "Wires & Cables", "Modular Switches & Sockets", "Circuit Breakers & Switchgear",
                "Commercial LED Lighting", "PVC Conduits & Accessories", "Industrial Distribution Boards",
                "Fans & Ventilation", "Motor Starters & Relays"
        };
        String[] hsnCodes = {"8544", "8536", "8538", "9405", "3917", "8537", "8414", "8504"};

        // Critical Demo Product X
        // Critical Demo Product X
        Product prodX = new Product(
                "PROD-WIR-001", tenantId, "POL-CU-15-RED", "1.5 sq mm Copper Wire Red 90m",
                "Wires & Cables", "8544", "COIL", BigDecimal.valueOf(370.0), BigDecimal.valueOf(0.18),
                50, 30, 5, BigDecimal.valueOf(35.0), "Flame retardant electrolytic copper building wire"
        );
        products.add(prodX);

        InventoryItem invX1 = new InventoryItem(
                "inv_bhi_001", tenantId, prodX.getId(), prodX.getName(), prodX.getSku(),
                wh1.getId(), wh1.getName(), 120, 108, 150, BigDecimal.valueOf(3.4), "HIGH"
        );
        inventoryItems.add(invX1);

        for (int i = 2; i <= 500; i++) {
            int catIdx = i % categories.length;
            String cat = categories[catIdx];
            String hsn = hsnCodes[catIdx];
            String sku = "SKU-" + cat.substring(0, 3).toUpperCase() + "-" + String.format("%04d", i);
            String name = cat + " Model " + (i * 7 % 100) + " Specification " + i;
            BigDecimal price = BigDecimal.valueOf(120.0 + (i * 15.0));

            int current = 30 + (i * 17 % 400);
            int reserved = (int)(current * 0.2);
            int incoming = (i % 3 == 0) ? 50 : 0;
            BigDecimal daysLeft = BigDecimal.valueOf((i == 2) ? 4.1 : (i == 3) ? 5.2 : 12.0 + (i % 25)).setScale(2, java.math.RoundingMode.HALF_UP);
            String risk = (daysLeft.compareTo(new BigDecimal("4.5")) <= 0) ? "HIGH" : (daysLeft.compareTo(new BigDecimal("7.0")) <= 0) ? "MEDIUM" : "LOW";

            Product p = new Product(
                    "PROD-" + String.format("%04d", i), tenantId, sku, name, cat, hsn,
                    "PCS", price, BigDecimal.valueOf(0.18), 40, 20, 5, BigDecimal.valueOf(12.0), "Industrial grade " + name
            );
            products.add(p);

            InventoryItem inv = new InventoryItem(
                    "inv_item_" + i, tenantId, p.getId(), p.getName(), p.getSku(),
                    (i % 2 == 0) ? wh1.getId() : wh2.getId(),
                    (i % 2 == 0) ? wh1.getName() : wh2.getName(),
                    current, reserved, incoming, daysLeft, risk
            );
            inventoryItems.add(inv);
        }
        productRepository.saveAll(products);
        inventoryItemRepository.saveAll(inventoryItems);

        // 7. Orders (including critical delayed Order #ORD-1042)
        List<SalesOrder> orders = new ArrayList<>();

        SalesOrder ord1042 = new SalesOrder(
                "ord_1042", tenantId, "ORD-1042", "CUST-0001", "Sharma Electronics",
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                OrderStatus.DELAYED, PaymentStatus.PARTIAL,
                BigDecimal.valueOf(42500.0), BigDecimal.valueOf(7650.0), BigDecimal.valueOf(50150.0),
                "HIGH", 3, "Upstream component deficit: 1.5 sq mm copper wire out of available stock in Bhiwandi; Supplier Polycab PO-2381 delayed 2 days."
        );
        SalesOrderItem item1042 = new SalesOrderItem(
                "item_1042_1", prodX.getId(), prodX.getName(), prodX.getSku(),
                50, BigDecimal.valueOf(370.0), BigDecimal.valueOf(0.18), BigDecimal.valueOf(18500.0)
        );
        ord1042.addItem(item1042);
        orders.add(ord1042);

        SalesOrder ord1043 = new SalesOrder(
                "ord_1043", tenantId, "ORD-1043", "CUST-0002", "ABC Traders",
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                OrderStatus.PROCESSING, PaymentStatus.PAID,
                BigDecimal.valueOf(18000.0), BigDecimal.valueOf(3240.0), BigDecimal.valueOf(21240.0),
                "HIGH", 1, "Truck transit dispatch delay on Mumbai-Nashik highway"
        );
        orders.add(ord1043);

        // Generate 100+ additional orders
        for (int i = 44; i <= 150; i++) {
            String ordNum = "ORD-" + (1000 + i);
            Customer c = customers.get(i % customers.size());
            OrderStatus st = (i % 6 == 0) ? OrderStatus.SHIPPED : (i % 6 == 1) ? OrderStatus.DELIVERED : OrderStatus.PROCESSING;
            PaymentStatus ps = (i % 3 == 0) ? PaymentStatus.PAID : (i % 3 == 1) ? PaymentStatus.PENDING : PaymentStatus.PARTIAL;

            SalesOrder o = new SalesOrder(
                    "ord_" + (1000 + i), tenantId, ordNum, c.getId(), c.getName(),
                    LocalDate.now().minusDays(i % 20), LocalDate.now().plusDays((i % 7) + 1), LocalDate.now().plusDays((i % 7) + 1),
                    st, ps, BigDecimal.valueOf(15000.0 + (i * 200)), BigDecimal.valueOf(2700.0), BigDecimal.valueOf(17700.0 + (i * 200)),
                    "LOW", 0, "On schedule"
            );
            orders.add(o);
        }
        salesOrderRepository.saveAll(orders);

        // 8. Purchase Orders (including delayed PO-2381)
        PurchaseOrder po2381 = new PurchaseOrder(
                "po_2381", tenantId, "PO-2381", "SUP-POLYCAB", "Polycab India Ltd.",
                LocalDate.now().minusDays(8), LocalDate.now().minusDays(2), null,
                OrderStatus.DELAYED, BigDecimal.valueOf(55500.0), BigDecimal.valueOf(9990.0), BigDecimal.valueOf(65490.0),
                2, "Factory transit delay from Halol Gujarat manufacturing plant"
        );
        po2381.addItem(new PurchaseOrderItem("poi_2381_1", prodX.getId(), prodX.getName(), prodX.getSku(), 150, BigDecimal.valueOf(370.0), BigDecimal.valueOf(0.18), BigDecimal.valueOf(55500.0)));
        purchaseOrderRepository.save(po2381);

        // 8b. Goods Receipt Notes (GRN for PO-2381 intake at Bhiwandi warehouse)
        GoodsReceiptNote grn2381 = new GoodsReceiptNote(
                "grn_2381", tenantId, "GRN-2026-081", "PO-2381", "wh_bhiwandi",
                "Bhiwandi Central Logistics Hub", "SUP-POLYCAB", "Polycab India Ltd.",
                LocalDate.now().minusDays(1), "ACCEPTED", "QA Officer S. Deshmukh",
                "Received 100 coils under partial shipment batch 1. Verified copper conductor standards."
        );
        grn2381.addItem(new GoodsReceiptNoteItem(
                "grni_2381_1", prodX.getId(), prodX.getName(), prodX.getSku(), "8544",
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(100.0), BigDecimal.ZERO, "Coils", "Passed QA"
        ));
        goodsReceiptNoteRepository.save(grn2381);

        // 9. Invoices (including overdue ABC Traders invoice for ₹2,84,500)
        Invoice invABC = new Invoice(
                "inv_abc_104", tenantId, "INV-2026-104", "PO-1980", "CUSTOMER", "CUST-0002", "ABC Traders",
                "27AABCT5001K1Z1", LocalDate.now().minusDays(48), LocalDate.now().minusDays(18),
                BigDecimal.valueOf(241101.69), BigDecimal.valueOf(21699.15), BigDecimal.valueOf(21699.15), BigDecimal.ZERO,
                BigDecimal.valueOf(284500.0), PaymentStatus.OVERDUE, 18, BigDecimal.valueOf(0.98), "VALIDATED",
                "Overdue 18 days beyond Net 30 payment agreement."
        );
        Invoice invApex = new Invoice(
                "inv_apex_098", tenantId, "INV-2026-098", "PO-1922", "CUSTOMER", "CUST-0003", "Apex Infrastructures",
                "27AABCT5002K1Z2", LocalDate.now().minusDays(42), LocalDate.now().minusDays(12),
                BigDecimal.valueOf(165677.97), BigDecimal.valueOf(14911.02), BigDecimal.valueOf(14911.02), BigDecimal.ZERO,
                BigDecimal.valueOf(195500.0), PaymentStatus.OVERDUE, 12, BigDecimal.valueOf(0.97), "VALIDATED",
                "Overdue 12 days."
        );
        invoiceRepository.saveAll(List.of(invABC, invApex));

        // 10. Documents
        Document doc1 = new Document(
                "DOC-INV-001", tenantId, "Polycab Inbound Invoice #INV-8821.pdf", "invoice_polycab_8821.pdf",
                "PDF", 450000, "/documents/polycab_8821.pdf", "EXTRACTED",
                "Extracted Polycab India Ltd Tax Invoice for 150 coils 1.5 sq mm copper wire. CGST 9% + SGST 9%.",
                BigDecimal.valueOf(0.97), "INVOICE", "INV-2026-104"
        );
        documentRepository.save(doc1);

        // 11. Monitoring Rules
        MonitoringRule rule1 = new MonitoringRule(
                "RULE-001", tenantId, "Inventory 7-Day Stockout Watcher",
                "Monitor inventory and tell me if any product is likely to run out within seven days.",
                MetricType.INVENTORY_DAYS_REMAINING, RuleOperator.LESS_THAN, BigDecimal.valueOf(7.0), "DAILY", "ALL_PRODUCTS"
        );
        monitoringRuleRepository.save(rule1);

        // 12. Alerts
        Alert alert1 = new Alert(
                "ALT-001", tenantId, "High Delivery Risk: Order #ORD-1042",
                "Order #ORD-1042 promised to Sharma Electronics is projected to miss delivery deadline due to inventory shortage.",
                AlertSeverity.CRITICAL, "ORDER", "ORD-1042", "ORDER_DELAY_DAYS",
                "Expedite purchase request to alternative supplier Havells India", "/orders/ORD-1042"
        );
        Alert alert2 = new Alert(
                "ALT-002", tenantId, "Stockout Risk: 1.5 sq mm Copper Wire",
                "Only 12 available coils remaining in Bhiwandi (3.4 days consumption coverage remaining).",
                AlertSeverity.HIGH, "PRODUCT", prodX.getId(), "INVENTORY_DAYS_REMAINING",
                "Review reorder calculation and create PO draft", "/inventory"
        );
        alertRepository.saveAll(List.of(alert1, alert2));

        // 13. Integrations (Tally, Zoho Books, IndiaMART, WhatsApp, Shopify, Razorpay)
        List<Integration> integrations = List.of(
                new Integration("INT-TALLY", tenantId, "Tally Prime ERP", "ERP", "CONNECTED", true, Instant.now().minusSeconds(1800), "Active sync: 1,420 vouchers synced", "Port: 9000, Tally XML ODBC Gateway enabled"),
                new Integration("INT-ZOHO", tenantId, "Zoho Books", "ACCOUNTING", "CONNECTED", true, Instant.now().minusSeconds(3600), "Active sync: e-Way bill and GSTIN verified", "OAuth2 Connected to Sharma Electricals org"),
                new Integration("INT-INDIAMART", tenantId, "IndiaMART B2B Marketplace", "MARKETPLACE", "CONNECTED", true, Instant.now().minusSeconds(7200), "34 purchase leads ingested", "Key CRM CRM-IM-9821"),
                new Integration("INT-WHATSAPP", tenantId, "WhatsApp Business API", "MESSAGING", "CONNECTED", true, Instant.now().minusSeconds(600), "Payment reminders active", "Meta Cloud API webhook active"),
                new Integration("INT-SHOPIFY", tenantId, "Shopify Store", "ECOMMERCE", "DISCONNECTED", false, null, "Inactive", "B2B portal pending credentials"),
                new Integration("INT-RAZORPAY", tenantId, "Razorpay Payments", "PAYMENTS", "CONNECTED", true, Instant.now().minusSeconds(900), "Virtual Accounts for NEFT/RTGS enabled", "Smart Collect & QR active")
        );
        integrationRepository.saveAll(integrations);

        // 14. Audit Logs
        AuditLog audit1 = new AuditLog(
                "AUD-SEED-01", tenantId, owner.getId(), owner.getFullName(),
                "TENANT_INITIALIZED", "ORGANIZATION", tenantId, null, "Status: ACTIVE",
                "Initial multi-tenant initialization for Sharma Electricals Pvt. Ltd.", "REQ-BOOTSTRAP", null
        );
        auditLogRepository.save(audit1);
    }
}
