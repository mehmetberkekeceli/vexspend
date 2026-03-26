package com.wallet.vexspend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportResponse {

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netCashflow;

    private BigDecimal budgetLimitTotal;

    private BigDecimal budgetSpentTotal;

    private BigDecimal budgetRemainingTotal;

    private List<CategorySpendingReportItem> categorySpending;

    private List<MonthlyTrendReportItem> monthlyTrend;

    private List<AccountBalanceReportItem> accountBalances;

    public LocalDate periodStart() {
        return periodStart;
    }

    public LocalDate periodEnd() {
        return periodEnd;
    }

    public BigDecimal totalIncome() {
        return totalIncome;
    }

    public BigDecimal totalExpense() {
        return totalExpense;
    }

    public BigDecimal netCashflow() {
        return netCashflow;
    }

    public BigDecimal budgetLimitTotal() {
        return budgetLimitTotal;
    }

    public BigDecimal budgetSpentTotal() {
        return budgetSpentTotal;
    }

    public BigDecimal budgetRemainingTotal() {
        return budgetRemainingTotal;
    }

    public List<CategorySpendingReportItem> categorySpending() {
        return categorySpending;
    }

    public List<MonthlyTrendReportItem> monthlyTrend() {
        return monthlyTrend;
    }

    public List<AccountBalanceReportItem> accountBalances() {
        return accountBalances;
    }

}

