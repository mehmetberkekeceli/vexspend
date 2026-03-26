package com.wallet.vexspend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.wallet.vexspend.dto.UserProfileResponse;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String tokenType;

    private String accessToken;

    private Instant expiresAt;

    private UserProfileResponse user;

    public String tokenType() {
        return tokenType;
    }

    public String accessToken() {
        return accessToken;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public UserProfileResponse user() {
        return user;
    }

}




