package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.recurring.CreateRecurringTransactionRequest;
import com.wallet.vexspend.dto.recurring.ProcessRecurringResult;
import com.wallet.vexspend.dto.recurring.RecurringTransactionResponse;
import com.wallet.vexspend.dto.recurring.UpdateRecurringTransactionRequest;
import com.wallet.vexspend.service.RecurringTransactionService;
import com.wallet.vexspend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
@Tag(name = "Recurring Transactions", description = "Recurring transaction template APIs")
@SecurityRequirement(name = "bearerAuth")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    @PostMapping
    @Operation(summary = "Create recurring transaction template")
    public ResponseEntity<RecurringTransactionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody CreateRecurringTransactionRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List recurring transaction templates")
    public ResponseEntity<List<RecurringTransactionResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(recurringService.list(userId));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get recurring transaction template by id")
    public ResponseEntity<RecurringTransactionResponse> get(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable UUID templateId) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(recurringService.get(userId, templateId));
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Update recurring transaction template")
    public ResponseEntity<RecurringTransactionResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID templateId,
                                                               @Valid @RequestBody UpdateRecurringTransactionRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(recurringService.update(userId, templateId, request));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Delete recurring transaction template")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID templateId) {
        UUID userId = SecurityUtils.userId(jwt);
        recurringService.delete(userId, templateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/process-due")
    @Operation(summary = "Process due recurring transactions and create real transactions")
    public ResponseEntity<ProcessRecurringResult> processDue(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(recurringService.processDue(userId, date));
    }
}

