package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.category.CategoryResponse;
import com.wallet.vexspend.dto.category.CreateCategoryRequest;
import com.wallet.vexspend.dto.category.UpdateCategoryRequest;
import com.wallet.vexspend.service.CategoryService;
import com.wallet.vexspend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody CreateCategoryRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List categories")
    public ResponseEntity<List<CategoryResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(categoryService.list(userId));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by id")
    public ResponseEntity<CategoryResponse> get(@AuthenticationPrincipal Jwt jwt,
                                                @PathVariable UUID categoryId) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(categoryService.get(userId, categoryId));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID categoryId,
                                                   @Valid @RequestBody UpdateCategoryRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(categoryService.update(userId, categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID categoryId) {
        UUID userId = SecurityUtils.userId(jwt);
        categoryService.delete(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}

