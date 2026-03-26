package com.wallet.vexspend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;

    private String username;

    private String email;

    private String fullName;

    private String profilePhotoUrl;

    private boolean enabled;

    private List<String> roles;

    private Instant createdAt;

    private Instant updatedAt;

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public String profilePhotoUrl() {
        return profilePhotoUrl;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<String> roles() {
        return roles;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

}


