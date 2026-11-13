
package com.bms.banking.service;

import com.bms.banking.entity.BankAccount;
import com.bms.banking.entity.Customer;
import com.bms.banking.entity.Transaction;
import com.bms.banking.repository.BankAccountRepository;
import com.bms.banking.repository.CustomerRepository;
import com.bms.banking.repository.TransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountSecurityService accountSecurityService;

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository,
            AccountSecurityService accountSecurityService) {

        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.accountSecurityService = accountSecurityService;
    }

    // CREATE BANK ACCOUNT
    @Transactional
    public BankAccount createAccount(Long customerId, String accountType) {

        accountSecurityService.verifyCustomerOwnership(customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        if (accountType == null ||
                (!accountType.equalsIgnoreCase("SAVINGS") &&
                        !accountType.equalsIgnoreCase("CURRENT"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid account type");
        }

        BankAccount account = new BankAccount();
        account.setAccountNumber(UUID.randomUUID().toString());
        account.setAccountType(accountType.toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(customer);

        return bankAccountRepository.save(account);
    }

    // DEPOSIT
    @Transactional
    public BankAccount deposit(Long accountId, BigDecimal amount) {

        accountSecurityService.verifyOwnership(accountId);
        validateAmount(amount);

        BankAccount account = findLockedAccount(accountId);
        account.setBalance(account.getBalance().add(amount));

        bankAccountRepository.save(account);
        saveTransaction("DEPOSIT", amount, null, accountId);

        return account;
    }

    // WITHDRAW
    @Transactional
    public BankAccount withdraw(Long accountId, BigDecimal amount) {

        accountSecurityService.verifyOwnership(accountId);
        validateAmount(amount);

        BankAccount account = findLockedAccount(accountId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        bankAccountRepository.save(account);
        saveTransaction("WITHDRAWAL", amount, accountId, null);

        return account;
    }

    // VIEW ACCOUNT
    @Transactional(readOnly = true)
    public BankAccount getAccount(Long accountId) {

        accountSecurityService.verifyOwnership(accountId);

        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bank account not found"));
    }

    // TRANSFER MONEY
    @Transactional
    public BankAccount transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount) {

        validateAmount(amount);

        if (fromAccountId == null || toAccountId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Account IDs are required");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot transfer to the same account");
        }

        // Only the sender must own the source account.
        accountSecurityService.verifyOwnership(fromAccountId);

        // Lock accounts in consistent order to prevent deadlocks.
        Long firstId = Math.min(fromAccountId, toAccountId);
        Long secondId = Math.max(fromAccountId, toAccountId);

        BankAccount firstAccount = findLockedAccount(firstId);
        BankAccount secondAccount = findLockedAccount(secondId);

        BankAccount fromAccount =
                fromAccountId.equals(firstId) ? firstAccount : secondAccount;

        BankAccount toAccount =
                toAccountId.equals(firstId) ? firstAccount : secondAccount;

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient balance for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        saveTransaction("TRANSFER", amount, fromAccountId, toAccountId);

        return fromAccount;
    }

    // TRANSACTION HISTORY
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(Long accountId) {

        accountSecurityService.verifyOwnership(accountId);

        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(
                        accountId, accountId);
    }

    // SAVE TRANSACTION RECORD
    private void saveTransaction(
            String type,
            BigDecimal amount,
            Long fromAccountId,
            Long toAccountId) {

        Transaction transaction = new Transaction();
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);

        transactionRepository.save(transaction);
    }

    // LOCK ACCOUNT BEFORE UPDATING BALANCE
    private BankAccount findLockedAccount(Long accountId) {

        return bankAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bank account not found"));
    }

    // VALIDATE TRANSACTION AMOUNT
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.signum() <= 0 ||
                amount.scale() > 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid transaction amount");
        }
    }
}
