package com.wallet.vexspend.repository;

import com.wallet.vexspend.entity.Role;
import com.wallet.vexspend.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}


