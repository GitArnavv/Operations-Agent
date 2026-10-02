package com.aiops.controller;

import com.aiops.domain.InventoryItem;
import com.aiops.domain.Product;
import com.aiops.domain.Warehouse;
import com.aiops.security.SecurityUtils;
import com.aiops.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getInventory() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getInventoryItems(tenantId));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getProducts(tenantId));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return inventoryService.getProductById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<Warehouse>> getWarehouses() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getWarehouses(tenantId));
    }
}
