package com.wallet.vexspend.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.entity.CategoryType;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;

    private String name;

    private CategoryType type;

    private String colorHex;

    private String icon;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

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

