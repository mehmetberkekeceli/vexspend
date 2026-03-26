package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.account.AccountResponse;
import com.wallet.vexspend.dto.account.CreateAccountRequest;
import com.wallet.vexspend.dto.account.UpdateAccountRequest;
import com.wallet.vexspend.entity.Account;
import com.wallet.vexspend.entity.AppUser;
import com.wallet.vexspend.exception.BusinessRuleException;
import com.wallet.vexspend.exception.ResourceConflictException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.AccountRepository;
import com.wallet.vexspend.repository.RecurringTransactionTemplateRepository;
import com.wallet.vexspend.repository.TransactionRepository;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionTemplateRepository recurringRepository;

    @Transactional
    public AccountResponse create(UUID userId, CreateAccountRequest request) {
        AppUser owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String name = normalizeName(request.name());
        if (accountRepository.existsByOwnerIdAndNameIgnoreCase(userId, name)) {
            throw new ResourceConflictException("Account with the same name already exists");
        }

        Account account = Account.builder()
                .owner(owner)
                .name(name)
                .type(request.type())
                .currencyCode(normalizeCurrency(request.currencyCode()))
                .currentBalance(normalizeBalance(request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance()))
                .active(true)
                .build();

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(UUID userId) {
        return accountRepository.findAllByOwnerIdOrderByNameAsc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID userId, UUID accountId) {
        return toResponse(requireOwnedAccount(userId, accountId));
    }

    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, UpdateAccountRequest request) {
        Account account = requireOwnedAccount(userId, accountId);

        if (request.name() == null && request.type() == null && request.currencyCode() == null
                && request.currentBalance() == null && request.active() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        if (request.name() != null) {
            String name = normalizeName(request.name());
            if (accountRepository.existsByOwnerIdAndNameIgnoreCaseAndIdNot(userId, name, accountId)) {
                throw new ResourceConflictException("Account with the same name already exists");
            }
            account.setName(name);
        }

        if (request.type() != null) {
            account.setType(request.type());
        }

        if (request.currencyCode() != null) {
            String currency = normalizeCurrency(request.currencyCode());
            if (!currency.equals(account.getCurrencyCode())
                    && (transactionRepository.existsByAccountId(accountId) || recurringRepository.existsByAccountId(accountId))) {
                throw new BusinessRuleException("Account currency cannot be changed after transactions or recurring templates are created");
            }
            account.setCurrencyCode(currency);
        }

        if (request.currentBalance() != null) {
            account.setCurrentBalance(normalizeBalance(request.currentBalance()));
        }

        if (request.active() != null) {
            account.setActive(request.active());
        }

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(UUID userId, UUID accountId) {
        Account account = requireOwnedAccount(userId, accountId);
        if (transactionRepository.existsByAccountId(accountId) || recurringRepository.existsByAccountId(accountId)) {
            throw new BusinessRuleException("Account cannot be deleted because transactions or recurring templates are linked to it");
        }
        accountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public Account requireOwnedAccount(UUID userId, UUID accountId) {
        return accountRepository.findByIdAndOwnerId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private String normalizeName(String name) {
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be blank");
        }
        return normalized;
    }

    private String normalizeCurrency(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal normalizeBalance(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getCurrencyCode(),
                account.getCurrentBalance(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}

