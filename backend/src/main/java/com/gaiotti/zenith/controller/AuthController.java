package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.dto.request.LoginRequest;
import com.gaiotti.zenith.dto.request.RegisterRequest;
import com.gaiotti.zenith.dto.response.AuthResponse;
import com.gaiotti.zenith.dto.response.MessageResponse;
import com.gaiotti.zenith.config.AllowedOriginsProvider;
import com.gaiotti.zenith.security.AuthCookieService;
import com.gaiotti.zenith.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTH_RESPONSE_CACHE_CONTROL = "no-store, no-cache, max-age=0, must-revalidate";

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final AllowedOriginsProvider allowedOriginsProvider;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthSession session = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL, AUTH_RESPONSE_CACHE_CONTROL)
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("Set-Cookie", authCookieService.buildRefreshTokenCookie(session.refreshToken()).toString())
                .body(session.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthSession session = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, AUTH_RESPONSE_CACHE_CONTROL)
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("Set-Cookie", authCookieService.buildRefreshTokenCookie(session.refreshToken()).toString())
                .body(session.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        validateBrowserOrigin(request);
        String refreshToken = authCookieService.extractRefreshToken(request)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token cookie is required"));
        AuthService.AuthSession session = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, AUTH_RESPONSE_CACHE_CONTROL)
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("Set-Cookie", authCookieService.buildRefreshTokenCookie(session.refreshToken()).toString())
                .body(session.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        validateBrowserOrigin(request);
        authCookieService.extractRefreshToken(request).ifPresent(authService::logoutByRefreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", authCookieService.buildClearRefreshTokenCookie().toString())
                .body(new MessageResponse("Logged out successfully"));
    }

    private void validateBrowserOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);

        if (origin != null && !origin.isBlank()) {
            if (!allowedOriginsProvider.isAllowedOrigin(origin)) {
                throw new IllegalArgumentException("Untrusted request origin");
            }
            return;
        }

        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null || referer.isBlank()) {
            return;
        }

        String refererOrigin = allowedOriginsProvider.extractOrigin(referer);
        if (refererOrigin == null || !allowedOriginsProvider.isAllowedOrigin(refererOrigin)) {
            throw new IllegalArgumentException("Untrusted request origin");
        }
    }
}
