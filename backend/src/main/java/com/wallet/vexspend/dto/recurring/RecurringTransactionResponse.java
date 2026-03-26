package com.wallet.vexspend.dto.recurring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.RecurrenceFrequency;
import com.wallet.vexspend.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionResponse {

    private UUID id;

    private UUID accountId;

    private String accountName;

    private UUID categoryId;

    private String categoryName;

    private UUID budgetId;

    private UUID budgetItemId;

    private TransactionType type;

    private BigDecimal amount;

    private RecurrenceFrequency frequency;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate nextExecutionDate;

    private LocalDate lastExecutionDate;

    private String merchant;

    private String note;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

    public UUID accountId() {
        return accountId;
    }

    public String accountName() {
        return accountName;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public String categoryName() {
        return categoryName;
    }

    public UUID budgetId() {
        return budgetId;
    }

    public UUID budgetItemId() {
        return budgetItemId;
    }

    public TransactionType type() {
        return type;
    }

    public BigDecimal amount() {
        return amount;
    }

    public RecurrenceFrequency frequency() {
        return frequency;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public LocalDate nextExecutionDate() {
        return nextExecutionDate;
    }

    public LocalDate lastExecutionDate() {
        return lastExecutionDate;
    }

    public String merchant() {
        return merchant;
    }

    public String note() {
        return note;
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

