package com.wallet.vexspend.dto.recurring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.RecurrenceFrequency;
import com.wallet.vexspend.entity.TransactionType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringTransactionRequest {

    private UUID accountId;

    private UUID categoryId;

    private UUID budgetId;

    private UUID budgetItemId;

    private TransactionType type;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private RecurrenceFrequency frequency;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate nextExecutionDate;

    @Size(max = 120)
    private String merchant;

    @Size(max = 500)
    private String note;

    private Boolean active;

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

    public LocalDate nextExecutionDate() {
        return nextExecutionDate;
    }

    public String merchant() {
        return merchant;
    }

    public String note() {
        return note;
    }

    public Boolean active() {
        return active;
    }

}


