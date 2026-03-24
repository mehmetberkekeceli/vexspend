package com.wallet.vexspend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @Size(min = 3, max = 60)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
        String username,

        @Email
        @Size(max = 150)
        String email,

        @Size(max = 120)
        String fullName,

        @Size(max = 700)
        @Pattern(regexp = "^(|https?://.*)$", message = "Profile photo URL must start with http:// or https://")
        String profilePhotoUrl
) {
}
