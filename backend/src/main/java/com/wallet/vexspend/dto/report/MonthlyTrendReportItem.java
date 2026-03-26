package com.wallet.vexspend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendReportItem {

    private String month;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netCashflow;

    public String month() {
        return month;
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

}

