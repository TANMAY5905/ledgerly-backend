package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class MemberExpenseDTO {

    private String username;
    private BigDecimal amount;
    private Double percentage;

    public MemberExpenseDTO(String username, BigDecimal amount){
        this.username = username;
        this.amount = amount;
    }

    public void setPercentage(Double percentage){
        this.percentage = percentage;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUsername() {
        return username;
    }
}
