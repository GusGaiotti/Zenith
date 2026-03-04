package com.gaiotti.zenith.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, AttemptBucket> attemptsByClient = new ConcurrentHashMap<>();

    @Value("${auth.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${auth.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${auth.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !RATE_LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long now = Instant.now().toEpochMilli();
        long windowMillis = Math.max(1L, windowSeconds) * 1000L;
        String key = resolveClientKey(request);

        AttemptBucket bucket = attemptsByClient.computeIfAbsent(key, ignored -> new AttemptBucket(now));
        long retryAfterSeconds;

        synchronized (bucket) {
            if (now - bucket.windowStartedAt >= windowMillis) {
                bucket.windowStartedAt = now;
                bucket.attempts = 0;
            }

            if (bucket.attempts >= Math.max(1, maxAttempts)) {
                retryAfterSeconds = Math.max(1L, (windowMillis - (now - bucket.windowStartedAt) + 999L) / 1000L);
            } else {
                bucket.attempts++;
                cleanupExpiredBuckets(now, windowMillis);
                filterChain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        objectMapper.writeValue(response.getWriter(), Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", "Too many authentication attempts. Please try again shortly.",
                "path", request.getRequestURI()
        ));
    }

    private void cleanupExpiredBuckets(long now, long windowMillis) {
        if (attemptsByClient.size() < 1000) {
            return;
        }

        attemptsByClient.entrySet().removeIf(entry -> now - entry.getValue().windowStartedAt >= windowMillis);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String clientIp = request.getRemoteAddr();

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            clientIp = forwardedFor.split(",")[0].trim();
        }

        return clientIp + "|" + request.getRequestURI();
    }

    private static final class AttemptBucket {
        private long windowStartedAt;
        private int attempts;

        private AttemptBucket(long windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
            this.attempts = 0;
        }
    }
}
