package com.wallet.vexspend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyProfileRequest {

    @Size(min = 3, max = 60)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
    private String username;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 120)
    private String fullName;

    @Size(max = 700)
    @Pattern(regexp = "^(|https?://.*)$", message = "Profile photo URL must start with http:// or https://")
    private String profilePhotoUrl;

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

}


