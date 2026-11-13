
package com.bms.banking;

import com.bms.banking.entity.BankAccount;
import com.bms.banking.repository.BankAccountRepository;
import com.bms.banking.repository.CustomerRepository;
import com.bms.banking.repository.TransactionRepository;
import com.bms.banking.service.AccountSecurityService;
import com.bms.banking.service.BankAccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {

    private BankAccountRepository bankAccountRepository;
    private AccountSecurityService accountSecurityService;
    private BankAccountService bankAccountService;

    @BeforeEach
    void setUp() {

        bankAccountRepository = mock(BankAccountRepository.class);
        CustomerRepository customerRepository =
                mock(CustomerRepository.class);
        TransactionRepository transactionRepository =
                mock(TransactionRepository.class);
        accountSecurityService = mock(AccountSecurityService.class);

        bankAccountService = new BankAccountService(
                bankAccountRepository,
                customerRepository,
                transactionRepository,
                accountSecurityService
        );
    }

    @Test
    void depositShouldIncreaseBalance() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("500.00"));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account));

        bankAccountService.deposit(1L, new BigDecimal("200.00"));

        assertEquals(
                0,
                new BigDecimal("700.00")
                        .compareTo(account.getBalance())
        );

        verify(accountSecurityService).verifyOwnership(1L);
        verify(bankAccountRepository).save(account);
    }

    @Test
    void withdrawShouldDecreaseBalance() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("500.00"));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account));

        bankAccountService.withdraw(1L, new BigDecimal("200.00"));

        assertEquals(
                0,
                new BigDecimal("300.00")
                        .compareTo(account.getBalance())
        );

        verify(accountSecurityService).verifyOwnership(1L);
    }

    @Test
    void withdrawShouldRejectInsufficientBalance() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("100.00"));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bankAccountService.withdraw(
                        1L, new BigDecimal("500.00"))
        );
    }

    @Test
    void depositShouldRejectNegativeAmount() {

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bankAccountService.deposit(
                        1L, new BigDecimal("-50.00"))
        );
    }

    @Test
    void unauthorizedAccountShouldBeRejected() {

        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN))
                .when(accountSecurityService).verifyOwnership(2L);

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bankAccountService.withdraw(
                        2L, new BigDecimal("10.00"))
        );

        verify(bankAccountRepository, never())
                .findByIdForUpdate(2L);
    }
}
