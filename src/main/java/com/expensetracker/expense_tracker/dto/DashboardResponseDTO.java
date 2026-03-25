package com.expensetracker.expense_tracker.dto;

import com.expensetracker.expense_tracker.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponseDTO {

    private BigDecimal totalExpense;
    private List<DailyExpenseDTO> dailyExpenses;
    private List<TagExpenseDTO> tagExpenses;
    private List<GroupExpenseDTO> groupExpenses;
    private List<Transaction> recentTransactions;

    public DashboardResponseDTO(
            BigDecimal totalExpense,
            List<DailyExpenseDTO> dailyExpenses,
            List<TagExpenseDTO> tagExpenses,
            List<GroupExpenseDTO> groupExpenses,
            List<Transaction> recentTransactions
     ){
        this.totalExpense = totalExpense;
        this.dailyExpenses = dailyExpenses;
        this.tagExpenses = tagExpenses;
        this.groupExpenses = groupExpenses;
        this.recentTransactions = recentTransactions;
    }

    public BigDecimal getTotalExpense() {return totalExpense;}

    public List<DailyExpenseDTO> getDailyExpenses() {return dailyExpenses;}

    public List<TagExpenseDTO> getTagExpenses() {return tagExpenses;}

    public List<GroupExpenseDTO> getGroupExpenses() {return groupExpenses;}

    public List<Transaction> getRecentTransactions() {return recentTransactions;}
}
