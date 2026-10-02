package com.aiops.controller;

import com.aiops.domain.Supplier;
import com.aiops.security.SecurityUtils;
import com.aiops.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getSuppliers() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(supplierService.getSuppliers(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return supplierService.getSupplierById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
