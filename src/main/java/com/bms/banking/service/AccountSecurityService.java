
package com.bms.banking.service;

import com.bms.banking.entity.AppUser;
import com.bms.banking.entity.BankAccount;
import com.bms.banking.repository.AppUserRepository;
import com.bms.banking.repository.BankAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountSecurityService {

    private final AppUserRepository appUserRepository;
    private final BankAccountRepository bankAccountRepository;

    public AccountSecurityService(
            AppUserRepository appUserRepository,
            BankAccountRepository bankAccountRepository) {

        this.appUserRepository = appUserRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public void verifyOwnership(Long accountId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Login required");
        }

        AppUser user = appUserRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        BankAccount account = bankAccountRepository
                .findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bank account not found"));

        if (user.getCustomer() == null ||
                !account.getCustomer().getId()
                        .equals(user.getCustomer().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this bank account");
        }
    }

    public void verifyCustomerOwnership(Long customerId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Login required");
        }

        AppUser user = appUserRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getCustomer() == null ||
                !user.getCustomer().getId().equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this customer profile");
        }
    }
}
