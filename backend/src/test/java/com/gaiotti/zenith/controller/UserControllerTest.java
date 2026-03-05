package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.config.AllowedOriginsProvider;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.AuthCookieService;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private AuthCookieService authCookieService;

    @MockBean
    private AllowedOriginsProvider allowedOriginsProvider;

    @Test
    void logout_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void logout_WithValidToken_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();
        
        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        doNothing().when(authService).logout(1L);
        when(authCookieService.buildClearRefreshTokenCookie())
                .thenReturn(org.springframework.http.ResponseCookie.from("|refresh_token", "").path("/").maxAge(0).build());

        mockMvc.perform(post("/api/v1/users/me/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")));
        
        verify(authService).logout(1L);
    }

    @Test
    @WithMockUser
    void logout_WithActiveTokens_RevokesTokensAndReturns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        doNothing().when(authService).logout(1L);
        when(authCookieService.buildClearRefreshTokenCookie())
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", "").path("/").maxAge(0).build());

        mockMvc.perform(post("/api/v1/users/me/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")));

        verify(authService).logout(1L);
    }
}
