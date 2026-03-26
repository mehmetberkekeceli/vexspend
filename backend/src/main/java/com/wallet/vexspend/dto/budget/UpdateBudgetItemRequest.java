package com.wallet.vexspend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBudgetItemRequest {

    @Size(max = 120)
    private String name;

    private UUID categoryId;

    @DecimalMin(value = "0.01", message = "Allocated amount must be greater than zero")
    private BigDecimal allocatedAmount;

    public String name() {
        return name;
    }

    public UUID categoryId() {
        return categoryId;
    }

    public BigDecimal allocatedAmount() {
        return allocatedAmount;
    }

}


