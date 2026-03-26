package com.wallet.vexspend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private UUID id;

    private String name;

    private String currencyCode;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private BigDecimal totalLimit;

    private BigDecimal spentAmount;

    private BigDecimal remainingAmount;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public LocalDate periodStart() {
        return periodStart;
    }

    public LocalDate periodEnd() {
        return periodEnd;
    }

    public BigDecimal totalLimit() {
        return totalLimit;
    }

    public BigDecimal spentAmount() {
        return spentAmount;
    }

    public BigDecimal remainingAmount() {
        return remainingAmount;
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}

