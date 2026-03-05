package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.config.AllowedOriginsProvider;
import com.gaiotti.zenith.dto.request.CreateTransactionRequest;
import com.gaiotti.zenith.dto.request.UpdateTransactionRequest;
import com.gaiotti.zenith.dto.response.PageResponse;
import com.gaiotti.zenith.dto.response.TransactionResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AllowedOriginsProvider allowedOriginsProvider;

    @Test
    void listTransactions_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ledgers/1/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void listTransactions_WithTypeFilter_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type("EXPENSE")
                .date(LocalDate.now())
                .createdByUserId(1L)
                .createdByDisplayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        PageResponse<TransactionResponse> pageResponse = new PageResponse<>(
                List.of(response),
                0,
                20,
                1,
                1,
                true
        );

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.listTransactions(eq(1L), any(User.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/transactions")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"));
    }

    @Test
    @WithMockUser
    void listTransactions_Paginated_ReturnsCorrectPageMetadata() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type("EXPENSE")
                .date(LocalDate.now())
                .createdByUserId(1L)
                .createdByDisplayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        PageResponse<TransactionResponse> pageResponse = new PageResponse<>(
                List.of(response),
                0,
                10,
                25,
                3,
                false
        );

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.listTransactions(eq(1L), any(User.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @WithMockUser
    void listTransactions_NonMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.listTransactions(eq(1L), any(User.class), any(), any(), any(), any(), any(), any()))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(get("/api/v1/ledgers/1/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createTransaction_ValidRequest_Returns201() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type("EXPENSE")
                .date(LocalDate.now())
                .createdByUserId(1L)
                .createdByDisplayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.createTransaction(eq(1L), any(CreateTransactionRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/ledgers/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100,
                                    "type": "EXPENSE",
                                    "date": "2024-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    @WithMockUser
    void createTransaction_NullAmount_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/ledgers/1/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "EXPENSE",
                                    "date": "2024-01-15"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateTransaction_ValidRequest_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(200))
                .type("INCOME")
                .date(LocalDate.now())
                .createdByUserId(1L)
                .createdByDisplayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.updateTransaction(eq(1L), eq(1L), any(UpdateTransactionRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/ledgers/1/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 200,
                                    "type": "INCOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    @WithMockUser
    void deleteTransaction_Returns204() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(delete("/api/v1/ledgers/1/transactions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getMonthlyTransactions_ValidYearMonth_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type("EXPENSE")
                .date(LocalDate.of(2025, 3, 15))
                .createdByUserId(1L)
                .createdByDisplayName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.getMonthlyTransactions(eq(1L), any(), any(User.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ledgers/1/transactions/months/2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].date").value("2025-03-15"));
    }

    @Test
    @WithMockUser
    void getMonthlyTransactions_InvalidYearMonthFormat_Returns400() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/ledgers/1/transactions/months/not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void exportTransactions_Authorized_ReturnsXlsx() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.exportTransactionsXlsx(eq(1L), any(User.class), any(), any(), any()))
                .thenReturn(new byte[] {1, 2, 3});

        mockMvc.perform(get("/api/v1/ledgers/1/transactions/export.xlsx")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"ledger-1-transactions-2026-03-01-to-2026-03-31.xlsx\""));
    }

    @Test
    @WithMockUser
    void exportTransactions_NotMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(transactionService.exportTransactionsXlsx(eq(1L), any(User.class), any(), any(), any()))
                .thenThrow(new AccessDeniedException("You are not a member of this ledger"));

        mockMvc.perform(get("/api/v1/ledgers/1/transactions/export.xlsx"))
                .andExpect(status().isForbidden());
    }
}
