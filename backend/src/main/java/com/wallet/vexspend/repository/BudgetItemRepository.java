package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, UUID> {
    List<BudgetItem> findAllByBudgetIdOrderByNameAsc(UUID budgetId);

    List<BudgetItem> findAllByBudgetId(UUID budgetId);

    Optional<BudgetItem> findByIdAndBudgetId(UUID id, UUID budgetId);

    @Query("select coalesce(sum(i.allocatedAmount), 0) from BudgetItem i where i.budget.id = :budgetId")
    BigDecimal sumAllocatedByBudgetId(UUID budgetId);

    boolean existsByCategoryId(UUID categoryId);

    void deleteAllByBudgetId(UUID budgetId);
}
