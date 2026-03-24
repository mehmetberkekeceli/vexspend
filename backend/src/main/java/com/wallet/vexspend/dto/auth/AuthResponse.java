package com.wallet.vexspend.dto.auth;

import com.wallet.vexspend.dto.UserProfileResponse;

import java.time.Instant;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        UserProfileResponse user
) {
}



