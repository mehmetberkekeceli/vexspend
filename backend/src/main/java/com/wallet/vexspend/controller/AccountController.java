package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.account.AccountResponse;
import com.wallet.vexspend.dto.account.CreateAccountRequest;
import com.wallet.vexspend.dto.account.UpdateAccountRequest;
import com.wallet.vexspend.service.AccountService;
import com.wallet.vexspend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Wallet/account management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create account")
    public ResponseEntity<AccountResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CreateAccountRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List accounts")
    public ResponseEntity<List<AccountResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(accountService.list(userId));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by id")
    public ResponseEntity<AccountResponse> get(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable UUID accountId) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(accountService.get(userId, accountId));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update account")
    public ResponseEntity<AccountResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID accountId,
                                                  @Valid @RequestBody UpdateAccountRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(accountService.update(userId, accountId, request));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete account")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID accountId) {
        UUID userId = SecurityUtils.userId(jwt);
        accountService.delete(userId, accountId);
        return ResponseEntity.noContent().build();
    }
}

