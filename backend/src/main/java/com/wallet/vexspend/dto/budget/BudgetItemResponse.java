package com.wallet.vexspend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItemResponse {

    private UUID id;

    private UUID budgetId;

    private UUID categoryId;

    private String categoryName;

    private String name;

    private BigDecimal allocatedAmount;

    private BigDecimal spentAmount;

    private BigDecimal remainingAmount;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

    public UUID budgetId() {
        return budgetId;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public String categoryName() {
        return categoryName;
    }

    public String name() {
        return name;
    }

    public BigDecimal allocatedAmount() {
        return allocatedAmount;
    }

    public BigDecimal spentAmount() {
        return spentAmount;
    }

    public BigDecimal remainingAmount() {
        return remainingAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}

