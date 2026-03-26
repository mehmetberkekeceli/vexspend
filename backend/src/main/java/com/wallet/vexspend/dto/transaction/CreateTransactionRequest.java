package com.wallet.vexspend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class CreateTransactionRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull
    private LocalDate transactionDate;

    @NotNull
    private UUID categoryId;

    private UUID budgetId;

    private UUID budgetItemId;

    @Size(max = 120)
    private String merchant;

    @Size(max = 500)
    private String note;

    public UUID accountId() {
        return accountId;
    }

    public TransactionType type() {
        return type;
    }

    public BigDecimal amount() {
        return amount;
    }

    public LocalDate transactionDate() {
        return transactionDate;
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

    public String merchant() {
        return merchant;
    }

    public String note() {
        return note;
    }

}


