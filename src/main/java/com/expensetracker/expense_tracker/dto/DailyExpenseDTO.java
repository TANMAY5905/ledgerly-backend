package com.expensetracker.expense_tracker.dto;

import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyExpenseDTO {

    private LocalDate date;
    private BigDecimal total;

    public DailyExpenseDTO(LocalDate date, BigDecimal total){
        this.date = date;
        this.total = total;
    }

    public LocalDate getDate() {return date;}

    public BigDecimal getTotal() {return total;}
}
