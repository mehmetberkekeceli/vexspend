package com.wallet.vexspend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;

    private TransactionType type;

    private BigDecimal amount;

    private LocalDate transactionDate;

    private UUID accountId;

    private String accountName;

    private UUID categoryId;

    private String categoryName;

    private UUID budgetId;

    private UUID budgetItemId;

    private UUID recurringTemplateId;

    private String merchant;

    private String note;

    private Instant createdAt;

    public UUID id() {
        return id;
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

    public UUID recurringTemplateId() {
        return recurringTemplateId;
    }

    public String merchant() {
        return merchant;
    }

    public String note() {
        return note;
    }

    public Instant createdAt() {
        return createdAt;
    }

}

