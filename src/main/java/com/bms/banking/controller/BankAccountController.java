
package com.bms.banking.controller;

import com.bms.banking.entity.BankAccount;
import com.bms.banking.entity.Transaction;
import com.bms.banking.service.BankAccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    // 1. CREATE BANK ACCOUNT
    @PostMapping
    public ResponseEntity<BankAccount> createAccount(
            @RequestParam Long customerId,
            @RequestParam String accountType) {

        BankAccount account =
                bankAccountService.createAccount(customerId, accountType);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    // 2. DEPOSIT MONEY
    @PostMapping("/{id}/deposit")
    public BankAccount deposit(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        return bankAccountService.deposit(id, amount);
    }

    // 3. WITHDRAW MONEY
    @PostMapping("/{id}/withdraw")
    public BankAccount withdraw(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        return bankAccountService.withdraw(id, amount);
    }

    // 4. GET ACCOUNT DETAILS AND BALANCE
    @GetMapping("/{id}")
    public BankAccount getAccount(@PathVariable Long id) {

        return bankAccountService.getAccount(id);
    }

    // 5. TRANSFER MONEY
    @PostMapping("/transfer")
    public BankAccount transfer(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam BigDecimal amount) {

        return bankAccountService.transfer(
                fromAccountId, toAccountId, amount);
    }

    // 6. GET TRANSACTION HISTORY
    @GetMapping("/{id}/transactions")
    public List<Transaction> getTransactionHistory(
            @PathVariable Long id) {

        return bankAccountService.getTransactionHistory(id);
    }
}
