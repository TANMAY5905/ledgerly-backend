package com.expensetracker.expense_tracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class GroupDashboardResponseDTO {

    private List<MonthlyExpenseDTO> monthlyExpenses;
    private List<MemberExpenseDTO> memberExpenses;
    private List<SettlementDTO> settlements;
    private BigDecimal totalExpense;
    private BigDecimal perPersonShare;

    public GroupDashboardResponseDTO(
            List<MonthlyExpenseDTO> monthlyExpenses,
            List<MemberExpenseDTO> memberExpenses,
            List<SettlementDTO> settlements,
            BigDecimal totalExpense,
            BigDecimal perPersonShare
    ) {
        this.monthlyExpenses = monthlyExpenses;
        this.memberExpenses = memberExpenses;
        this.settlements = settlements;
        this.totalExpense = totalExpense;
        this.perPersonShare = perPersonShare;
    }

    public List<MonthlyExpenseDTO> getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(List<MonthlyExpenseDTO> monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public List<MemberExpenseDTO> getMemberExpenses() {
        return memberExpenses;
    }

    public void setMemberExpenses(List<MemberExpenseDTO> memberExpenses) {
        this.memberExpenses = memberExpenses;
    }

    public List<SettlementDTO> getSettlements() {
        return settlements;
    }

    public void setSettlements(List<SettlementDTO> settlements) {
        this.settlements = settlements;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getPerPersonShare() {
        return perPersonShare;
    }

    public void setPerPersonShare(BigDecimal perPersonShare) {
        this.perPersonShare = perPersonShare;
    }
}
