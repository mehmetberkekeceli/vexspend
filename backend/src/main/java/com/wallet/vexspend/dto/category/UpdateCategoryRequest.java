package com.wallet.vexspend.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 80)
    private String name;

    @Pattern(regexp = "^(|#[0-9a-fA-F]{6})$", message = "Color must be HEX format like #22c55e")
    private String colorHex;

    @Size(max = 40)
    private String icon;

    private Boolean active;

    public String name() {
        return name;
    }

    public String colorHex() {
        return colorHex;
    }

    public String icon() {
        return icon;
    }

    public Boolean active() {
        return active;
    }

}


