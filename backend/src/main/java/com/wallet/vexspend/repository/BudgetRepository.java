package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findAllByOwnerIdOrderByPeriodStartDesc(UUID ownerId);

    Optional<Budget> findByIdAndOwnerId(UUID budgetId, UUID ownerId);

    boolean existsByIdAndOwnerId(UUID budgetId, UUID ownerId);
}
