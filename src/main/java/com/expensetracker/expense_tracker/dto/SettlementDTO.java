package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class SettlementDTO {

    private String fromUser;
    private String toUser;
    private BigDecimal amount;

    public SettlementDTO(String fromUser, String toUser, BigDecimal amount){
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
