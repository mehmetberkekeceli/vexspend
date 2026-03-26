package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.budget.BudgetItemResponse;
import com.wallet.vexspend.dto.budget.BudgetResponse;
import com.wallet.vexspend.dto.budget.CreateBudgetItemRequest;
import com.wallet.vexspend.dto.budget.CreateBudgetRequest;
import com.wallet.vexspend.dto.budget.UpdateBudgetItemRequest;
import com.wallet.vexspend.dto.budget.UpdateBudgetRequest;
import com.wallet.vexspend.service.BudgetItemService;
import com.wallet.vexspend.service.BudgetService;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget and budget item management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetItemService budgetItemService;

    @PostMapping
    @Operation(summary = "Create budget")
    public ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody CreateBudgetRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List budgets")
    public ResponseEntity<List<BudgetResponse>> listBudgets(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(budgetService.list(userId));
    }

    @GetMapping("/{budgetId}")
    @Operation(summary = "Get budget by id")
    public ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID budgetId) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(budgetService.get(userId, budgetId));
    }

    @PutMapping("/{budgetId}")
    @Operation(summary = "Update budget")
    public ResponseEntity<BudgetResponse> updateBudget(@AuthenticationPrincipal Jwt jwt,
                                                       @PathVariable UUID budgetId,
                                                       @Valid @RequestBody UpdateBudgetRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(budgetService.update(userId, budgetId, request));
    }

    @DeleteMapping("/{budgetId}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable UUID budgetId) {
        UUID userId = SecurityUtils.userId(jwt);
        budgetService.delete(userId, budgetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{budgetId}/items")
    @Operation(summary = "Create budget item")
    public ResponseEntity<BudgetItemResponse> createBudgetItem(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID budgetId,
                                                               @Valid @RequestBody CreateBudgetItemRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetItemService.create(userId, budgetId, request));
    }

    @GetMapping("/{budgetId}/items")
    @Operation(summary = "List budget items")
    public ResponseEntity<List<BudgetItemResponse>> listBudgetItems(@AuthenticationPrincipal Jwt jwt,
                                                                    @PathVariable UUID budgetId) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(budgetItemService.list(userId, budgetId));
    }

    @PutMapping("/{budgetId}/items/{itemId}")
    @Operation(summary = "Update budget item")
    public ResponseEntity<BudgetItemResponse> updateBudgetItem(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID budgetId,
                                                               @PathVariable UUID itemId,
                                                               @Valid @RequestBody UpdateBudgetItemRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(budgetItemService.update(userId, budgetId, itemId, request));
    }

    @DeleteMapping("/{budgetId}/items/{itemId}")
    @Operation(summary = "Delete budget item")
    public ResponseEntity<Void> deleteBudgetItem(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable UUID budgetId,
                                                 @PathVariable UUID itemId) {
        UUID userId = SecurityUtils.userId(jwt);
        budgetItemService.delete(userId, budgetId, itemId);
        return ResponseEntity.noContent().build();
    }
}

