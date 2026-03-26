package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.category.CategoryResponse;
import com.wallet.vexspend.dto.category.CreateCategoryRequest;
import com.wallet.vexspend.dto.category.UpdateCategoryRequest;
import com.wallet.vexspend.entity.AppUser;
import com.wallet.vexspend.entity.Category;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceConflictException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.BudgetItemRepository;
import com.wallet.vexspend.repository.CategoryRepository;
import com.wallet.vexspend.repository.RecurringTransactionTemplateRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionTemplateRepository recurringRepository;

    @Transactional
    public CategoryResponse create(UUID userId, CreateCategoryRequest request) {
        AppUser owner = getUser(userId);
        String name = normalizeName(request.name());

        if (categoryRepository.existsByOwnerIdAndNameIgnoreCaseAndType(userId, name, request.type())) {
            throw new ResourceConflictException("Category already exists for this type");
        }

        Category category = Category.builder()
                .owner(owner)
                .name(name)
                .type(request.type())
                .colorHex(normalizeNullable(request.colorHex()))
                .icon(normalizeNullable(request.icon()))
                .active(true)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(UUID userId) {
        return categoryRepository.findAllByOwnerIdOrderByNameAsc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndOwnerId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndOwnerId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.name() == null && request.colorHex() == null
                && request.icon() == null && request.active() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        String newName = request.name() == null ? null : normalizeName(request.name());

        if (newName != null && categoryRepository.existsByOwnerIdAndNameIgnoreCaseAndTypeAndIdNot(
                userId, newName, category.getType(), categoryId
        )) {
            throw new ResourceConflictException("Category already exists for this type");
        }

        if (newName != null) {
            category.setName(newName);
        }
        if (request.colorHex() != null) {
            category.setColorHex(normalizeNullable(request.colorHex()));
        }
        if (request.icon() != null) {
            category.setIcon(normalizeNullable(request.icon()));
        }
        if (request.active() != null) {
            category.setActive(request.active());
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndOwnerId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (budgetItemRepository.existsByCategoryId(categoryId)
                || transactionRepository.existsByCategoryId(categoryId)
                || recurringRepository.existsByCategoryId(categoryId)) {
            throw new BusinessRuleException("Category cannot be deleted because it is used by budget items, transactions or recurring templates");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category requireOwnedCategory(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndOwnerId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private AppUser getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeName(String name) {
        String value = name.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColorHex(),
                category.getIcon(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

