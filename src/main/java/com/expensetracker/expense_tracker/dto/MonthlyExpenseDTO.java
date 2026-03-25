package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class MonthlyExpenseDTO {

    private String month;
    private BigDecimal total;

    public MonthlyExpenseDTO(String month, BigDecimal total){
        this.month = month;
        this.total = total;
    }
}
