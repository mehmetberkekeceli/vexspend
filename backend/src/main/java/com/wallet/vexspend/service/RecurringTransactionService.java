package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.recurring.CreateRecurringTransactionRequest;
import com.wallet.vexspend.dto.recurring.ProcessRecurringResult;
import com.wallet.vexspend.dto.recurring.RecurringTransactionResponse;
import com.wallet.vexspend.dto.recurring.UpdateRecurringTransactionRequest;
import com.wallet.vexspend.entity.Account;
import com.wallet.vexspend.entity.Budget;
import com.wallet.vexspend.entity.BudgetItem;
import com.wallet.vexspend.entity.Category;
import com.wallet.vexspend.entity.CategoryType;
import com.wallet.vexspend.entity.RecurringTransactionTemplate;
import com.wallet.vexspend.entity.RecurrenceFrequency;
import com.wallet.vexspend.entity.Transaction;
import com.wallet.vexspend.entity.TransactionType;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.RecurringTransactionTemplateRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionTemplateRepository recurringRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final BudgetItemService budgetItemService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public RecurringTransactionResponse create(UUID userId, CreateRecurringTransactionRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BusinessRuleException("endDate cannot be before startDate");
        }

        Account account = accountService.requireOwnedAccount(userId, request.accountId());
        Category category = categoryService.requireOwnedCategory(userId, request.categoryId());
        Budget budget = null;
        BudgetItem budgetItem = null;

        validateTypeAndCategory(request.type(), category.getType());

        if (request.type() == TransactionType.EXPENSE) {
            if (request.budgetId() == null || request.budgetItemId() == null) {
                throw new BusinessRuleException("Recurring expense requires budgetId and budgetItemId");
            }
            budget = budgetService.requireOwnedBudget(userId, request.budgetId());
            budgetItem = budgetItemService.requireBudgetItem(request.budgetId(), request.budgetItemId());
            validateBudgetLink(budget, budgetItem, category, account);
        } else {
            if (request.budgetId() != null || request.budgetItemId() != null) {
                throw new BusinessRuleException("Recurring income cannot be linked to budget or budget item");
            }
        }

        RecurringTransactionTemplate template = RecurringTransactionTemplate.builder()
                .owner(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .account(account)
                .category(category)
                .budget(budget)
                .budgetItem(budgetItem)
                .type(request.type())
                .amount(normalizeAmount(request.amount()))
                .frequency(request.frequency())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .nextExecutionDate(request.startDate())
                .merchant(normalizeNullable(request.merchant()))
                .note(normalizeNullable(request.note()))
                .active(true)
                .build();

        return toResponse(recurringRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> list(UUID userId) {
        return recurringRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse get(UUID userId, UUID templateId) {
        return toResponse(requireOwnedTemplate(userId, templateId));
    }

    @Transactional
    public RecurringTransactionResponse update(UUID userId, UUID templateId, UpdateRecurringTransactionRequest request) {
        RecurringTransactionTemplate template = requireOwnedTemplate(userId, templateId);

        if (request.accountId() == null && request.categoryId() == null && request.budgetId() == null
                && request.budgetItemId() == null && request.type() == null && request.amount() == null
                && request.frequency() == null && request.startDate() == null && request.endDate() == null
                && request.nextExecutionDate() == null && request.merchant() == null && request.note() == null
                && request.active() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        Account account = request.accountId() == null ? template.getAccount() : accountService.requireOwnedAccount(userId, request.accountId());
        Category category = request.categoryId() == null ? template.getCategory() : categoryService.requireOwnedCategory(userId, request.categoryId());
        TransactionType type = request.type() == null ? template.getType() : request.type();

        Budget budget = template.getBudget();
        BudgetItem budgetItem = template.getBudgetItem();

        if (type == TransactionType.EXPENSE) {
            UUID budgetId = request.budgetId() == null ? (budget == null ? null : budget.getId()) : request.budgetId();
            UUID budgetItemId = request.budgetItemId() == null ? (budgetItem == null ? null : budgetItem.getId()) : request.budgetItemId();

            if (budgetId == null || budgetItemId == null) {
                throw new BusinessRuleException("Recurring expense requires budgetId and budgetItemId");
            }

            budget = budgetService.requireOwnedBudget(userId, budgetId);
            budgetItem = budgetItemService.requireBudgetItem(budgetId, budgetItemId);
            validateBudgetLink(budget, budgetItem, category, account);
        } else {
            budget = null;
            budgetItem = null;
        }

        validateTypeAndCategory(type, category.getType());

        LocalDate startDate = request.startDate() == null ? template.getStartDate() : request.startDate();
        LocalDate endDate = request.endDate() == null ? template.getEndDate() : request.endDate();
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessRuleException("endDate cannot be before startDate");
        }

        if (request.accountId() != null) {
            template.setAccount(account);
        }
        if (request.categoryId() != null) {
            template.setCategory(category);
        }
        template.setBudget(budget);
        template.setBudgetItem(budgetItem);
        template.setType(type);

        if (request.amount() != null) {
            template.setAmount(normalizeAmount(request.amount()));
        }
        if (request.frequency() != null) {
            template.setFrequency(request.frequency());
        }

        template.setStartDate(startDate);
        template.setEndDate(endDate);

        if (request.nextExecutionDate() != null) {
            template.setNextExecutionDate(request.nextExecutionDate());
        } else if (request.startDate() != null && template.getNextExecutionDate().isBefore(startDate)) {
            template.setNextExecutionDate(startDate);
        }

        if (request.merchant() != null) {
            template.setMerchant(normalizeNullable(request.merchant()));
        }
        if (request.note() != null) {
            template.setNote(normalizeNullable(request.note()));
        }
        if (request.active() != null) {
            template.setActive(request.active());
        }

        return toResponse(recurringRepository.save(template));
    }

    @Transactional
    public void delete(UUID userId, UUID templateId) {
        RecurringTransactionTemplate template = requireOwnedTemplate(userId, templateId);
        transactionRepository.clearRecurringTemplateReference(userId, templateId);
        recurringRepository.delete(template);
    }

    @Transactional
    public ProcessRecurringResult processDue(UUID userId, LocalDate executionDate) {
        LocalDate effectiveDate = executionDate == null ? LocalDate.now() : executionDate;

        List<RecurringTransactionTemplate> dueTemplates = recurringRepository
                .findAllByOwnerIdAndActiveTrueAndNextExecutionDateLessThanEqualOrderByNextExecutionDateAsc(userId, effectiveDate);

        List<UUID> createdIds = new ArrayList<>();

        for (RecurringTransactionTemplate template : dueTemplates) {
            LocalDate nextDate = template.getNextExecutionDate();

            while (template.isActive() && !nextDate.isAfter(effectiveDate)) {
                if (template.getEndDate() != null && nextDate.isAfter(template.getEndDate())) {
                    template.setActive(false);
                    break;
                }

                Transaction transaction = transactionService.createFromRecurring(userId, template, nextDate);
                createdIds.add(transaction.getId());

                template.setLastExecutionDate(nextDate);
                nextDate = calculateNextDate(nextDate, template.getFrequency());
                template.setNextExecutionDate(nextDate);

                if (template.getEndDate() != null && nextDate.isAfter(template.getEndDate())) {
                    template.setActive(false);
                }
            }

            recurringRepository.save(template);
        }

        return new ProcessRecurringResult(effectiveDate, dueTemplates.size(), createdIds);
    }

    @Transactional(readOnly = true)
    public RecurringTransactionTemplate requireOwnedTemplate(UUID userId, UUID templateId) {
        return recurringRepository.findByIdAndOwnerId(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction template not found"));
    }

    private void validateTypeAndCategory(TransactionType transactionType, CategoryType categoryType) {
        if (transactionType == TransactionType.EXPENSE && categoryType != CategoryType.EXPENSE) {
            throw new BusinessRuleException("Expense recurring transaction must use EXPENSE category");
        }
        if (transactionType == TransactionType.INCOME && categoryType != CategoryType.INCOME) {
            throw new BusinessRuleException("Income recurring transaction must use INCOME category");
        }
    }

    private void validateBudgetLink(Budget budget, BudgetItem budgetItem, Category category, Account account) {
        if (!budgetItem.getCategory().getId().equals(category.getId())) {
            throw new BusinessRuleException("Budget item category must match recurring category");
        }
        if (!budget.getCurrencyCode().equalsIgnoreCase(account.getCurrencyCode())) {
            throw new BusinessRuleException("Budget currency and account currency must match");
        }
    }

    private LocalDate calculateNextDate(LocalDate date, RecurrenceFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return date.plusDays(1);
            case WEEKLY:
                return date.plusWeeks(1);
            case MONTHLY:
                return date.plusMonths(1);
            default:
                throw new IllegalArgumentException("Unsupported recurrence frequency");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private RecurringTransactionResponse toResponse(RecurringTransactionTemplate template) {
        return new RecurringTransactionResponse(
                template.getId(),
                template.getAccount().getId(),
                template.getAccount().getName(),
                template.getCategory().getId(),
                template.getCategory().getName(),
                template.getBudget() == null ? null : template.getBudget().getId(),
                template.getBudgetItem() == null ? null : template.getBudgetItem().getId(),
                template.getType(),
                template.getAmount(),
                template.getFrequency(),
                template.getStartDate(),
                template.getEndDate(),
                template.getNextExecutionDate(),
                template.getLastExecutionDate(),
                template.getMerchant(),
                template.getNote(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}

