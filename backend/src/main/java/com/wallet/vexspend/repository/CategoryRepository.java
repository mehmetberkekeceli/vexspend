package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.Category;
import com.wallet.vexspend.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByOwnerIdOrderByNameAsc(UUID ownerId);

    Optional<Category> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByOwnerIdAndNameIgnoreCaseAndType(UUID ownerId, String name, CategoryType type);

    boolean existsByOwnerIdAndNameIgnoreCaseAndTypeAndIdNot(UUID ownerId, String name, CategoryType type, UUID id);
}
