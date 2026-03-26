package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Transaction> findAllByOwnerIdAndTransactionDateBetween(UUID ownerId, LocalDate from, LocalDate to);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByBudgetId(UUID budgetId);

    boolean existsByBudgetItemId(UUID budgetItemId);

    boolean existsByAccountId(UUID accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Transaction t set t.recurringTemplate = null where t.owner.id = :ownerId and t.recurringTemplate.id = :templateId")
    int clearRecurringTemplateReference(@Param("ownerId") UUID ownerId, @Param("templateId") UUID templateId);
}
