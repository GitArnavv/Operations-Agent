package com.aiops.service;

import com.aiops.domain.Supplier;
import com.aiops.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getSuppliers(String tenantId) {
        return supplierRepository.findByTenantId(tenantId);
    }

    public Optional<Supplier> getSupplierById(String tenantId, String id) {
        return supplierRepository.findByTenantIdAndId(tenantId, id);
    }
}
