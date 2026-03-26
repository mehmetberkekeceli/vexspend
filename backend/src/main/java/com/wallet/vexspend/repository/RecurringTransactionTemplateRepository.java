package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.RecurringTransactionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionTemplateRepository extends JpaRepository<RecurringTransactionTemplate, UUID> {
    List<RecurringTransactionTemplate> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<RecurringTransactionTemplate> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<RecurringTransactionTemplate> findAllByOwnerIdAndActiveTrueAndNextExecutionDateLessThanEqualOrderByNextExecutionDateAsc(
            UUID ownerId,
            LocalDate executionDate
    );

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByBudgetId(UUID budgetId);

    boolean existsByBudgetItemId(UUID budgetItemId);

    boolean existsByAccountId(UUID accountId);
}
