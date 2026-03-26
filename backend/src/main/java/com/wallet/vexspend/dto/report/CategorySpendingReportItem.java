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
public class CategorySpendingReportItem {

    private UUID categoryId;

    private String categoryName;

    private BigDecimal amount;

    private BigDecimal percentage;

    public UUID categoryId() {
        return categoryId;
    }

    public String categoryName() {
        return categoryName;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal percentage() {
        return percentage;
    }

}

