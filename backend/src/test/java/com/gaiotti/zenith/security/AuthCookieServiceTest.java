package com.gaiotti.zenith.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthCookieServiceTest {

    private AuthCookieService authCookieService;

    @BeforeEach
    void setUp() {
        authCookieService = new AuthCookieService();
        ReflectionTestUtils.setField(authCookieService, "refreshCookieName", "refresh_token");
        ReflectionTestUtils.setField(authCookieService, "refreshCookiePath", "/api/v1/auth");
        ReflectionTestUtils.setField(authCookieService, "refreshCookieSecure", true);
        ReflectionTestUtils.setField(authCookieService, "refreshCookieSameSite", "Strict");
        ReflectionTestUtils.setField(authCookieService, "refreshCookieDomain", "");
        ReflectionTestUtils.setField(authCookieService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void buildRefreshTokenCookie_SetsSecureFlags() {
        ResponseCookie cookie = authCookieService.buildRefreshTokenCookie("token-value");

        assertTrue(cookie.toString().contains("refresh_token=token-value"));
        assertTrue(cookie.toString().contains("HttpOnly"));
        assertTrue(cookie.toString().contains("Secure"));
        assertTrue(cookie.toString().contains("SameSite=Strict"));
        assertTrue(cookie.toString().contains("Path=/api/v1/auth"));
    }

    @Test
    void buildClearRefreshTokenCookie_ExpiresImmediately() {
        ResponseCookie cookie = authCookieService.buildClearRefreshTokenCookie();

        assertTrue(cookie.toString().contains("refresh_token="));
        assertTrue(cookie.toString().contains("Max-Age=0"));
    }

    @Test
    void extractRefreshToken_ReturnsTokenWhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("other", "x"),
                new Cookie("refresh_token", "token-value")
        });

        Optional<String> token = authCookieService.extractRefreshToken(request);
        assertTrue(token.isPresent());
        assertEquals("token-value", token.get());
    }

    @Test
    void extractRefreshToken_ReturnsEmptyWhenMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("other", "x")
        });

        Optional<String> token = authCookieService.extractRefreshToken(request);
        assertTrue(token.isEmpty());
    }
}
