package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.CitationEvidence;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.InventoryItem;
import com.aiops.domain.Product;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class InventoryTools {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;

    public InventoryTools(InventoryItemRepository inventoryItemRepository,
                          ProductRepository productRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
    }

    public AgentTool createGetInventoryTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_inventory"; }
            @Override public String getDescription() { return "Get all inventory items or filter by critical stockout risk."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "inventory.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                Boolean criticalOnly = (Boolean) input.get("criticalOnly");

                List<InventoryItem> items = (criticalOnly != null && criticalOnly)
                        ? inventoryItemRepository.findByTenantIdAndStockoutRiskIn(tenantId, List.of("HIGH", "CRITICAL"))
                        : inventoryItemRepository.findByTenantId(tenantId);

                for (InventoryItem item : items) {
                    if ("HIGH".equals(item.getStockoutRisk()) || "CRITICAL".equals(item.getStockoutRisk())) {
                        String daysLeftStr = item.getDaysOfStockRemaining() != null
                                ? item.getDaysOfStockRemaining().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                                : "0.00";
                        ctx.addEvidence(new CitationEvidence("INVENTORY", item.getId(),
                                "Product Stockout Risk: " + item.getProductName(),
                                "Available: " + item.getAvailableStock() + " units (" + daysLeftStr + " days remaining). Warehouse: " + item.getWarehouseName(),
                                0.96, "/inventory"));
                    }
                }

                return new ToolExecutionResult("get_inventory", true, items,
                        "Found " + items.size() + " inventory items", null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createGetProductInventoryTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_product_inventory"; }
            @Override public String getDescription() { return "Get stock details for a specific product name or SKU across all warehouses."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "inventory.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String query = (String) input.get("productQuery");

                List<Product> products = productRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, query);
                if (products.isEmpty()) {
                    return new ToolExecutionResult("get_product_inventory", false, null, null,
                            "Product matching '" + query + "' not found", System.currentTimeMillis() - start);
                }

                Product prod = products.get(0);
                List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductId(tenantId, prod.getId());
                int totalCurrent = items.stream().mapToInt(InventoryItem::getCurrentStock).sum();
                int totalReserved = items.stream().mapToInt(InventoryItem::getReservedStock).sum();
                int totalAvailable = items.stream().mapToInt(InventoryItem::getAvailableStock).sum();
                int totalIncoming = items.stream().mapToInt(InventoryItem::getIncomingStock).sum();

                Map<String, Object> details = new LinkedHashMap<>();
                details.put("product", prod);
                details.put("totalCurrentStock", totalCurrent);
                details.put("totalReservedStock", totalReserved);
                details.put("totalAvailableStock", totalAvailable);
                details.put("totalIncomingStock", totalIncoming);
                details.put("warehouseBreakdown", items);

                ctx.addEvidence(new CitationEvidence("INVENTORY", prod.getId(),
                        "Inventory Record: " + prod.getName() + " (" + prod.getSku() + ")",
                        "Current Stock: " + totalCurrent + " " + prod.getUnit() + " | Available: " + totalAvailable + " | Reserved: " + totalReserved + " | Incoming: " + totalIncoming,
                        0.98, "/inventory"));

                return new ToolExecutionResult("get_product_inventory", true, details,
                        "Retrieved inventory for " + prod.getName(), null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createCalculateReorderQuantityTool() {
        return new AgentTool() {
            @Override public String getName() { return "calculate_reorder_quantity"; }
            @Override public String getDescription() { return "Calculate optimal reorder quantity based on lead time, average daily demand, and safety stock."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "inventory.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                String productId = (String) input.get("productId");

                Optional<Product> prodOpt = productRepository.findByTenantIdAndId(tenantId, productId);
                if (prodOpt.isEmpty()) {
                    return new ToolExecutionResult("calculate_reorder_quantity", false, null, null,
                            "Product " + productId + " not found", System.currentTimeMillis() - start);
                }

                Product prod = prodOpt.get();
                List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductId(tenantId, prod.getId());
                int available = items.stream().mapToInt(InventoryItem::getAvailableStock).sum();

                // Formula: Reorder Quantity = (Daily Demand * (Lead Time + Review Period)) + Safety Stock - Current Available Stock
                int leadTime = prod.getLeadTimeDays();
                BigDecimal dailyDemand = prod.getAvgDailyDemand() != null ? prod.getAvgDailyDemand() : BigDecimal.ZERO;
                int safetyStock = prod.getSafetyStock();
                int reviewPeriodDays = 14;

                BigDecimal totalDemandCover = dailyDemand.multiply(BigDecimal.valueOf(leadTime + reviewPeriodDays));
                BigDecimal requiredBeforeCap = totalDemandCover.add(BigDecimal.valueOf(safetyStock)).subtract(BigDecimal.valueOf(available));
                int requiredQuantity = Math.max(0, requiredBeforeCap.setScale(0, java.math.RoundingMode.CEILING).intValue());

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("productId", prod.getId());
                result.put("productName", prod.getName());
                result.put("sku", prod.getSku());
                result.put("availableStock", available);
                result.put("leadTimeDays", leadTime);
                result.put("dailyDemand", dailyDemand);
                result.put("safetyStock", safetyStock);
                result.put("recommendedReorderQuantity", requiredQuantity);
                result.put("formulaExplanation", "Reorder Qty = (Daily Demand " + dailyDemand + " * (Lead Time " + leadTime + " + Review Period " + reviewPeriodDays + ")) + Safety Stock " + safetyStock + " - Available Stock " + available);

                return new ToolExecutionResult("calculate_reorder_quantity", true, result,
                        "Calculated reorder quantity of " + requiredQuantity + " " + prod.getUnit() + " for " + prod.getName(), null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
