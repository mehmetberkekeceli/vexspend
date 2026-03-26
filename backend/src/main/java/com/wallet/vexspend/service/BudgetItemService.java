package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.budget.BudgetItemResponse;
import com.wallet.vexspend.dto.budget.CreateBudgetItemRequest;
import com.wallet.vexspend.dto.budget.UpdateBudgetItemRequest;
import com.wallet.vexspend.entity.Budget;
import com.wallet.vexspend.entity.BudgetItem;
import com.wallet.vexspend.entity.Category;
import com.wallet.vexspend.entity.CategoryType;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.BudgetItemRepository;
import com.wallet.vexspend.repository.RecurringTransactionTemplateRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetItemService {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final BudgetItemRepository budgetItemRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionTemplateRepository recurringRepository;

    @Transactional
    public BudgetItemResponse create(UUID userId, UUID budgetId, CreateBudgetItemRequest request) {
        Budget budget = budgetService.requireOwnedBudget(userId, budgetId);
        Category category = categoryService.requireOwnedCategory(userId, request.categoryId());

        if (category.getType() != CategoryType.EXPENSE) {
            throw new BusinessRuleException("Budget items can only be linked to EXPENSE categories");
        }

        BigDecimal allocatedAmount = normalizeAmount(request.allocatedAmount());
        validateAllocationWithinBudget(budget, allocatedAmount, null);

        BudgetItem item = BudgetItem.builder()
                .budget(budget)
                .category(category)
                .name(normalizeName(request.name()))
                .allocatedAmount(allocatedAmount)
                .spentAmount(BigDecimal.ZERO)
                .build();

        return toResponse(budgetItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<BudgetItemResponse> list(UUID userId, UUID budgetId) {
        budgetService.requireOwnedBudget(userId, budgetId);
        return budgetItemRepository.findAllByBudgetIdOrderByNameAsc(budgetId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetItemResponse update(UUID userId, UUID budgetId, UUID itemId, UpdateBudgetItemRequest request) {
        Budget budget = budgetService.requireOwnedBudget(userId, budgetId);
        BudgetItem item = budgetItemRepository.findByIdAndBudgetId(itemId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget item not found"));

        if (request.name() == null && request.categoryId() == null && request.allocatedAmount() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        Category category = item.getCategory();
        if (request.categoryId() != null) {
            category = categoryService.requireOwnedCategory(userId, request.categoryId());
            if (category.getType() != CategoryType.EXPENSE) {
                throw new BusinessRuleException("Budget items can only be linked to EXPENSE categories");
            }
        }

        BigDecimal newAllocated = request.allocatedAmount() == null ? item.getAllocatedAmount() : normalizeAmount(request.allocatedAmount());
        if (newAllocated.compareTo(item.getSpentAmount()) < 0) {
            throw new BusinessRuleException("Allocated amount cannot be lower than spent amount");
        }

        validateAllocationWithinBudget(budget, newAllocated, item);

        if (request.name() != null) {
            item.setName(normalizeName(request.name()));
        }
        item.setCategory(category);
        item.setAllocatedAmount(newAllocated);

        return toResponse(budgetItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID userId, UUID budgetId, UUID itemId) {
        budgetService.requireOwnedBudget(userId, budgetId);
        BudgetItem item = budgetItemRepository.findByIdAndBudgetId(itemId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget item not found"));

        if (item.getSpentAmount().compareTo(BigDecimal.ZERO) > 0
                || transactionRepository.existsByBudgetItemId(itemId)
                || recurringRepository.existsByBudgetItemId(itemId)) {
            throw new BusinessRuleException("Budget item cannot be deleted because transactions or recurring templates are linked to it");
        }

        budgetItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public BudgetItem requireBudgetItem(UUID budgetId, UUID itemId) {
        return budgetItemRepository.findByIdAndBudgetId(itemId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget item not found"));
    }

    private void validateAllocationWithinBudget(Budget budget, BigDecimal targetAllocation, BudgetItem currentItem) {
        BigDecimal allocatedTotal = budgetItemRepository.sumAllocatedByBudgetId(budget.getId());

        if (currentItem != null) {
            allocatedTotal = allocatedTotal.subtract(currentItem.getAllocatedAmount());
        }

        BigDecimal finalAllocated = allocatedTotal.add(targetAllocation);
        if (finalAllocated.compareTo(budget.getTotalLimit()) > 0) {
            throw new BusinessRuleException("Budget item allocation exceeds budget total limit");
        }
    }

    private String normalizeName(String name) {
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Budget item name cannot be blank");
        }
        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BudgetItemResponse toResponse(BudgetItem item) {
        BigDecimal remaining = item.getAllocatedAmount().subtract(item.getSpentAmount()).setScale(2, RoundingMode.HALF_UP);

        return new BudgetItemResponse(
                item.getId(),
                item.getBudget().getId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getName(),
                item.getAllocatedAmount(),
                item.getSpentAmount(),
                remaining,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}

