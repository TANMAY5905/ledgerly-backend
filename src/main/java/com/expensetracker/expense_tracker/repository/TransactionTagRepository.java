package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.TransactionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionTagRepository extends JpaRepository<TransactionTag, Long> {
    @Modifying
    @Transactional
    void deleteByTransactionId(Long transactionId);
}
