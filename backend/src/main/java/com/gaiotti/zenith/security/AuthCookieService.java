package com.gaiotti.zenith.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieService {

    @Value("${auth.refresh-cookie.name:refresh_token}")
    private String refreshCookieName;

    @Value("${auth.refresh-cookie.path:/api/v1/auth}")
    private String refreshCookiePath;

    @Value("${auth.refresh-cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Value("${auth.refresh-cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    @Value("${auth.refresh-cookie.domain:}")
    private String refreshCookieDomain;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(refreshCookiePath)
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs));

        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            cookieBuilder.domain(refreshCookieDomain);
        }

        return cookieBuilder.build();
    }

    public ResponseCookie buildClearRefreshTokenCookie() {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(refreshCookiePath)
                .maxAge(Duration.ZERO);

        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            cookieBuilder.domain(refreshCookieDomain);
        }

        return cookieBuilder.build();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> refreshCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
