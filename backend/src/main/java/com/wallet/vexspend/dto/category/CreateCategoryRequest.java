package com.wallet.vexspend.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.CategoryType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotNull
    private CategoryType type;

    @Pattern(regexp = "^(|#[0-9a-fA-F]{6})$", message = "Color must be HEX format like #22c55e")
    private String colorHex;

    @Size(max = 40)
    private String icon;

    public String name() {
        return name;
    }

    public CategoryType type() {
        return type;
    }

    public String colorHex() {
        return colorHex;
    }

    public String icon() {
        return icon;
    }

}


