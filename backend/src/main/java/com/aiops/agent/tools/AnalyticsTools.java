package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.CitationEvidence;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.InventoryItem;
import com.aiops.domain.Invoice;
import com.aiops.domain.SalesOrder;
import com.aiops.domain.enums.PaymentStatus;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.InvoiceRepository;
import com.aiops.repository.SalesOrderRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class AnalyticsTools {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InvoiceRepository invoiceRepository;

    public AnalyticsTools(SalesOrderRepository salesOrderRepository,
                          InventoryItemRepository inventoryItemRepository,
                          InvoiceRepository invoiceRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public AgentTool createGetOperationalKpisTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_operational_kpis"; }
            @Override public String getDescription() { return "Calculates operational health KPIs: delayed orders, stockout risks, overdue receivables, and attention exceptions."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "analytics.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();

                List<SalesOrder> delayedOrders = salesOrderRepository.findByTenantIdAndDeliveryRisk(tenantId, "HIGH");
                List<InventoryItem> criticalStock = inventoryItemRepository.findByTenantIdAndStockoutRiskIn(tenantId, List.of("HIGH", "CRITICAL"));
                List<Invoice> overdueInvoices = invoiceRepository.findByTenantIdAndPaymentStatus(tenantId, PaymentStatus.OVERDUE);

                BigDecimal totalOverdueInr = overdueInvoices.stream()
                        .map(Invoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal overdueLakhs = totalOverdueInr.divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP);

                Map<String, Object> kpis = new LinkedHashMap<>();
                kpis.put("delayedOrdersCount", delayedOrders.size());
                kpis.put("stockoutRisksCount", criticalStock.size());
                kpis.put("overdueInvoicesCount", overdueInvoices.size());
                kpis.put("totalOverdueInr", totalOverdueInr);
                kpis.put("totalOverdueLakhs", overdueLakhs.toPlainString());

                // Add top citations
                for (SalesOrder o : delayedOrders) {
                    ctx.addEvidence(new CitationEvidence("ORDER", o.getId(),
                            "Order #" + o.getOrderNumber() + " (" + o.getCustomerName() + ")",
                            "At risk of missing delivery on " + o.getPromisedDeliveryDate() + ". Delay: " + o.getDelayDays() + " days.",
                            0.97, "/orders/" + o.getOrderNumber()));
                }

                for (InventoryItem i : criticalStock) {
                    String daysStr = i.getDaysOfStockRemaining() != null
                            ? i.getDaysOfStockRemaining().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                            : "0.00";
                    ctx.addEvidence(new CitationEvidence("INVENTORY", i.getId(),
                            "Inventory: " + i.getProductName(),
                            "Only " + i.getAvailableStock() + " units remaining (" + daysStr + " days stock left).",
                            0.96, "/inventory"));
                }

                if (!overdueInvoices.isEmpty()) {
                    Invoice largest = overdueInvoices.stream().max(Comparator.comparing(Invoice::getTotalAmount)).orElse(overdueInvoices.get(0));
                    ctx.addEvidence(new CitationEvidence("INVOICE", largest.getId(),
                            "Overdue Invoice #" + largest.getInvoiceNumber(),
                            "Largest overdue invoice: " + largest.getEntityName() + " owes ₹" + largest.getTotalAmount() + " (overdue by " + largest.getOverdueDays() + " days).",
                            0.95, "/invoices/" + largest.getInvoiceNumber()));
                }

                return new ToolExecutionResult("get_operational_kpis", true, kpis,
                        "Calculated daily operational health KPIs for attention center", null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
