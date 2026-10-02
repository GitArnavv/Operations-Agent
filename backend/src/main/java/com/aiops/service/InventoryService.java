package com.aiops.service;

import com.aiops.domain.InventoryItem;
import com.aiops.domain.Product;
import com.aiops.domain.Warehouse;
import com.aiops.repository.InventoryItemRepository;
import com.aiops.repository.ProductRepository;
import com.aiops.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            ProductRepository productRepository,
                            WarehouseRepository warehouseRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<InventoryItem> getInventoryItems(String tenantId) {
        return inventoryItemRepository.findByTenantId(tenantId);
    }

    public List<Product> getProducts(String tenantId) {
        return productRepository.findByTenantId(tenantId);
    }

    public Optional<Product> getProductById(String tenantId, String id) {
        return productRepository.findByTenantIdAndId(tenantId, id);
    }

    public List<Warehouse> getWarehouses(String tenantId) {
        return warehouseRepository.findByTenantId(tenantId);
    }
}
