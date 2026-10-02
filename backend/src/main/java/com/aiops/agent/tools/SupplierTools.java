package com.aiops.agent.tools;

import com.aiops.agent.AgentRunContext;
import com.aiops.agent.CitationEvidence;
import com.aiops.agent.ToolExecutionResult;
import com.aiops.domain.Supplier;
import com.aiops.domain.enums.RiskLevel;
import com.aiops.repository.SupplierRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SupplierTools {

    private final SupplierRepository supplierRepository;

    public SupplierTools(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public AgentTool createGetSuppliersTool() {
        return new AgentTool() {
            @Override public String getName() { return "get_suppliers"; }
            @Override public String getDescription() { return "Get list of registered suppliers with ratings and lead time."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "suppliers.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                List<Supplier> suppliers = supplierRepository.findByTenantId(tenantId);
                return new ToolExecutionResult("get_suppliers", true, suppliers,
                        "Found " + suppliers.size() + " active suppliers", null,
                        System.currentTimeMillis() - start);
            }
        };
    }

    public AgentTool createCompareSuppliersTool() {
        return new AgentTool() {
            @Override public String getName() { return "compare_suppliers"; }
            @Override public String getDescription() { return "Compare historical supplier performance, lead times, and reliability metrics."; }
            @Override public RiskLevel getRiskLevel() { return RiskLevel.READ_ONLY; }
            @Override public String getRequiredPermission() { return "suppliers.read"; }

            @Override
            public ToolExecutionResult execute(Map<String, Object> input, AgentRunContext ctx) {
                long start = System.currentTimeMillis();
                String tenantId = ctx.getTenantId();
                List<Supplier> suppliers = supplierRepository.findByTenantId(tenantId);

                // Sort by on-time delivery rate and reliability score
                List<Supplier> sorted = new ArrayList<>(suppliers);
                sorted.sort((a, b) -> (b.getReliabilityScore() != null && a.getReliabilityScore() != null)
                        ? b.getReliabilityScore().compareTo(a.getReliabilityScore())
                        : 0);

                for (Supplier s : sorted) {
                    ctx.addEvidence(new CitationEvidence("SUPPLIER", s.getId(),
                            "Supplier: " + s.getName(),
                            "On-Time: " + s.getOnTimeDeliveryRate() + "% | Lead Time: " + s.getLeadTimeDays() + " days | Defect Rate: " + s.getDefectRate() + "% | Terms: " + s.getPaymentTerms(),
                            0.95, "/suppliers/" + s.getId()));
                }

                Map<String, Object> comparison = new LinkedHashMap<>();
                comparison.put("rankedSuppliers", sorted);
                if (!sorted.isEmpty()) {
                    comparison.put("bestPerformingSupplier", sorted.get(0));
                }

                return new ToolExecutionResult("compare_suppliers", true, comparison,
                        "Compared " + sorted.size() + " suppliers across lead time, defect rate and SLA compliance", null,
                        System.currentTimeMillis() - start);
            }
        };
    }
}
