
package com.bms.banking.repository;

import com.bms.banking.entity.BankAccount;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    // 1. FIND ACCOUNT BY ACCOUNT NUMBER
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    // 2. CHECK IF ACCOUNT NUMBER EXISTS
    boolean existsByAccountNumber(String accountNumber);

    // 3. LOCK ACCOUNT DURING MONEY TRANSACTIONS
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
}
