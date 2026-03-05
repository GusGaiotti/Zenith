package com.gaiotti.zenith.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AllowedOriginsProvider {

    private final Environment environment;

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins;

    private List<String> allowedOrigins;
    private Set<String> allowedOriginsSet;

    @PostConstruct
    void initialize() {
        boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));
        this.allowedOrigins = parseAllowedOrigins(corsAllowedOrigins, isProd);
        this.allowedOriginsSet = Set.copyOf(this.allowedOrigins);
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public boolean isAllowedOrigin(String origin) {
        String normalized = normalizeOrigin(origin);
        return normalized != null && allowedOriginsSet.contains(normalized);
    }

    public String extractOrigin(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }

            StringBuilder origin = new StringBuilder()
                    .append(uri.getScheme())
                    .append("://")
                    .append(uri.getHost());

            if (uri.getPort() != -1) {
                origin.append(":").append(uri.getPort());
            }

            return origin.toString();
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private List<String> parseAllowedOrigins(String rawOrigins, boolean isProd) {
        if (rawOrigins == null || rawOrigins.isBlank()) {
            if (isProd) {
                throw new IllegalStateException("CORS_ALLOWED_ORIGINS must be configured when the prod profile is active");
            }
            return List.of("http://localhost:3000");
        }

        List<String> origins = Arrays.stream(rawOrigins.split(","))
                .map(String::trim)
                .map(this::normalizeOrigin)
                .filter(origin -> origin != null && !origin.isEmpty())
                .peek(origin -> validateOrigin(origin))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));

        if (origins.isEmpty() && isProd) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS must contain at least one origin when the prod profile is active");
        }

        return origins;
    }

    private void validateOrigin(String origin) {
        if ("*".equals(origin)) {
            throw new IllegalStateException("Wildcard CORS origins are not allowed when credentials are enabled");
        }

        URI parsed = URI.create(origin);
        if (parsed.getScheme() == null || parsed.getHost() == null) {
            throw new IllegalStateException("CORS origin must include a valid scheme and host: " + origin);
        }

        if (!"http".equalsIgnoreCase(parsed.getScheme()) && !"https".equalsIgnoreCase(parsed.getScheme())) {
            throw new IllegalStateException("CORS origin must use http or https: " + origin);
        }
    }

    private String normalizeOrigin(String origin) {
        if (origin == null) {
            return null;
        }
        String normalized = origin.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
