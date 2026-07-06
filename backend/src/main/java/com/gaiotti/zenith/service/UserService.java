package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.UpdateProfileRequest;
import com.gaiotti.zenith.dto.response.UserProfileResponse;
import com.gaiotti.zenith.model.RefreshToken;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.PasswordResetTokenRepository;
import com.gaiotti.zenith.repository.RefreshTokenRepository;
import com.gaiotti.zenith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .aiEnabled(user.isAiEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Current password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user = userRepository.save(user);
        return getProfile(user);
    }

    @Transactional
    public void deleteAccount(User user) {
        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        List<RefreshToken> tokens =
                refreshTokenRepository.findAllByUserIdAndRevokedFalse(user.getId());
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);

        userRepository.delete(user);
    }
}
