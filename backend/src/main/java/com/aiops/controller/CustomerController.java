package com.aiops.controller;

import com.aiops.domain.Customer;
import com.aiops.security.SecurityUtils;
import com.aiops.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(customerService.getCustomers(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return customerService.getCustomerById(tenantId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
