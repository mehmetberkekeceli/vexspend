package com.wallet.vexspend.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.AccountType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @Size(max = 120)
    private String name;

    private AccountType type;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be ISO-4217 format")
    private String currencyCode;

    @DecimalMin(value = "0.00", message = "Balance must be zero or positive")
    private BigDecimal currentBalance;

    private Boolean active;

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

    public Boolean active() {
        return active;
    }

}


