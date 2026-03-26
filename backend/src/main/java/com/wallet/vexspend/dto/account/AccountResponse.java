package com.wallet.vexspend.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.AccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private UUID id;

    private String name;

    private AccountType type;

    private String currencyCode;

    private BigDecimal currentBalance;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public AccountType type() {
        return type;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public BigDecimal currentBalance() {
        return currentBalance;
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

