package com.gaiotti.zenith.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaiotti.zenith.security.JwtAuthFilter;
import com.gaiotti.zenith.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsConfigurationSource_TrimsConfiguredOrigins() {
        SecurityConfig securityConfig = new SecurityConfig(
                new JwtAuthFilter(mock(JwtService.class)),
                mock(UserDetailsService.class),
                new ObjectMapper(),
                mock(Environment.class)
        );
        ReflectionTestUtils.setField(securityConfig, "corsAllowedOrigins", " http://localhost:3000, https://app.example.com ");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/auth/login"));

        assertNotNull(configuration);
        assertEquals(
                java.util.List.of("http://localhost:3000", "https://app.example.com"),
                configuration.getAllowedOrigins()
        );
        assertEquals(
                java.util.List.of("Authorization", "Content-Type", "X-Requested-With"),
                configuration.getAllowedHeaders()
        );
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
    }
}
