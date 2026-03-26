package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByOwnerIdOrderByNameAsc(UUID ownerId);

    Optional<Account> findByIdAndOwnerId(UUID accountId, UUID ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);

    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(UUID ownerId, String name, UUID accountId);
}
