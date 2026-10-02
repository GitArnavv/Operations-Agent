package com.aiops.service;

import com.aiops.domain.Customer;
import com.aiops.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getCustomers(String tenantId) {
        return customerRepository.findByTenantId(tenantId);
    }

    public Optional<Customer> getCustomerById(String tenantId, String id) {
        return customerRepository.findByTenantIdAndId(tenantId, id);
    }
}
