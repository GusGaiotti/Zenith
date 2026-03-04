package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.LoginRequest;
import com.gaiotti.zenith.dto.request.RegisterRequest;
import com.gaiotti.zenith.dto.response.AuthResponse;
import com.gaiotti.zenith.model.RefreshToken;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.RefreshTokenRepository;
import com.gaiotti.zenith.repository.UserRepository;
import com.gaiotti.zenith.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    public record AuthSession(AuthResponse response, String refreshToken) {
    }

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthSession register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedDisplayName = request.getDisplayName().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(normalizedDisplayName)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
        return new AuthSession(response, refreshTokenValue);
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
        return new AuthSession(response, refreshTokenValue);
    }

    @Transactional
    public AuthSession refresh(String refreshTokenValue) {
        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (jwtService.isAccessToken(refreshTokenValue)) {
            throw new IllegalArgumentException("Cannot use access token as refresh token");
        }

        Long userIdFromToken = jwtService.getUserIdFromToken(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenForUpdate(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or revoked refresh token"));

        if (refreshToken.isRevoked()) {
            logout(userIdFromToken);
            throw new IllegalArgumentException("Security breach detected: token reuse attempt. All sessions have been invalidated.");
        }
        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        String email = jwtService.getEmailFromToken(refreshTokenValue);
        Long userId = jwtService.getUserIdFromToken(refreshTokenValue);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(userId, email);
        String newRefreshTokenValue = jwtService.generateRefreshToken(userId, email);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
        return new AuthSession(response, newRefreshTokenValue);
    }

    @Transactional
    public void logoutByRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .ifPresent(refreshToken -> logout(refreshToken.getUser().getId()));
    }

    @Transactional
    public void logout(Long userId) {
        List<RefreshToken> validTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);

        if (!validTokens.isEmpty()) {
            validTokens.forEach(token -> token.setRevoked(true));
            refreshTokenRepository.saveAll(validTokens);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
