package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class UserExpenseSummaryDTO {

    private BigDecimal totalExpense;
    private BigDecimal yourShare;

    public UserExpenseSummaryDTO(BigDecimal totalExpense, BigDecimal yourShare){
        this.totalExpense = totalExpense;
        this.yourShare = yourShare;
    }

    public BigDecimal getTotalExpense(){
        return totalExpense;
    }

    public BigDecimal getYourShare(){
        return yourShare;
    }
}
