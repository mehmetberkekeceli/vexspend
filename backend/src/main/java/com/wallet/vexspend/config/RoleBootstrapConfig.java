package com.wallet.vexspend.config;

import com.wallet.vexspend.entity.Role;
import com.wallet.vexspend.entity.RoleName;
import com.wallet.vexspend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoleBootstrapConfig {

    private final RoleRepository roleRepository;

    @Bean
    ApplicationRunner roleInitializer() {
        return args -> {
            seedRole(RoleName.ROLE_USER, "Standard application user");
            seedRole(RoleName.ROLE_ADMIN, "Administrator with elevated privileges");
        };
    }

    private void seedRole(RoleName roleName, String description) {
        roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(
                Role.builder()
                        .name(roleName)
                        .description(description)
                        .build()
        ));
    }
}


