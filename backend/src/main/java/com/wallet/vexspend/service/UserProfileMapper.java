package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.UserProfileResponse;
import com.wallet.vexspend.entity.AppUser;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(AppUser user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted(Comparator.naturalOrder())
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getProfilePhotoUrl(),
                user.isEnabled(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

