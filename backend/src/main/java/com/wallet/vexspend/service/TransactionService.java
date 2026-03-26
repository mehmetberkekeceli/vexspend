package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.transaction.CreateTransactionRequest;
import com.wallet.vexspend.dto.transaction.TransactionResponse;
import com.wallet.vexspend.entity.Account;
import com.wallet.vexspend.entity.Budget;
import com.wallet.vexspend.entity.BudgetItem;
import com.wallet.vexspend.entity.Category;
import com.wallet.vexspend.entity.CategoryType;
import com.wallet.vexspend.entity.RecurringTransactionTemplate;
import com.wallet.vexspend.entity.Transaction;
import com.wallet.vexspend.entity.TransactionType;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.TransactionRepository;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final BudgetItemService budgetItemService;
    private final AccountService accountService;

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        Transaction transaction = createInternal(userId, request, null);
        return toResponse(transaction);
    }

    @Transactional
    public Transaction createFromRecurring(UUID userId, RecurringTransactionTemplate template, LocalDate executionDate) {
        CreateTransactionRequest request = new CreateTransactionRequest(
                template.getAccount().getId(),
                template.getType(),
                template.getAmount(),
                executionDate,
                template.getCategory().getId(),
                template.getBudget() == null ? null : template.getBudget().getId(),
                template.getBudgetItem() == null ? null : template.getBudgetItem().getId(),
                template.getMerchant(),
                template.getNote()
        );

        return createInternal(userId, request, template);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(UUID userId, LocalDate from, LocalDate to,
                                          TransactionType type, UUID budgetId, UUID categoryId, UUID accountId) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from date must be before or equal to to date");
        }

        Specification<Transaction> spec = (root, query, cb) -> cb.equal(root.get("owner").get("id"), userId);

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), to));
        }
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (budgetId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("budget").get("id"), budgetId));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (accountId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("account").get("id"), accountId));
        }

        Sort sort = Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt"));

        return transactionRepository.findAll(spec, sort).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndOwnerId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getType() == TransactionType.EXPENSE
                && transaction.getBudget() != null
                && transaction.getBudgetItem() != null) {

            BigDecimal budgetSpent = transaction.getBudget().getSpentAmount().subtract(transaction.getAmount());
            BigDecimal itemSpent = transaction.getBudgetItem().getSpentAmount().subtract(transaction.getAmount());

            transaction.getBudget().setSpentAmount(maxZero(budgetSpent));
            transaction.getBudgetItem().setSpentAmount(maxZero(itemSpent));
        }

        reverseAccountEffect(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        transactionRepository.delete(transaction);
    }

    private Transaction createInternal(UUID userId, CreateTransactionRequest request, RecurringTransactionTemplate template) {
        Category category = categoryService.requireOwnedCategory(userId, request.categoryId());
        Account account = accountService.requireOwnedAccount(userId, request.accountId());
        BigDecimal amount = normalizeAmount(request.amount());

        if (!account.isActive()) {
            throw new BusinessRuleException("Cannot create transaction on inactive account");
        }

        if ((request.type() == TransactionType.EXPENSE && category.getType() != CategoryType.EXPENSE)
                || (request.type() == TransactionType.INCOME && category.getType() != CategoryType.INCOME)) {
            throw new BusinessRuleException("Transaction type and category type must match");
        }

        Budget budget = null;
        BudgetItem budgetItem = null;

        if (request.type() == TransactionType.EXPENSE) {
            if (request.budgetId() == null || request.budgetItemId() == null) {
                throw new BusinessRuleException("Expense transaction requires budgetId and budgetItemId");
            }

            budget = budgetService.requireOwnedBudget(userId, request.budgetId());
            budgetItem = budgetItemService.requireBudgetItem(request.budgetId(), request.budgetItemId());

            if (!budgetItem.getCategory().getId().equals(category.getId())) {
                throw new BusinessRuleException("Transaction category must match budget item category");
            }

            if (!budget.getCurrencyCode().equalsIgnoreCase(account.getCurrencyCode())) {
                throw new BusinessRuleException("Budget currency and account currency must match");
            }

            if (request.transactionDate().isBefore(budget.getPeriodStart()) || request.transactionDate().isAfter(budget.getPeriodEnd())) {
                throw new BusinessRuleException("Transaction date must be inside budget period");
            }

            BigDecimal newBudgetSpent = budget.getSpentAmount().add(amount);
            if (newBudgetSpent.compareTo(budget.getTotalLimit()) > 0) {
                throw new BusinessRuleException("Budget limit exceeded");
            }

            BigDecimal newItemSpent = budgetItem.getSpentAmount().add(amount);
            if (newItemSpent.compareTo(budgetItem.getAllocatedAmount()) > 0) {
                throw new BusinessRuleException("Budget item allocation exceeded");
            }

            budget.setSpentAmount(newBudgetSpent.setScale(2, RoundingMode.HALF_UP));
            budgetItem.setSpentAmount(newItemSpent.setScale(2, RoundingMode.HALF_UP));
        } else {
            if (request.budgetId() != null || request.budgetItemId() != null) {
                throw new BusinessRuleException("Income transaction cannot be linked to budget or budget item");
            }
        }

        applyAccountEffect(account, request.type(), amount);

        Transaction transaction = Transaction.builder()
                .owner(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found")))
                .account(account)
                .budget(budget)
                .budgetItem(budgetItem)
                .category(category)
                .recurringTemplate(template)
                .type(request.type())
                .amount(amount)
                .transactionDate(request.transactionDate())
                .merchant(normalizeNullable(request.merchant()))
                .note(normalizeNullable(request.note()))
                .build();

        return transactionRepository.save(transaction);
    }

    private void applyAccountEffect(Account account, TransactionType type, BigDecimal amount) {
        BigDecimal balance = account.getCurrentBalance();
        if (type == TransactionType.EXPENSE) {
            balance = balance.subtract(amount);
        } else {
            balance = balance.add(amount);
        }
        account.setCurrentBalance(balance.setScale(2, RoundingMode.HALF_UP));
    }

    private void reverseAccountEffect(Account account, TransactionType type, BigDecimal amount) {
        BigDecimal balance = account.getCurrentBalance();
        if (type == TransactionType.EXPENSE) {
            balance = balance.add(amount);
        } else {
            balance = balance.subtract(amount);
        }
        account.setCurrentBalance(balance.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal maxZero(BigDecimal value) {
        return value.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
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

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getBudget() == null ? null : transaction.getBudget().getId(),
                transaction.getBudgetItem() == null ? null : transaction.getBudgetItem().getId(),
                transaction.getRecurringTemplate() == null ? null : transaction.getRecurringTemplate().getId(),
                transaction.getMerchant(),
                transaction.getNote(),
                transaction.getCreatedAt()
        );
    }
}

