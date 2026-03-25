package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class TagExpenseDTO {

    private String tagName;
    private BigDecimal total;
    private Double percentage;

    public TagExpenseDTO(String tagName, BigDecimal total){
        this.tagName = tagName;
        this.total = total;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getTagName() {return tagName;}
    public BigDecimal getTotal() {return total;}

    public Double getPercentage() {return percentage;}
}
