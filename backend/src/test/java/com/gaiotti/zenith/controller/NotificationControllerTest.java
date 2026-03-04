package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.dto.response.NotificationListResponse;
import com.gaiotti.zenith.dto.response.NotificationResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser
    void listNotifications_Returns200() throws Exception {
        User testUser = User.builder().id(1L).email("test@example.com").displayName("Test").build();

        NotificationListResponse response = NotificationListResponse.builder()
                .unreadCount(1)
                .items(List.of(NotificationResponse.builder()
                        .id(1L)
                        .type("TRANSACTION_CREATED")
                        .title("Nova transacao")
                        .body("Body")
                        .createdAt(LocalDateTime.now())
                        .build()))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(notificationService.listForUser(testUser, 7, true)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.items[0].type").value("TRANSACTION_CREATED"));
    }

    @Test
    @WithMockUser
    void markSeen_Returns200() throws Exception {
        User testUser = User.builder().id(1L).email("test@example.com").displayName("Test").build();
        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(patch("/api/v1/notifications/mark-seen")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "ids": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notifications marked as seen"));

        verify(notificationService).markSeen(eq(List.of(1L, 2L)), any(User.class));
    }

    @Test
    @WithMockUser
    void markSeen_WithEmptyIds_Returns400() throws Exception {
        User testUser = User.builder().id(1L).email("test@example.com").displayName("Test").build();
        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(patch("/api/v1/notifications/mark-seen")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "ids": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
