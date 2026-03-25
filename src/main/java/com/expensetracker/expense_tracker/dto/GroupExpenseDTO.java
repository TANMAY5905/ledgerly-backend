package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;

public class GroupExpenseDTO {

    private Long groupId;
    private BigDecimal total;
    private String groupName;

    public GroupExpenseDTO(Long groupId, String groupName, BigDecimal total){
        this.groupId = groupId;
        this.groupName = groupName;
        this.total = total;
    }

    public Long getGroupId() {return groupId;}
    public BigDecimal getTotal() {return total;}
    public String getGroupName() {return groupName;}
}
