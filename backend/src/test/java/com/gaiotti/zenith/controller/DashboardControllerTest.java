package com.gaiotti.zenith.controller;

import com.gaiotti.zenith.config.SecurityConfig;
import com.gaiotti.zenith.dto.response.*;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.security.AuthUtils;
import com.gaiotti.zenith.security.JwtService;
import com.gaiotti.zenith.security.UserDetailsServiceImpl;
import com.gaiotti.zenith.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthUtils authUtils;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getDashboard_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ledgers/1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getDashboard_AuthenticatedMember_Returns200WithCorrectBody() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardResponse.CategoryBreakdown catBreakdown = DashboardResponse.CategoryBreakdown.builder()
                .categoryId(10L)
                .categoryName("Groceries")
                .totalAmount(new BigDecimal("500.00"))
                .build();

        DashboardResponse.UserBreakdown userBreakdown = DashboardResponse.UserBreakdown.builder()
                .userId(1L)
                .email("user@example.com")
                .totalAmount(new BigDecimal("500.00"))
                .build();

        DashboardResponse dashboardResponse = DashboardResponse.builder()
                .totalIncome(new BigDecimal("3000.00"))
                .totalExpense(new BigDecimal("500.00"))
                .expenseByCategory(List.of(catBreakdown))
                .expenseByUser(List.of(userBreakdown))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getDashboard(eq(1L), any(User.class), any(), any())).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpense").value(500.00))
                .andExpect(jsonPath("$.expenseByCategory[0].categoryId").value(10))
                .andExpect(jsonPath("$.expenseByCategory[0].categoryName").value("Groceries"))
                .andExpect(jsonPath("$.expenseByCategory[0].totalAmount").value(500.00))
                .andExpect(jsonPath("$.expenseByUser[0].userId").value(1))
                .andExpect(jsonPath("$.expenseByUser[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.expenseByUser[0].totalAmount").value(500.00));
    }

    @Test
    @WithMockUser
    void getDashboard_NonMember_Returns403() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getDashboard(eq(1L), any(User.class), any(), any()))
                .thenThrow(new AccessDeniedException("You are not a member of this ledger"));

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getDashboard_EmptyMonthReturnsZeroTotals() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardResponse emptyResponse = DashboardResponse.builder()
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .expenseByCategory(List.of())
                .expenseByUser(List.of())
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getDashboard(eq(1L), any(User.class), any(), any())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0))
                .andExpect(jsonPath("$.totalExpense").value(0))
                .andExpect(jsonPath("$.expenseByCategory").isArray())
                .andExpect(jsonPath("$.expenseByCategory").isEmpty())
                .andExpect(jsonPath("$.expenseByUser").isArray())
                .andExpect(jsonPath("$.expenseByUser").isEmpty());
    }

    @Test
    @WithMockUser
    void getOverview_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardOverviewResponse overviewResponse = DashboardOverviewResponse.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("3200.00"))
                .netBalance(new BigDecimal("1800.00"))
                .savingsRate(new BigDecimal("36.00"))
                .dailyBurnRate(new BigDecimal("114.29"))
                .projectedEndOfMonthExpense(new BigDecimal("3428.57"))
                .projectedEndOfMonthBalance(new BigDecimal("1571.43"))
                .monthOverMonthExpenseChange(new BigDecimal("6.67"))
                .monthOverMonthIncomeChange(new BigDecimal("11.11"))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getOverview(eq(1L), any(User.class), any(), any())).thenReturn(overviewResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpense").value(3200.00))
                .andExpect(jsonPath("$.netBalance").value(1800.00))
                .andExpect(jsonPath("$.savingsRate").value(36.00))
                .andExpect(jsonPath("$.dailyBurnRate").value(114.29))
                .andExpect(jsonPath("$.projectedEndOfMonthExpense").value(3428.57))
                .andExpect(jsonPath("$.monthOverMonthExpenseChange").value(6.67));
    }

    @Test
    @WithMockUser
    void getCoupleSplit_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardCoupleSplitResponse.UserContribution contribution1 = 
                DashboardCoupleSplitResponse.UserContribution.builder()
                .userId(1L)
                .email("user1@example.com")
                .displayName("User One")
                .totalIncome(new BigDecimal("3000.00"))
                .totalExpense(new BigDecimal("1500.00"))
                .incomePercentage(new BigDecimal("60.00"))
                .expensePercentage(new BigDecimal("50.00"))
                .netFairnessDelta(new BigDecimal("10.00"))
                .build();

        DashboardCoupleSplitResponse.HighestTransaction highestTx = 
                DashboardCoupleSplitResponse.HighestTransaction.builder()
                .amount(new BigDecimal("500.00"))
                .userDisplayName("User One")
                .build();

        DashboardCoupleSplitResponse splitResponse = DashboardCoupleSplitResponse.builder()
                .userContributions(List.of(contribution1))
                .highestTransaction(highestTx)
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getCoupleSplit(eq(1L), any(User.class), any(), any())).thenReturn(splitResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/couple-split"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userContributions[0].userId").value(1))
                .andExpect(jsonPath("$.userContributions[0].totalIncome").value(3000.00))
                .andExpect(jsonPath("$.userContributions[0].incomePercentage").value(60.00))
                .andExpect(jsonPath("$.highestTransaction.amount").value(500.00));
    }

    @Test
    @WithMockUser
    void getTrends_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardTrendsResponse.MonthlyTrend trend1 = DashboardTrendsResponse.MonthlyTrend.builder()
                .yearMonth("2026-01")
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("3000.00"))
                .net(new BigDecimal("2000.00"))
                .savingsRate(new BigDecimal("40.00"))
                .build();

        DashboardTrendsResponse trendsResponse = DashboardTrendsResponse.builder()
                .monthlyTrends(List.of(trend1))
                .overallTrend(DashboardTrendsResponse.TrendDirection.IMPROVING)
                .bestMonth(trend1)
                .worstMonth(trend1)
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getTrends(eq(1L), any(User.class), eq(6), any(), any())).thenReturn(trendsResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTrends[0].yearMonth").value("2026-01"))
                .andExpect(jsonPath("$.monthlyTrends[0].totalIncome").value(5000.00))
                .andExpect(jsonPath("$.overallTrend").value("IMPROVING"));
    }

    @Test
    @WithMockUser
    void getTrends_WithCustomMonths_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardTrendsResponse trendsResponse = DashboardTrendsResponse.builder()
                .monthlyTrends(List.of())
                .overallTrend(DashboardTrendsResponse.TrendDirection.STABLE)
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getTrends(eq(1L), any(User.class), eq(3), any(), any())).thenReturn(trendsResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/trends")
                        .param("months", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getOverview_WithMemberFilter_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardOverviewResponse overviewResponse = DashboardOverviewResponse.builder()
                .totalIncome(new BigDecimal("1200.00"))
                .totalExpense(new BigDecimal("500.00"))
                .netBalance(new BigDecimal("700.00"))
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getOverview(eq(1L), any(User.class), any(), eq(2L))).thenReturn(overviewResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/overview")
                        .param("createdByUserId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1200.00))
                .andExpect(jsonPath("$.totalExpense").value(500.00));
    }

    @Test
    @WithMockUser
    void getCategoriesBreakdown_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardCategoriesBreakdownResponse.CategoryDetail category = 
                DashboardCategoriesBreakdownResponse.CategoryDetail.builder()
                .categoryId(1L)
                .name("Groceries")
                .color("#FF5733")
                .totalSpent(new BigDecimal("500.00"))
                .percentageOfTotal(new BigDecimal("50.00"))
                .transactionCount(10)
                .averageTransactionValue(new BigDecimal("50.00"))
                .build();

        DashboardCategoriesBreakdownResponse categoriesResponse = 
                DashboardCategoriesBreakdownResponse.builder()
                .categories(List.of(category))
                .totalExpenses(new BigDecimal("1000.00"))
                .topCategory(category)
                .mostFrequentCategory("Groceries")
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getCategoriesBreakdown(eq(1L), any(User.class), any(), any())).thenReturn(categoriesResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/categories/breakdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Groceries"))
                .andExpect(jsonPath("$.categories[0].totalSpent").value(500.00))
                .andExpect(jsonPath("$.topCategory.name").value("Groceries"))
                .andExpect(jsonPath("$.mostFrequentCategory").value("Groceries"));
    }

    @Test
    @WithMockUser
    void getPulse_AuthenticatedMember_Returns200() throws Exception {
        User testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        DashboardPulseResponse.DailySpending daily = DashboardPulseResponse.DailySpending.builder()
                .date("2026-02-26")
                .totalExpense(new BigDecimal("150.00"))
                .build();

        DashboardPulseResponse.HighestSpendingDay highestDay = DashboardPulseResponse.HighestSpendingDay.builder()
                .date("2026-02-20")
                .amount(new BigDecimal("300.00"))
                .build();

        DashboardPulseResponse pulseResponse = DashboardPulseResponse.builder()
                .dailySpending(List.of(daily))
                .sevenDayRollingAverage(new BigDecimal("150.00"))
                .highestSpendingDay(highestDay)
                .zeroSpendingDays(5)
                .currentSpendingStreak(3)
                .build();

        when(authUtils.getAuthenticatedUser()).thenReturn(testUser);
        when(dashboardService.getPulse(eq(1L), any(User.class), any(), any())).thenReturn(pulseResponse);

        mockMvc.perform(get("/api/v1/ledgers/1/dashboard/pulse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sevenDayRollingAverage").value(150.00))
                .andExpect(jsonPath("$.highestSpendingDay.amount").value(300.00))
                .andExpect(jsonPath("$.zeroSpendingDays").value(5))
                .andExpect(jsonPath("$.currentSpendingStreak").value(3));
    }
}

