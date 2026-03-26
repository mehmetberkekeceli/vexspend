package com.wallet.vexspend.dto.recurring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.RecurrenceFrequency;
import com.wallet.vexspend.entity.TransactionType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringTransactionRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    private UUID categoryId;

    private UUID budgetId;

    private UUID budgetItemId;

    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull
    private RecurrenceFrequency frequency;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 120)
    private String merchant;

    @Size(max = 500)
    private String note;

    public UUID accountId() {
        return accountId;
    }

    public UUID categoryId() {
        return categoryId;
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

    public String merchant() {
        return merchant;
    }

    public String note() {
        return note;
    }

}


