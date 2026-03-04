package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.*;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User member;

    @BeforeEach
    void setUp() {
        member = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();
    }

    @Test
    void getDashboard_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getDashboard(99L, member))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getDashboard_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getDashboard(1L, member))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this ledger");
    }

    @Test
    void getDashboard_ReturnsCorrectTotals() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);

        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new BigDecimal("1200.50"));
        when(transactionRepository.sumExpensesByCategoryForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.<Object[]>of(new Object[]{10L, "Groceries", new BigDecimal("500.00")}));
        when(transactionRepository.sumExpensesByUserForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "user@example.com", new BigDecimal("800.00")}));

        DashboardResponse response = dashboardService.getDashboard(1L, member);

        assertThat(response.getTotalIncome()).isEqualByComparingTo("3000.00");
        assertThat(response.getTotalExpense()).isEqualByComparingTo("1200.50");
        assertThat(response.getExpenseByCategory()).hasSize(1);
        assertThat(response.getExpenseByUser()).hasSize(1);
    }

    @Test
    void getDashboard_NullTotalsFromDb_ReturnsZero() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(null);
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(null);
        when(transactionRepository.sumExpensesByCategoryForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of());
        when(transactionRepository.sumExpensesByUserForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(1L, member);

        assertThat(response.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getExpenseByCategory()).isEmpty();
        assertThat(response.getExpenseByUser()).isEmpty();
    }

    @Test
    void getDashboard_OnlyIncomeNoExpenses_CategoryAndUserBreakdownEmpty() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);

        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByCategoryForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of());
        when(transactionRepository.sumExpensesByUserForLedgerAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard(1L, member);

        assertThat(response.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(response.getTotalExpense()).isEqualByComparingTo("0");
        assertThat(response.getExpenseByUser()).isEmpty();
    }

    @Test
    void getOverview_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getOverview(99L, member))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getOverview_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getOverview(1L, member))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this ledger");
    }

    @Test
    void getOverview_ReturnsFinancialMetrics() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new BigDecimal("5000.00"), new BigDecimal("4500.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new BigDecimal("3200.00"), new BigDecimal("3000.00"));

        DashboardOverviewResponse response = dashboardService.getOverview(1L, member);

        assertThat(response.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(response.getTotalExpense()).isEqualByComparingTo("3200.00");
        assertThat(response.getNetBalance()).isEqualByComparingTo("1800.00");
    }

    @Test
    void getCoupleSplit_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getCoupleSplit(99L, member))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getCoupleSplit_ReturnsUserContributions() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        
        List<Object[]> dynamicsResults = new ArrayList<>();
        dynamicsResults.add(new Object[]{1L, "user1@example.com", "User One", new BigDecimal("3000.00"), new BigDecimal("1500.00")});
        dynamicsResults.add(new Object[]{2L, "user2@example.com", "User Two", new BigDecimal("2000.00"), new BigDecimal("1700.00")});
        when(transactionRepository.getCoupleDynamics(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(dynamicsResults);
        when(transactionRepository.getHighestTransaction(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new Object[]{new BigDecimal("500.00"), "User One"});

        DashboardCoupleSplitResponse response = dashboardService.getCoupleSplit(1L, member);

        assertThat(response.getUserContributions()).hasSize(2);
        assertThat(response.getHighestTransaction()).isNotNull();
        assertThat(response.getHighestTransaction().getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void getTrends_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getTrends(99L, member, 6))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getTrends_ReturnsMonthlyTrends() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        
        List<Object[]> trendResults = new ArrayList<>();
        trendResults.add(new Object[]{2026.0, 1.0, new BigDecimal("5000.00"), new BigDecimal("5000.00")});
        trendResults.add(new Object[]{2026.0, 2.0, new BigDecimal("5000.00"), new BigDecimal("4850.00")});
        trendResults.add(new Object[]{2026.0, 3.0, new BigDecimal("5000.00"), new BigDecimal("4500.00")});
        when(transactionRepository.getMonthlyTrends(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(trendResults);

        DashboardTrendsResponse response = dashboardService.getTrends(1L, member, 6);

        assertThat(response.getMonthlyTrends()).hasSize(3);
        assertThat(response.getOverallTrend()).isEqualTo(DashboardTrendsResponse.TrendDirection.IMPROVING);
    }

    @Test
    void getCategoriesBreakdown_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getCategoriesBreakdown(99L, member))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getCategoriesBreakdown_ReturnsCategoryDetails() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        
        List<Object[]> categoryResults = new ArrayList<>();
        categoryResults.add(new Object[]{1L, "Groceries", "#FF5733", new BigDecimal("500.00"), 10L, new BigDecimal("50.00")});
        categoryResults.add(new Object[]{2L, "Entertainment", "#33FF57", new BigDecimal("300.00"), 5L, new BigDecimal("60.00")});
        when(transactionRepository.getCategoryBreakdown(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(categoryResults);
        when(transactionRepository.getUncategorizedTotals(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(new Object[]{new BigDecimal("100.00"), 3L});

        DashboardCategoriesBreakdownResponse response = dashboardService.getCategoriesBreakdown(1L, member);

        assertThat(response.getCategories()).hasSize(2);
        assertThat(response.getTotalExpenses()).isEqualByComparingTo("900.00");
        assertThat(response.getTopCategory()).isNotNull();
        assertThat(response.getTopCategory().getName()).isEqualTo("Groceries");
    }

    @Test
    void getPulse_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getPulse(99L, member))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void getPulse_ReturnsDailySpending() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);

        YearMonth targetMonth = YearMonth.of(2026, 2);
        List<Object[]> dailyResults = new ArrayList<>();
        dailyResults.add(new Object[]{LocalDate.of(2026, 2, 26), new BigDecimal("150.00")});
        dailyResults.add(new Object[]{LocalDate.of(2026, 2, 27), new BigDecimal("200.00")});
        dailyResults.add(new Object[]{LocalDate.of(2026, 2, 28), new BigDecimal("100.00")});
        when(transactionRepository.getDailySpending(eq(1L), any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(dailyResults);

        DashboardPulseResponse response = dashboardService.getPulse(1L, member, targetMonth);

        assertThat(response.getDailySpending()).hasSize(targetMonth.lengthOfMonth());
        assertThat(response.getHighestSpendingDay()).isNotNull();
        assertThat(response.getSevenDayRollingAverage()).isEqualByComparingTo("64.29");
    }

    @Test
    void getOverview_InvalidMemberFilter_ThrowsAccessDeniedException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getOverview(1L, member, YearMonth.now(), 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Invalid member filter for this ledger");
    }

    @Test
    void getOverview_WithMemberFilter_ForwardsCreatedByToRepository() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 2L)).thenReturn(true);
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), eq(2L)))
                .thenReturn(new BigDecimal("1000.00"), new BigDecimal("800.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), eq(2L)))
                .thenReturn(new BigDecimal("500.00"), new BigDecimal("400.00"));

        dashboardService.getOverview(1L, member, YearMonth.now(), 2L);

        verify(transactionRepository, times(2)).sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class), eq(2L));
        verify(transactionRepository, times(2)).sumAmountByLedgerAndTypeAndDateRange(
                eq(1L), eq(Transaction.TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class), eq(2L));
    }
}
