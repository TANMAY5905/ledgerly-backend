package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.*;
import com.expensetracker.expense_tracker.entity.Transaction;
import com.expensetracker.expense_tracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public DashboardResponseDTO getDashboard(String username,
                                             FilterType totalFilter,
                                             FilterType dailyFilter,
                                             FilterType tagFilter,
                                             FilterType groupFilter,
                                             LocalDate startDate,
                                             LocalDate endDate){

        LocalDate[] totalRange = resolveDateRange(totalFilter, startDate, endDate);
        LocalDate[] dailyRange = resolveDateRange(dailyFilter, startDate, endDate);
        LocalDate[] tagRange = resolveDateRange(tagFilter, startDate, endDate);
        LocalDate[] groupRange = resolveDateRange(groupFilter, startDate, endDate);

        LocalDateTime totalStart = totalRange[0].atStartOfDay();
        LocalDateTime totalEnd = totalRange[1].atTime(23,59,59);

        LocalDateTime dailyStart = dailyRange[0].atStartOfDay();
        LocalDateTime dailyEnd = dailyRange[1].atTime(23, 59, 59);

        LocalDateTime tagStart = tagRange[0].atStartOfDay();
        LocalDateTime tagEnd = tagRange[1].atTime(23, 59, 59);

        LocalDateTime groupStart = groupRange[0].atStartOfDay();
        LocalDateTime groupEnd = groupRange[1].atTime(23, 59, 59);

        Double total = transactionRepository
                .getTotalExpense(username, totalStart, totalEnd);

        List<Object[]> rawDaily = transactionRepository
                .getDailyExpensesRaw(username, dailyStart, dailyEnd);

        List<DailyExpenseDTO> dailyExpenses = rawDaily.stream()
                .map(obj -> {
                    LocalDate date = (LocalDate) obj[0];   // ✅ MUST be LocalDate
                    BigDecimal amount = (BigDecimal) obj[1];

                    return new DailyExpenseDTO(
                            date,
                            amount
                    );
                })
                .toList();

        List<TagExpenseDTO> tagExpenses =
                transactionRepository.getTagExpenses(username, tagStart, tagEnd);

        BigDecimal totalTagAmount = tagExpenses.stream()
                .map(tag -> tag.getTotal() != null ? tag.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalTagAmount.compareTo(BigDecimal.ZERO) > 0) {
            tagExpenses.forEach(tag -> {

                BigDecimal amount = tag.getTotal() != null ? tag.getTotal() : BigDecimal.ZERO;

                double percent = amount
                        .divide(totalTagAmount, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

                tag.setPercentage(percent);
            });
        }

        List<GroupExpenseDTO> groupExpenses =
                transactionRepository.getGroupExpenses(username, groupStart, groupEnd);

        List<Transaction> recentTransactions =
                transactionRepository.getRecentTransactions(username);

        return new DashboardResponseDTO(
                BigDecimal.valueOf(total),
                dailyExpenses,
                tagExpenses,
                groupExpenses,
                recentTransactions
        );
    }

    private LocalDate[] resolveDateRange(
            FilterType filter,
            LocalDate startDate,
            LocalDate endDate
    ){
        LocalDate today = LocalDate.now();

        if(filter == null){
            filter = FilterType.LAST_7_DAYS; // default per section
        }

        return switch (filter) {

            case LAST_7_DAYS ->
                    new LocalDate[]{today.minusDays(7), today};

            case LAST_MONTH ->
                    new LocalDate[]{today.minusMonths(1), today};

            case LAST_3_MONTHS ->
                    new LocalDate[]{today.minusMonths(3), today};

            case CUSTOM -> {
                if(startDate == null || endDate == null){
                    throw new RuntimeException("Custom filter requires startDate & endDate");
                }
                yield new LocalDate[]{startDate, endDate};
            }
            default ->
                    new LocalDate[]{today.minusDays(7), today};
        };
    }

    public GroupDashboardResponseDTO getGroupDashboard(Long groupId) {

        // 1. Monthly Trend
        List<MonthlyExpenseDTO> monthlyExpenses =
                transactionRepository.getMonthlyGroupExpenses(groupId)
                        .stream()
                        .map(obj -> new MonthlyExpenseDTO(
                                (String) obj[0],
                                (BigDecimal) obj[1]
                        ))
                        .toList();

        // 2. Member Expenses
        List<MemberExpenseDTO> members =
                transactionRepository.getMemberExpenses(groupId)
                        .stream()
                        .map(obj -> new MemberExpenseDTO(
                                (String) obj[0],
                                (BigDecimal) obj[1]
                        ))
                        .toList();

        // Total
        BigDecimal total = transactionRepository.getTotalGroupExpense(groupId);

        // 3. Percentage Calculation
        members.forEach(m -> {
            double percent = m.getAmount()
                    .divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            m.setPercentage(percent);
        });

        // 4. Equal Split
        BigDecimal perPerson = total.divide(
                BigDecimal.valueOf(members.size()),
                2,
                RoundingMode.HALF_UP
        );

        // 5. OWE ALGORITHM 🔥
        List<SettlementDTO> settlements = calculateSettlements(members, perPerson);

        return new GroupDashboardResponseDTO(
                monthlyExpenses,
                members,
                settlements,
                total,
                perPerson
        );
    }

    private List<SettlementDTO> calculateSettlements(
            List<MemberExpenseDTO> members,
            BigDecimal perPerson
    ) {

        List<SettlementDTO> result = new ArrayList<>();

        Map<String, BigDecimal> balanceMap = new HashMap<>();

        for (MemberExpenseDTO m: members){
            BigDecimal balance = m.getAmount().subtract(perPerson);
            balanceMap.put(m.getUsername(), balance);
        }

        List<Map.Entry<String, BigDecimal>> creditors = balanceMap.entrySet()
                .stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        List<Map.Entry<String, BigDecimal>> debtors = balanceMap.entrySet()
                .stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) < 0)
                .toList();

        for (var debtor : debtors) {
            BigDecimal debtAmount = debtor.getValue().abs();

            for(var creditor : creditors) {
                if (debtAmount.compareTo(BigDecimal.ZERO) == 0) break;

                BigDecimal credit = creditor.getValue();

                if (credit.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal settleAmount = debtAmount.min(credit);

                result.add(new SettlementDTO(
                        debtor.getKey(),
                        creditor.getKey(),
                        settleAmount
                ));

                debtAmount = debtAmount.subtract(settleAmount);
                creditor.setValue(credit.subtract(settleAmount));
            }
        }
        return result;
    }
}