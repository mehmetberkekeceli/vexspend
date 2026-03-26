package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.UserProfileResponse;
import com.wallet.vexspend.dto.user.UpdateMyProfileRequest;
import com.wallet.vexspend.entity.AppUser;
import com.wallet.vexspend.exception.ResourceConflictException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(userProfileMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserProfileResponse updateCurrentUser(UUID id, UpdateMyProfileRequest request) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.username() == null && request.email() == null
                && request.fullName() == null && request.profilePhotoUrl() == null) {
            throw new IllegalArgumentException("At least one profile field must be provided");
        }

        String username = request.username() == null ? null : normalizeUsername(request.username());
        String email = request.email() == null ? null : normalizeEmail(request.email());
        String fullName = normalizeNullable(request.fullName());
        String profilePhotoUrl = normalizeNullable(request.profilePhotoUrl());

        if (username != null && userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ResourceConflictException("Username is already in use");
        }
        if (email != null && userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResourceConflictException("Email is already in use");
        }

        if (username != null) {
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (request.fullName() != null) {
            user.setFullName(fullName);
        }
        if (request.profilePhotoUrl() != null) {
            user.setProfilePhotoUrl(profilePhotoUrl);
        }

        return userProfileMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteCurrentUser(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
