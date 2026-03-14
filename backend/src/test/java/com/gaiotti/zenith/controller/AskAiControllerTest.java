package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.AllowedOriginsProvider;
import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.dto.response.AskAiResponse;
import com.gaiotti.zenith.dto.response.AskAiUsageResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.QuotaExceededException;
import com.gaiotti.zenith.exception.RateLimitExceededException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.ai.AskAiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AskAiController.class)
@Import(SecurityConfig.class)
class AskAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private AskAiService askAiService;

    @MockBean
    private AllowedOriginsProvider allowedOriginsProvider;

    @Test
    void ask_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Como economizar?\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void ask_ValidRequest_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("User")
                .build();

        AskAiResponse response = AskAiResponse.builder()
                .answer("Resposta")
                .contextLevelUsed(AskAiResponse.ContextLevel.SUMMARY)
                .disclaimer("Aviso")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(askAiService.ask(eq(1L), any(User.class), any(AskAiRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "Como economizar neste mes?",
                                  "yearMonth": "2026-03",
                                  "includeTransactions": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Resposta"))
                .andExpect(jsonPath("$.contextLevelUsed").value("SUMMARY"));
    }

    @Test
    @WithMockUser
    void ask_InvalidPayload_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "",
                                  "yearMonth": "03-2026"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void ask_NotLedgerMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(askAiService.ask(eq(1L), any(User.class), any(AskAiRequest.class), anyString()))
                .thenThrow(new AccessDeniedException("You are not a member of this ledger"));

        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Como economizar?\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void ask_RateLimitExceeded_Returns429() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(askAiService.ask(eq(1L), any(User.class), any(AskAiRequest.class), anyString()))
                .thenThrow(new RateLimitExceededException("AI rate limit exceeded for this user"));

        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Como economizar?\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser
    void ask_QuotaExceeded_Returns429() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(askAiService.ask(eq(1L), any(User.class), any(AskAiRequest.class), anyString()))
                .thenThrow(new QuotaExceededException("Daily AI quota exceeded for this user"));

        mockMvc.perform(post("/api/v1/ledgers/1/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Como economizar?\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser
    void usage_Authenticated_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("User")
                .build();

        AskAiUsageResponse usageResponse = AskAiUsageResponse.builder()
                .mode("local")
                .accessAllowed(true)
                .perUserPerMinuteLimit(8)
                .perIpPerMinuteLimit(20)
                .perUserDailyQuota(50)
                .perUserCurrentMinuteUsed(1)
                .perIpCurrentMinuteUsed(1)
                .perUserDailyUsed(3)
                .perUserDailyRemaining(47)
                .note("Modo local")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(askAiService.getUsage(eq(1L), any(User.class), anyString())).thenReturn(usageResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/ai/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("local"))
                .andExpect(jsonPath("$.perUserDailyRemaining").value(47));
    }
}
