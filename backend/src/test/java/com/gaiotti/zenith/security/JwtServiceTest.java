package com.gaiotti.zenith.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "dGVzdFNlY3JldEtleVRoYXQBcyBBdCBsZWFzdCAyNTYgYml0cyBMb25n");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
        jwtService.initializeSigningKey();
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertTrue(jwtService.isAccessToken(token));
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtService.generateRefreshToken(1L, "test@example.com");

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void getEmailFromToken_Success() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");

        String email = jwtService.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void getUserIdFromToken_Success() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");

        Long userId = jwtService.getUserIdFromToken(token);

        assertEquals(1L, userId);
    }

    @Test
    void isTokenValid_InvalidToken() {
        boolean isValid = jwtService.isTokenValid("invalidToken");

        assertFalse(isValid);
    }

    @Test
    void isAccessToken_ValidAccessToken() {
        String token = jwtService.generateAccessToken(1L, "test@example.com");

        boolean isAccess = jwtService.isAccessToken(token);

        assertTrue(isAccess);
    }

    @Test
    void isAccessToken_RefreshToken() {
        String token = jwtService.generateRefreshToken(1L, "test@example.com");

        boolean isAccess = jwtService.isAccessToken(token);

        assertFalse(isAccess);
    }

    @Test
    void initializeSigningKey_InvalidBase64_ThrowsIllegalStateException() {
        JwtService invalidJwtService = new JwtService();
        ReflectionTestUtils.setField(invalidJwtService, "secretKey", "not-base64");

        assertThrows(IllegalStateException.class, invalidJwtService::initializeSigningKey);
    }
}
