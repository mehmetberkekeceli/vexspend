package com.wallet.vexspend.util;

import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID userId(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT subject");
        }
    }
}
