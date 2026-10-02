package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.CitationEvidence;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.InventoryItem;
import com.aiops.domain.PurchaseOrder;
import com.aiops.domain.SalesOrder;
import com.aiops.domain.Supplier;
import com.aiops.domain.enums.OrderStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.PurchaseOrderRepository;
import com.aiops.repository.SalesOrderRepository;
import com.aiops.repository.SupplierRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OrderTools {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    public OrderTools(SalesOrderRepository salesOrderRepository,
                      InventoryItemRepository inventoryItemRepository,
                      PurchaseOrderRepository purchaseOrderRepository,
                      SupplierRepository supplierRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
    }

    public AgentTool createGetOrdersTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_orders"; }
            @Override public String getDescription() { return "Retrieve sales orders with optional status or delivery risk filters."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "orders.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String riskFilter = (String) input.get("deliveryRisk");

                List<SalesOrder> orders = (riskFilter != null && !riskFilter.isBlank())
                        ? salesOrderRepository.findByTenantIdAndDeliveryRisk(tenantId, riskFilter)
                        : salesOrderRepository.findByTenantId(tenantId);

                for (SalesOrder order : orders) {
                    if ("HIGH".equalsIgnoreCase(order.getDeliveryRisk()) || "CRITICAL".equalsIgnoreCase(order.getDeliveryRisk())) {
                        ctx.addEvidence(new CitationEvidence("ORDER", order.getId(),
                                "Order #" + order.getOrderNumber(),
                                "Customer: " + order.getCustomerName() + " | Delivery Risk: " + order.getDeliveryRisk() + " | Promised: " + order.getPromisedDeliveryDate(),
                                0.95, "/orders/" + order.getOrderNumber()));
                    }
                }

                return new ToolExecutionResult("get_orders", true, orders,
                        "Retrieved " + orders.size() + " orders for tenant " + tenantId, null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createInvestigateOrderDelayTool() {
        return new AgentTool() {
            @Override public String getName() { return "investigate_order_delay"; }
            @Override public String getDescription() { return "Investigates root cause of an order delay across items, inventory, purchase orders, and supplier reliability."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "orders.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String orderNumber = (String) input.get("orderNumber");

                Optional<SalesOrder> orderOpt = salesOrderRepository.findByTenantIdAndOrderNumber(tenantId, orderNumber);
                if (orderOpt.isEmpty()) {
                    return new ToolExecutionResult("investigate_order_delay", false, null, null,
                            "Order " + orderNumber + " not found", System.currentTimeMillis() - start);
                }

                SalesOrder order = orderOpt.get();
                Map<String, Object> investigation = new LinkedHashMap<>();
                investigation.put("order", order);

                ctx.addEvidence(new CitationEvidence("ORDER", order.getId(),
                        "Order #" + order.getOrderNumber(),
                        "Promised Delivery: " + order.getPromisedDeliveryDate() + ", Delay: " + order.getDelayDays() + " days. Status: " + order.getStatus(),
                        0.98, "/orders/" + order.getOrderNumber()));

                // Step 2: Check Inventory for order items
                List<Map<String, Object>> itemStockAnalysis = new ArrayList<>();
                order.getItems().forEach(item -> {
                    List<InventoryItem> invList = inventoryItemRepository.findByTenantIdAndProductId(tenantId, item.getProductId());
                    int available = invList.stream().mapToInt(InventoryItem::getAvailableStock).sum();
                    Map<String, Object> m = new HashMap<>();
                    m.put("productName", item.getProductName());
                    m.put("sku", item.getSku());
                    m.put("orderedQuantity", item.getQuantity());
                    m.put("availableStock", available);
                    m.put("isDeficit", available < item.getQuantity());
                    itemStockAnalysis.add(m);

                    if (available < item.getQuantity()) {
                        ctx.addEvidence(new CitationEvidence("INVENTORY", item.getProductId(),
                                "Inventory: " + item.getProductName(),
                                "Stock Deficit: Ordered " + item.getQuantity() + " units, but available stock is only " + available + " units.",
                                0.96, "/inventory"));
                    }
                });
                investigation.put("stockAnalysis", itemStockAnalysis);

                // Step 3: Find relevant Purchase Orders
                List<PurchaseOrder> delayedPOs = purchaseOrderRepository.findByTenantIdAndStatus(tenantId, OrderStatus.DELAYED);
                if (!delayedPOs.isEmpty()) {
                    PurchaseOrder po = delayedPOs.get(0);
                    investigation.put("delayedPO", po);

                    ctx.addEvidence(new CitationEvidence("PURCHASE_ORDER", po.getId(),
                            "Purchase Order #" + po.getPoNumber(),
                            "Supplier: " + po.getSupplierName() + " | Expected: " + po.getExpectedDeliveryDate() + " | Overdue by " + po.getDelayDays() + " days.",
                            0.94, "/purchase-orders/" + po.getPoNumber()));

                    // Check supplier reliability
                    supplierRepository.findByTenantIdAndId(tenantId, po.getSupplierId()).ifPresent(sup -> {
                        investigation.put("supplierRecord", sup);
                        ctx.addEvidence(new CitationEvidence("SUPPLIER", sup.getId(),
                                "Supplier: " + sup.getName(),
                                "On-Time Rate: " + sup.getOnTimeDeliveryRate() + "% | Lead Time: " + sup.getLeadTimeDays() + " days | Reliability Score: " + sup.getReliabilityScore() + "/5.0",
                                0.92, "/suppliers/" + sup.getId()));
                    });
                }

                return new ToolExecutionResult("investigate_order_delay", true, investigation,
                        "Completed multi-entity root cause investigation for order " + orderNumber, null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
