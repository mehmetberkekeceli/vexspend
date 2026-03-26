package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.budget.BudgetResponse;
import com.wallet.vexspend.dto.budget.CreateBudgetRequest;
import com.wallet.vexspend.dto.budget.UpdateBudgetRequest;
import com.wallet.vexspend.entity.AppUser;
import com.wallet.vexspend.entity.Budget;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.BudgetItemRepository;
import com.wallet.vexspend.repository.BudgetRepository;
import com.wallet.vexspend.repository.RecurringTransactionTemplateRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionTemplateRepository recurringRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetResponse create(UUID userId, CreateBudgetRequest request) {
        validatePeriod(request.periodStart(), request.periodEnd());

        AppUser owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Budget budget = Budget.builder()
                .owner(owner)
                .name(normalizeText(request.name()))
                .currencyCode(normalizeCurrency(request.currencyCode()))
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .totalLimit(normalizeAmount(request.totalLimit()))
                .spentAmount(BigDecimal.ZERO)
                .active(true)
                .build();

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(UUID userId) {
        return budgetRepository.findAllByOwnerIdOrderByPeriodStartDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetResponse get(UUID userId, UUID budgetId) {
        Budget budget = requireOwnedBudget(userId, budgetId);
        return toResponse(budget);
    }

    @Transactional
    public BudgetResponse update(UUID userId, UUID budgetId, UpdateBudgetRequest request) {
        Budget budget = requireOwnedBudget(userId, budgetId);

        if (request.name() == null && request.currencyCode() == null && request.periodStart() == null
                && request.periodEnd() == null && request.totalLimit() == null && request.active() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        LocalDate periodStart = request.periodStart() == null ? budget.getPeriodStart() : request.periodStart();
        LocalDate periodEnd = request.periodEnd() == null ? budget.getPeriodEnd() : request.periodEnd();
        validatePeriod(periodStart, periodEnd);

        BigDecimal totalLimit = request.totalLimit() == null ? budget.getTotalLimit() : normalizeAmount(request.totalLimit());

        if (totalLimit.compareTo(budget.getSpentAmount()) < 0) {
            throw new BusinessRuleException("Budget limit cannot be lower than current spent amount");
        }

        BigDecimal allocated = budgetItemRepository.sumAllocatedByBudgetId(budgetId);
        if (totalLimit.compareTo(allocated) < 0) {
            throw new BusinessRuleException("Budget limit cannot be lower than allocated budget item total");
        }

        if (request.name() != null) {
            budget.setName(normalizeText(request.name()));
        }
        if (request.currencyCode() != null) {
            budget.setCurrencyCode(normalizeCurrency(request.currencyCode()));
        }
        budget.setPeriodStart(periodStart);
        budget.setPeriodEnd(periodEnd);
        budget.setTotalLimit(totalLimit);
        if (request.active() != null) {
            budget.setActive(request.active());
        }

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(UUID userId, UUID budgetId) {
        Budget budget = requireOwnedBudget(userId, budgetId);

        if (transactionRepository.existsByBudgetId(budgetId) || recurringRepository.existsByBudgetId(budgetId)) {
            throw new BusinessRuleException("Budget cannot be deleted because transactions or recurring templates are linked to it");
        }

        budgetItemRepository.deleteAllByBudgetId(budgetId);
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public Budget requireOwnedBudget(UUID userId, UUID budgetId) {
        return budgetRepository.findByIdAndOwnerId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessRuleException("Budget period start must be before or equal to period end");
        }
    }

    private String normalizeText(String value) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Text value cannot be blank");
        }
        return normalized;
    }

    private String normalizeCurrency(String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal remaining = budget.getTotalLimit().subtract(budget.getSpentAmount()).setScale(2, RoundingMode.HALF_UP);

        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getCurrencyCode(),
                budget.getPeriodStart(),
                budget.getPeriodEnd(),
                budget.getTotalLimit(),
                budget.getSpentAmount(),
                remaining,
                budget.isActive(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}

