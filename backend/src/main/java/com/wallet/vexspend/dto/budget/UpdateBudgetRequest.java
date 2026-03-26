package com.wallet.vexspend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBudgetRequest {

    @Size(max = 120)
    private String name;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be ISO-4217 format (e.g. USD)")
    private String currencyCode;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    @DecimalMin(value = "0.01", message = "Budget limit must be greater than zero")
    private BigDecimal totalLimit;

    private Boolean active;

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

    public Boolean active() {
        return active;
    }

}


