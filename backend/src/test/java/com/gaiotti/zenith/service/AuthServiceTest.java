package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.request.LoginRequest;
import com.gaiotti.zenith.dto.request.RegisterRequest;
import com.gaiotti.zenith.dto.response.AuthResponse;
import com.gaiotti.zenith.model.RefreshToken;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.RefreshTokenRepository;
import com.gaiotti.zenith.repository.UserRepository;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.service.ai.AiAccessControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AiAccessControlService aiAccessControlService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setDisplayName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .displayName("Test User")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(aiAccessControlService.isAiAllowed(any(User.class))).thenReturn(true);

        AuthService.AuthSession session = authService.register(registerRequest);
        AuthResponse response = session.response();

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", session.refreshToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getDisplayName());

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_NormalizesEmailAndDisplayName() {
        registerRequest.setEmail("  TEST@EXAMPLE.COM ");
        registerRequest.setDisplayName("  Test User  ");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(aiAccessControlService.isAiAllowed(any(User.class))).thenReturn(true);

        authService.register(registerRequest);

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(argThat(user ->
                "test@example.com".equals(user.getEmail()) && "Test User".equals(user.getDisplayName())));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(aiAccessControlService.isAiAllowed(any(User.class))).thenReturn(true);

        AuthService.AuthSession session = authService.login(loginRequest);
        AuthResponse response = session.response();

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", session.refreshToken());
        assertEquals(1L, response.getUserId());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_NormalizesEmailBeforeAuthentication() {
        loginRequest.setEmail("  TEST@EXAMPLE.COM ");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(aiAccessControlService.isAiAllowed(any(User.class))).thenReturn(true);

        authService.login(loginRequest);

        verify(authenticationManager).authenticate(argThat(authentication -> authentication instanceof UsernamePasswordAuthenticationToken
                && "test@example.com".equals(authentication.getPrincipal())));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_InvalidCredentials() {
        doThrow(new BadCredentialsException("Invalid credentials"))
            .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_UserNotFound() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    void refresh_Success() {
        String refreshToken = "validRefreshToken";
        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .token(refreshToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        
        when(refreshTokenRepository.findByTokenForUpdate(refreshToken)).thenReturn(Optional.of(existingToken));
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);
        when(jwtService.getEmailFromToken(refreshToken)).thenReturn("test@example.com");
        when(jwtService.getUserIdFromToken(refreshToken)).thenReturn(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(anyLong(), anyString())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(anyLong(), anyString())).thenReturn("newRefreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(aiAccessControlService.isAiAllowed(any(User.class))).thenReturn(true);

        AuthService.AuthSession session = authService.refresh(refreshToken);
        AuthResponse response = session.response();

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", session.refreshToken());
        
        verify(refreshTokenRepository).save(argThat(token -> token.isRevoked()));
    }

    @Test
    void refresh_InvalidJwtToken_ThrowsIllegalArgumentException() {
        String refreshToken = "invalidToken";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(refreshToken));
        verify(refreshTokenRepository, never()).findByTokenForUpdate(anyString());
    }

    @Test
    void refresh_RevokedOrNotFoundInDb_ThrowsIllegalArgumentException() {
        String refreshToken = "revokedRefreshToken";
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);
        when(refreshTokenRepository.findByTokenForUpdate(refreshToken)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void refresh_ExpiredToken() {
        String refreshToken = "expiredRefreshToken";
        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .token(refreshToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();

        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);
        when(refreshTokenRepository.findByTokenForUpdate(refreshToken)).thenReturn(Optional.of(existingToken));

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void refresh_IsAccessToken() {
        String accessToken = "accessTokenAsRefresh";
        when(jwtService.isTokenValid(accessToken)).thenReturn(true);
        when(jwtService.isAccessToken(accessToken)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(accessToken));
        verify(refreshTokenRepository, never()).findByTokenForUpdate(anyString());
    }

    @Test
    void refresh_UserNotFound_ThrowsIllegalArgumentException() {
        String refreshToken = "validRefreshToken";
        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .token(refreshToken)
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);
        when(refreshTokenRepository.findByTokenForUpdate(refreshToken)).thenReturn(Optional.of(existingToken));
        when(jwtService.getEmailFromToken(refreshToken)).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void logoutByRefreshToken_WhenTokenExists_RevokesUserSessions() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("validRefreshToken")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("validRefreshToken"))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.findAllByUserIdAndRevokedFalse(1L))
                .thenReturn(java.util.List.of(refreshToken));

        authService.logoutByRefreshToken("validRefreshToken");

        verify(refreshTokenRepository).findByTokenAndRevokedFalse("validRefreshToken");
        verify(refreshTokenRepository).saveAll(anyList());
    }

    @Test
    void logoutByRefreshToken_WhenTokenMissing_DoesNothing() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("missingToken"))
                .thenReturn(Optional.empty());

        authService.logoutByRefreshToken("missingToken");

        verify(refreshTokenRepository).findByTokenAndRevokedFalse("missingToken");
        verify(refreshTokenRepository, never()).findAllByUserIdAndRevokedFalse(anyLong());
    }
}
