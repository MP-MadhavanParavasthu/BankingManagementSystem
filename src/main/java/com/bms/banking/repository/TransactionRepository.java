
package com.bms.banking.repository;

import com.bms.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // Get transaction history for a bank account
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(
            Long fromAccountId,
            Long toAccountId
    );
}
