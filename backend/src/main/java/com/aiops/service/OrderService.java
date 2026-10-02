package com.aiops.service;

import com.aiops.domain.SalesOrder;
import com.aiops.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final SalesOrderRepository salesOrderRepository;

    public OrderService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    public List<SalesOrder> getOrders(String tenantId) {
        return salesOrderRepository.findByTenantId(tenantId);
    }

    public Optional<SalesOrder> getOrderById(String tenantId, String id) {
        return salesOrderRepository.findByTenantIdAndId(tenantId, id);
    }

    public Optional<SalesOrder> getOrderByNumber(String tenantId, String orderNumber) {
        return salesOrderRepository.findByTenantIdAndOrderNumber(tenantId, orderNumber);
    }
}
