
package com.bms.banking.controller;

import com.bms.banking.entity.AppUser;
import com.bms.banking.entity.Customer;
import com.bms.banking.repository.AppUserRepository;
import com.bms.banking.repository.CustomerRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AppUserRepository appUserRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER NEW CUSTOMER AND LOGIN
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {

        if (request == null ||
                request.username() == null ||
                request.username().isBlank() ||
                request.password() == null ||
                request.password().length() < 12 ||
                request.name() == null ||
                request.name().isBlank() ||
                request.email() == null ||
                request.email().isBlank() ||
                request.phone() == null ||
                request.phone().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username, password (minimum 12 characters), " +
                            "name, email and phone are required");
        }

        String username = request.username().trim();

        if (appUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists");
        }

        // CREATE A NEW CUSTOMER
        Customer customer = new Customer();
        customer.setName(request.name().trim());
        customer.setEmail(request.email().trim());
        customer.setPhone(request.phone().trim());

        customer = customerRepository.save(customer);

        // CREATE LOGIN LINKED TO NEW CUSTOMER
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setCustomer(customer);

        try {
            appUserRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Registration could not be completed");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Customer and user registered successfully");
    }

    public record RegisterRequest(
            String username,
            String password,
            String name,
            String email,
            String phone
    ) {}
}
