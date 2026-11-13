
package com.bms.banking.controller;

import com.bms.banking.entity.Customer;
import com.bms.banking.repository.CustomerRepository;
import com.bms.banking.service.AccountSecurityService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final AccountSecurityService accountSecurityService;

    public CustomerController(
            CustomerRepository customerRepository,
            AccountSecurityService accountSecurityService) {

        this.customerRepository = customerRepository;
        this.accountSecurityService = accountSecurityService;
    }

    // VIEW OWN CUSTOMER PROFILE
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {

        accountSecurityService.verifyCustomerOwnership(id);

        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"));
    }

    // UPDATE OWN CUSTOMER PROFILE
    @PutMapping("/{id}")
    public Customer updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer updatedCustomer) {

        accountSecurityService.verifyCustomerOwnership(id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"));

        if (updatedCustomer.getName() == null ||
                updatedCustomer.getName().isBlank() ||
                updatedCustomer.getEmail() == null ||
                updatedCustomer.getEmail().isBlank() ||
                updatedCustomer.getPhone() == null ||
                updatedCustomer.getPhone().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, email and phone are required");
        }

        customer.setName(updatedCustomer.getName());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhone(updatedCustomer.getPhone());

        return customerRepository.save(customer);
    }
}
