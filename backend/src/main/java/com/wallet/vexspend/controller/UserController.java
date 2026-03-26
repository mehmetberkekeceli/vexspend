package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.UserProfileResponse;
import com.wallet.vexspend.dto.user.UpdateMyProfileRequest;
import com.wallet.vexspend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserProfileResponse> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.updateCurrentUser(userId, request));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userService.deleteCurrentUser(userId);
        return ResponseEntity.noContent().build();
    }
}


