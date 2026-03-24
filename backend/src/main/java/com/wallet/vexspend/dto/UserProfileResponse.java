package com.wallet.vexspend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String profilePhotoUrl,
        boolean enabled,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}

