package com.wallet.vexspend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceReportItem {

    private UUID accountId;

    private String accountName;

    private String currencyCode;

    private BigDecimal currentBalance;

    public UUID accountId() {
        return accountId;
    }

    public String accountName() {
        return accountName;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public BigDecimal currentBalance() {
        return currentBalance;
    }

}

