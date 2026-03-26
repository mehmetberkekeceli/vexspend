package com.wallet.vexspend.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.AccountType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private AccountType type;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be ISO-4217 format")
    private String currencyCode;

    @DecimalMin(value = "0.00", message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance;

    public String name() {
        return name;
    }

    public AccountType type() {
        return type;
    }

    public String currencyCode() {
        return currencyCode;
    }

    public BigDecimal initialBalance() {
        return initialBalance;
    }

}


