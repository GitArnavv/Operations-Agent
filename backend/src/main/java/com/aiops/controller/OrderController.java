package com.aiops.controller;

import com.aiops.domain.SalesOrder;
import com.aiops.security.SecurityUtils;
import com.aiops.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<SalesOrder>> getOrders() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(orderService.getOrders(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getOrderById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return orderService.getOrderById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<SalesOrder> getOrderByNumber(@PathVariable String orderNumber) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return orderService.getOrderByNumber(tenantId, orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
