package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.dto.request.LoginRequest;
import com.gaiotti.zenith.dto.request.RegisterRequest;
import com.gaiotti.zenith.dto.response.AuthResponse;
import com.gaiotti.zenith.repository.RefreshTokenRepository;
import com.gaiotti.zenith.security.AuthCookieService;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthCookieService authCookieService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDisplayName("Test User");

        AuthService.AuthSession session = buildSession("accessToken", "refreshToken");

        when(authService.register(any(RegisterRequest.class))).thenReturn(session);
        when(authCookieService.buildRefreshTokenCookie("refreshToken"))
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", "refreshToken").path("/").build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(header().string("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refreshToken")));
    }

    @Test
    void register_ValidationError() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthService.AuthSession session = buildSession("accessToken", "refreshToken");
        when(authService.login(any(LoginRequest.class))).thenReturn(session);
        when(authCookieService.buildRefreshTokenCookie("refreshToken"))
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", "refreshToken").path("/").build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(header().string("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refreshToken")));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_Success() throws Exception {
        AuthService.AuthSession session = buildSession("newAccessToken", "newRefreshToken");
        when(authCookieService.extractRefreshToken(any())).thenReturn(java.util.Optional.of("validRefreshToken"));
        when(authService.refresh("validRefreshToken")).thenReturn(session);
        when(authCookieService.buildRefreshTokenCookie("newRefreshToken"))
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", "newRefreshToken").path("/").build());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(header().string("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=newRefreshToken")));
    }

    @Test
    void refresh_MissingCookie_ReturnsBadRequest() throws Exception {
        when(authCookieService.extractRefreshToken(any())).thenReturn(java.util.Optional.empty());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_InvalidOrigin_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_ClearsCookie() throws Exception {
        when(authCookieService.extractRefreshToken(any())).thenReturn(java.util.Optional.of("refreshToken"));
        when(authCookieService.buildClearRefreshTokenCookie())
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", "").path("/").maxAge(0).build());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")));
    }

    @Test
    void logout_InvalidRefererOrigin_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.REFERER, "https://evil.example/attack")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private AuthService.AuthSession buildSession(String accessToken, String refreshToken) {
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .userId(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();
        return new AuthService.AuthSession(response, refreshToken);
    }
}
