package com.wallet.vexspend.service;

import com.wallet.vexspend.dto.auth.AuthResponse;
import com.wallet.vexspend.dto.auth.LoginRequest;
import com.wallet.vexspend.dto.auth.RegisterRequest;
import com.wallet.vexspend.entity.AppUser;
import com.wallet.vexspend.entity.Role;
import com.wallet.vexspend.entity.RoleName;
import com.wallet.vexspend.exception.ResourceConflictException;
import com.wallet.vexspend.exception.ResourceNotFoundException;
import com.wallet.vexspend.repository.RoleRepository;
import com.wallet.vexspend.repository.UserRepository;
import com.wallet.vexspend.security.UserPrincipal;
import com.wallet.vexspend.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserProfileMapper userProfileMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResourceConflictException("Username is already in use");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email is already in use");
        }

        Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        AppUser newUser = AppUser.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(defaultRole))
                .enabled(true)
                .build();

        AppUser savedUser = userRepository.save(newUser);
        UserPrincipal principal = UserPrincipal.from(savedUser);
        JwtTokenService.JwtToken jwtToken = jwtTokenService.generate(principal);

        return new AuthResponse("Bearer", jwtToken.accessToken(), jwtToken.expiresAt(), userProfileMapper.toResponse(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail().trim(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AppUser user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JwtTokenService.JwtToken jwtToken = jwtTokenService.generate(principal);
        return new AuthResponse("Bearer", jwtToken.accessToken(), jwtToken.expiresAt(), userProfileMapper.toResponse(user));
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}




