package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.dto.request.CreateLedgerRequest;
import com.gaiotti.zenith.dto.request.InviteUserRequest;
import com.gaiotti.zenith.dto.response.InvitationResponse;
import com.gaiotti.zenith.dto.response.LedgerResponse;
import com.gaiotti.zenith.dto.response.MemberResponse;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.RefreshTokenRepository;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.LedgerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerController.class)
@Import(SecurityConfig.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LedgerService ledgerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private AuthUtils authUtils;

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();
    }

    @Test
    @WithMockUser
    void createLedger_Success() throws Exception {
        User testUser = createTestUser();
        CreateLedgerRequest request = new CreateLedgerRequest();
        request.setName("My Ledger");

        LedgerResponse response = LedgerResponse.builder()
                .id(1L)
                .name("My Ledger")
                .createdAt(LocalDateTime.now())
                .members(List.of(MemberResponse.builder()
                        .userId(1L)
                        .email("test@example.com")
                        .displayName("Test User")
                        .joinedAt(LocalDateTime.now())
                        .build()))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.createLedger(eq("My Ledger"), any(User.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledgers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Ledger"));
    }

    @Test
    @WithMockUser
    void createLedger_ValidationError() throws Exception {
        CreateLedgerRequest request = new CreateLedgerRequest();

        mockMvc.perform(post("/api/v1/ledgers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getLedgerDetails_Success() throws Exception {
        User testUser = createTestUser();
        LedgerResponse response = LedgerResponse.builder()
                .id(1L)
                .name("My Ledger")
                .createdAt(LocalDateTime.now())
                .members(List.of(MemberResponse.builder()
                        .userId(1L)
                        .email("test@example.com")
                        .displayName("Test User")
                        .joinedAt(LocalDateTime.now())
                        .build()))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.getLedgerDetails(1L, testUser)).thenReturn(response);

        mockMvc.perform(get("/api/v1/ledgers/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Ledger"));
    }

    @Test
    @WithMockUser
    void getMyLedger_Success() throws Exception {
        User testUser = createTestUser();
        LedgerResponse response = LedgerResponse.builder()
                .id(1L)
                .name("My Ledger")
                .createdAt(LocalDateTime.now())
                .members(List.of(MemberResponse.builder()
                        .userId(1L)
                        .email("test@example.com")
                        .displayName("Test User")
                        .joinedAt(LocalDateTime.now())
                        .build()))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.getCurrentUserLedger(testUser)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/ledgers/me")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Ledger"));
    }

    @Test
    @WithMockUser
    void getMyLedger_NotFound() throws Exception {
        User testUser = createTestUser();
        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.getCurrentUserLedger(testUser)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ledgers/me")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void inviteUser_Success() throws Exception {
        User testUser = createTestUser();
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("invited@example.com");

        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .token("invite-token")
                .invitedEmail("invited@example.com")
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.inviteUser(eq(1L), any(User.class), eq("invited@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledgers/1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("invite-token"))
                .andExpect(jsonPath("$.invitedEmail").value("invited@example.com"));
    }

    @Test
    @WithMockUser
    void inviteUser_ValidationError() throws Exception {
        InviteUserRequest request = new InviteUserRequest();

        mockMvc.perform(post("/api/v1/ledgers/1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void acceptInvitation_Success() throws Exception {
        User testUser = createTestUser();
        LedgerResponse response = LedgerResponse.builder()
                .id(1L)
                .name("My Ledger")
                .createdAt(LocalDateTime.now())
                .members(List.of(MemberResponse.builder()
                        .userId(1L)
                        .email("test@example.com")
                        .displayName("Test User")
                        .joinedAt(LocalDateTime.now())
                        .build()))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.acceptInvitation("token123", testUser)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/ledgers/invitations/token123/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("My Ledger"));
    }

    @Test
    @WithMockUser
    void declineInvitation_Success() throws Exception {
        User testUser = createTestUser();
        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .token("token123")
                .invitedEmail("test@example.com")
                .status("DECLINED")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.declineInvitation("token123", testUser)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/ledgers/invitations/token123/decline")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    @WithMockUser
    void cancelInvitation_Success() throws Exception {
        User testUser = createTestUser();
        InvitationResponse response = InvitationResponse.builder()
                .id(1L)
                .token("token123")
                .invitedEmail("test@example.com")
                .status("CANCELED")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(ledgerService.cancelInvitation("token123", testUser)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/ledgers/invitations/token123/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}
