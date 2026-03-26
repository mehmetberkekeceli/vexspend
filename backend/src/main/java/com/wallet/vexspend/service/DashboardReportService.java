package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.report.AccountBalanceReportItem;
import com.wallet.vexspend.dto.report.CategorySpendingReportItem;
import com.wallet.vexspend.dto.report.DashboardReportResponse;
import com.wallet.vexspend.dto.report.MonthlyTrendReportItem;
import com.wallet.vexspend.entity.Budget;
import com.wallet.vexspend.entity.Transaction;
import com.wallet.vexspend.entity.TransactionType;
import com.wallet.vexspend.repository.AccountRepository;
import com.wallet.vexspend.repository.BudgetRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardReportService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public DashboardReportResponse getDashboard(UUID userId, LocalDate from, LocalDate to, Integer trendMonths) {
        LocalDate periodEnd = to == null ? LocalDate.now() : to;
        LocalDate periodStart = from == null ? periodEnd.withDayOfMonth(1) : from;

        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("from date must be before or equal to to date");
        }

        List<Transaction> periodTransactions = transactionRepository
                .findAllByOwnerIdAndTransactionDateBetween(userId, periodStart, periodEnd);

        BigDecimal totalIncome = sumByType(periodTransactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(periodTransactions, TransactionType.EXPENSE);
        BigDecimal netCashflow = totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP);

        List<CategorySpendingReportItem> categorySpending = buildCategorySpending(periodTransactions, totalExpense);

        List<Budget> overlappedBudgets = budgetRepository.findAllByOwnerIdOrderByPeriodStartDesc(userId).stream()
                .filter(b -> isOverlapping(b, periodStart, periodEnd))
                .collect(Collectors.toList());

        BigDecimal budgetLimitTotal = overlappedBudgets.stream()
                .map(Budget::getTotalLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal budgetSpentTotal = overlappedBudgets.stream()
                .map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal budgetRemainingTotal = budgetLimitTotal.subtract(budgetSpentTotal).setScale(2, RoundingMode.HALF_UP);

        List<AccountBalanceReportItem> accountBalances = accountRepository.findAllByOwnerIdOrderByNameAsc(userId).stream()
                .map(a -> new AccountBalanceReportItem(a.getId(), a.getName(), a.getCurrencyCode(), a.getCurrentBalance()))
                .collect(Collectors.toList());

        int months = (trendMonths == null || trendMonths < 1) ? 6 : trendMonths;
        List<MonthlyTrendReportItem> monthlyTrend = buildMonthlyTrend(userId, periodEnd, months);

        return new DashboardReportResponse(
                periodStart,
                periodEnd,
                totalIncome,
                totalExpense,
                netCashflow,
                budgetLimitTotal,
                budgetSpentTotal,
                budgetRemainingTotal,
                categorySpending,
                monthlyTrend,
                accountBalances
        );
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(tx -> tx.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<CategorySpendingReportItem> buildCategorySpending(List<Transaction> transactions, BigDecimal totalExpense) {
        Map<UUID, BigDecimal> amountMap = new HashMap<>();
        Map<UUID, String> nameMap = new HashMap<>();

        for (Transaction tx : transactions) {
            if (tx.getType() != TransactionType.EXPENSE) {
                continue;
            }

            UUID categoryId = tx.getCategory().getId();
            amountMap.merge(categoryId, tx.getAmount(), BigDecimal::add);
            nameMap.putIfAbsent(categoryId, tx.getCategory().getName());
        }

        List<CategorySpendingReportItem> items = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : amountMap.entrySet()) {
            BigDecimal amount = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : amount.multiply(BigDecimal.valueOf(100)).divide(totalExpense, 2, RoundingMode.HALF_UP);

            items.add(new CategorySpendingReportItem(entry.getKey(), nameMap.get(entry.getKey()), amount, percentage));
        }

        items.sort(Comparator.comparing(CategorySpendingReportItem::amount).reversed());
        return items;
    }

    private boolean isOverlapping(Budget budget, LocalDate from, LocalDate to) {
        return !budget.getPeriodEnd().isBefore(from) && !budget.getPeriodStart().isAfter(to);
    }

    private List<MonthlyTrendReportItem> buildMonthlyTrend(UUID userId, LocalDate periodEnd, int months) {
        YearMonth endMonth = YearMonth.from(periodEnd);
        YearMonth startMonth = endMonth.minusMonths(months - 1L);

        LocalDate trendStart = startMonth.atDay(1);
        List<Transaction> trendTransactions = transactionRepository.findAllByOwnerIdAndTransactionDateBetween(
                userId,
                trendStart,
                periodEnd
        );

        Map<YearMonth, BigDecimal> incomeMap = new HashMap<>();
        Map<YearMonth, BigDecimal> expenseMap = new HashMap<>();

        for (Transaction tx : trendTransactions) {
            YearMonth month = YearMonth.from(tx.getTransactionDate());
            if (tx.getType() == TransactionType.INCOME) {
                incomeMap.merge(month, tx.getAmount(), BigDecimal::add);
            } else {
                expenseMap.merge(month, tx.getAmount(), BigDecimal::add);
            }
        }

        Map<YearMonth, MonthlyTrendReportItem> ordered = new LinkedHashMap<>();
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            BigDecimal income = incomeMap.getOrDefault(cursor, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal expense = expenseMap.getOrDefault(cursor, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = income.subtract(expense).setScale(2, RoundingMode.HALF_UP);

            ordered.put(cursor, new MonthlyTrendReportItem(cursor.toString(), income, expense, net));
            cursor = cursor.plusMonths(1);
        }

        return new ArrayList<>(ordered.values());
    }
}

