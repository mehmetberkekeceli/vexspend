package com.wallet.vexspend.controller;

import com.wallet.vexspend.dto.transaction.CreateTransactionRequest;
import com.wallet.vexspend.dto.transaction.TransactionResponse;
import com.wallet.vexspend.entity.TransactionType;
import com.wallet.vexspend.service.TransactionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create transaction")
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody CreateTransactionRequest request) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List transactions with optional filters")
    public ResponseEntity<List<TransactionResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID budgetId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId
    ) {
        UUID userId = SecurityUtils.userId(jwt);
        return ResponseEntity.ok(transactionService.list(userId, from, to, type, budgetId, categoryId, accountId));
    }

    @DeleteMapping("/{transactionId}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID transactionId) {
        UUID userId = SecurityUtils.userId(jwt);
        transactionService.delete(userId, transactionId);
        return ResponseEntity.noContent().build();
    }
}

