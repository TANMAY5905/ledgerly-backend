package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.dto.GroupExpenseDTO;
import com.expensetracker.expense_tracker.dto.TagExpenseDTO;
import com.expensetracker.expense_tracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable page);

    List<Transaction> findByGroupId(Long groupId);

    // Total Expense
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.createdBy.username = :username
        AND t.createdAt BETWEEN :startDate AND :endDate
    """)
    Double getTotalExpense(String username, LocalDateTime startDate, LocalDateTime endDate);

    // Daily Expense
    @Query("""
        SELECT DATE(t.createdAt), SUM(t.amount)
        FROM Transaction t
        WHERE t.createdBy.username = :username
        AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    List<Object[]> getDailyExpensesRaw(
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Tag Expense
    @Query("""
        SELECT new com.expensetracker.expense_tracker.dto.TagExpenseDTO(
            tag.name,
            SUM(t.amount)
        )
        FROM Transaction t
        JOIN t.transactionTags tt
        JOIN tt.tag tag
        WHERE t.createdBy.username = :username
        AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY tag.name
    """)
    List<TagExpenseDTO> getTagExpenses(String username, LocalDateTime startDate, LocalDateTime endDate);

    // Group Expense
    @Query("""
        SELECT new com.expensetracker.expense_tracker.dto.GroupExpenseDTO(
            t.group.id,
            t.group.name,
            SUM(t.amount)
        )
        FROM Transaction t
        WHERE t.createdBy.username = :username
        AND t.group IS NOT NULL
        AND t.createdAt BETWEEN :startDate AND :endDate
        GROUP BY t.group.id, t.group.name
    """)
    List<GroupExpenseDTO> getGroupExpenses(
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Recent Transactions
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.createdBy.username = :username
        ORDER BY t.createdAt DESC
    """)
    List<Transaction> getRecentTransactions(String username);

    //line chart
    @Query("""
        SELECT TO_CHAR(t.createdAt, 'YYYY-MM'), SUM(t.amount)
        FROM Transaction t
        WHERE t.group.id = :groupId
        GROUP BY TO_CHAR(t.createdAt, 'YYYY-MM')
        ORDER BY TO_CHAR(t.createdAt, 'YYYY-MM')
    """)
    List<Object[]> getMonthlyGroupExpenses(Long groupId);

    //pie chart
    @Query("""
        SELECT t.createdBy.username, SUM(t.amount)
        FROM Transaction t
        WHERE t.group.id = :groupId
        GROUP BY t.createdBy.username
    """)
    List<Object[]> getMemberExpenses(Long groupId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.group.id = :groupId
    """)
    BigDecimal getTotalGroupExpense(Long groupId);
}