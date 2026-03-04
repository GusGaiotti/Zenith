package com.gaiotti.zenith.service;

import com.gaiotti.zenith.dto.response.*;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TREND_THRESHOLD = new BigDecimal("10");

    public DashboardResponse getDashboard(Long ledgerId, User authenticatedUser) {
        return getDashboard(ledgerId, authenticatedUser, null, null);
    }

    public DashboardResponse getDashboard(Long ledgerId, User authenticatedUser, YearMonth targetMonth) {
        return getDashboard(ledgerId, authenticatedUser, targetMonth, null);
    }

    public DashboardResponse getDashboard(Long ledgerId, User authenticatedUser, YearMonth targetMonth, Long createdByUserId) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        YearMonth currentMonth = resolveMonth(targetMonth);
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal totalIncome = nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.INCOME, startDate, endDate, createdByUserId
        ));
        BigDecimal totalExpense = abs(nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.EXPENSE, startDate, endDate, createdByUserId
        )));

        List<DashboardResponse.CategoryBreakdown> expenseByCategory = transactionRepository
                .sumExpensesByCategoryForLedgerAndDateRange(ledgerId, startDate, endDate, createdByUserId)
                .stream()
                .map(row -> DashboardResponse.CategoryBreakdown.builder()
                        .categoryId(((Number) row[0]).longValue())
                        .categoryName((String) row[1])
                        .totalAmount(abs(toBigDecimal(row[2])))
                        .build())
                .toList();

        List<DashboardResponse.UserBreakdown> expenseByUser = transactionRepository
                .sumExpensesByUserForLedgerAndDateRange(ledgerId, startDate, endDate, createdByUserId)
                .stream()
                .map(row -> DashboardResponse.UserBreakdown.builder()
                        .userId(((Number) row[0]).longValue())
                        .email((String) row[1])
                        .totalAmount(abs(toBigDecimal(row[2])))
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .expenseByCategory(expenseByCategory)
                .expenseByUser(expenseByUser)
                .build();
    }

    public DashboardOverviewResponse getOverview(Long ledgerId, User authenticatedUser) {
        return getOverview(ledgerId, authenticatedUser, null, null);
    }

    public DashboardOverviewResponse getOverview(Long ledgerId, User authenticatedUser, YearMonth targetMonth) {
        return getOverview(ledgerId, authenticatedUser, targetMonth, null);
    }

    public DashboardOverviewResponse getOverview(
            Long ledgerId,
            User authenticatedUser,
            YearMonth targetMonth,
            Long createdByUserId
    ) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        YearMonth currentMonth = resolveMonth(targetMonth);
        LocalDate currentStart = currentMonth.atDay(1);
        LocalDate currentEnd = currentMonth.atEndOfMonth();

        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate prevStart = previousMonth.atDay(1);
        LocalDate prevEnd = previousMonth.atEndOfMonth();

        BigDecimal totalIncome = nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.INCOME, currentStart, currentEnd, createdByUserId
        ));
        BigDecimal totalExpense = abs(nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.EXPENSE, currentStart, currentEnd, createdByUserId
        )));

        BigDecimal prevIncome = nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.INCOME, prevStart, prevEnd, createdByUserId
        ));
        BigDecimal prevExpense = abs(nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId, Transaction.TransactionType.EXPENSE, prevStart, prevEnd, createdByUserId
        )));

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netBalance.multiply(HUNDRED).divide(totalIncome, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate today = LocalDate.now();
        LocalDate burnRateEndDate = currentMonth.equals(YearMonth.from(today)) ? today : currentEnd;
        long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(currentStart, burnRateEndDate) + 1;
        BigDecimal dailyBurnRate = daysElapsed > 0 
                ? totalExpense.divide(BigDecimal.valueOf(daysElapsed), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long daysInMonth = currentMonth.lengthOfMonth();
        BigDecimal projectedEndOfMonthExpense = dailyBurnRate.multiply(BigDecimal.valueOf(daysInMonth));

        BigDecimal projectedEndOfMonthBalance = totalIncome.subtract(projectedEndOfMonthExpense);

        BigDecimal monthOverMonthExpenseChange = BigDecimal.ZERO;
        if (prevExpense.compareTo(BigDecimal.ZERO) > 0) {
            monthOverMonthExpenseChange = totalExpense.subtract(prevExpense)
                    .multiply(HUNDRED)
                    .divide(prevExpense, 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthOverMonthIncomeChange = BigDecimal.ZERO;
        if (prevIncome.compareTo(BigDecimal.ZERO) > 0) {
            monthOverMonthIncomeChange = totalIncome.subtract(prevIncome)
                    .multiply(HUNDRED)
                    .divide(prevIncome, 2, RoundingMode.HALF_UP);
        }

        return DashboardOverviewResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .savingsRate(savingsRate)
                .dailyBurnRate(dailyBurnRate)
                .projectedEndOfMonthExpense(projectedEndOfMonthExpense)
                .projectedEndOfMonthBalance(projectedEndOfMonthBalance)
                .monthOverMonthExpenseChange(monthOverMonthExpenseChange)
                .monthOverMonthIncomeChange(monthOverMonthIncomeChange)
                .build();
    }

    public DashboardCoupleSplitResponse getCoupleSplit(Long ledgerId, User authenticatedUser) {
        return getCoupleSplit(ledgerId, authenticatedUser, null, null);
    }

    public DashboardCoupleSplitResponse getCoupleSplit(Long ledgerId, User authenticatedUser, YearMonth targetMonth) {
        return getCoupleSplit(ledgerId, authenticatedUser, targetMonth, null);
    }

    public DashboardCoupleSplitResponse getCoupleSplit(
            Long ledgerId,
            User authenticatedUser,
            YearMonth targetMonth,
            Long createdByUserId
    ) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        YearMonth currentMonth = resolveMonth(targetMonth);
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Object[]> results = transactionRepository.getCoupleDynamics(ledgerId, startDate, endDate, createdByUserId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] row : results) {
            BigDecimal income = toBigDecimal(row[3]);
            BigDecimal expense = toBigDecimal(row[4]);
            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(expense);
        }

        List<DashboardCoupleSplitResponse.UserContribution> contributions = new ArrayList<>();
        for (Object[] row : results) {
            Long userId = ((Number) row[0]).longValue();
            String email = (String) row[1];
            String displayName = (String) row[2];
            BigDecimal income = toBigDecimal(row[3]);
            BigDecimal expense = toBigDecimal(row[4]);

            BigDecimal incomePercentage = totalIncome.compareTo(BigDecimal.ZERO) > 0
                    ? income.multiply(HUNDRED).divide(totalIncome, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal expensePercentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? expense.multiply(HUNDRED).divide(totalExpense, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal netFairnessDelta = incomePercentage.subtract(expensePercentage);

            contributions.add(DashboardCoupleSplitResponse.UserContribution.builder()
                    .userId(userId)
                    .email(email)
                    .displayName(displayName)
                    .totalIncome(income)
                    .totalExpense(expense)
                    .incomePercentage(incomePercentage)
                    .expensePercentage(expensePercentage)
                    .netFairnessDelta(netFairnessDelta)
                    .build());
        }

        Object[] highestTx = transactionRepository.getHighestTransaction(ledgerId, startDate, endDate, createdByUserId);
        DashboardCoupleSplitResponse.HighestTransaction highestTransaction = null;
        if (highestTx != null && highestTx.length >= 2 && highestTx[0] != null) {
            highestTransaction = DashboardCoupleSplitResponse.HighestTransaction.builder()
                    .amount(toBigDecimal(highestTx[0]))
                    .userDisplayName((String) highestTx[1])
                    .build();
        }

        return DashboardCoupleSplitResponse.builder()
                .userContributions(contributions)
                .highestTransaction(highestTransaction)
                .build();
    }

    public DashboardTrendsResponse getTrends(Long ledgerId, User authenticatedUser, int months) {
        return getTrends(ledgerId, authenticatedUser, months, null, null);
    }

    public DashboardTrendsResponse getTrends(Long ledgerId, User authenticatedUser, int months, YearMonth endMonth) {
        return getTrends(ledgerId, authenticatedUser, months, endMonth, null);
    }

    public DashboardTrendsResponse getTrends(
            Long ledgerId,
            User authenticatedUser,
            int months,
            YearMonth endMonth,
            Long createdByUserId
    ) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        int effectiveMonths = Math.min(Math.max(months, 1), 12);
        YearMonth currentMonth = resolveMonth(endMonth);
        LocalDate startDate = currentMonth.minusMonths(effectiveMonths - 1).atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Object[]> results = transactionRepository.getMonthlyTrends(ledgerId, startDate, endDate, createdByUserId);

        List<DashboardTrendsResponse.MonthlyTrend> trends = new ArrayList<>();
        BigDecimal bestNet = null;
        BigDecimal worstNet = null;
        DashboardTrendsResponse.MonthlyTrend bestMonth = null;
        DashboardTrendsResponse.MonthlyTrend worstMonth = null;

        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal income = toBigDecimal(row[2]);
            BigDecimal expense = toBigDecimal(row[3]);

            BigDecimal net = income.subtract(expense);
            BigDecimal savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                    ? net.multiply(HUNDRED).divide(income, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String yearMonth = String.format("%d-%02d", year, month);

            DashboardTrendsResponse.MonthlyTrend trend = DashboardTrendsResponse.MonthlyTrend.builder()
                    .yearMonth(yearMonth)
                    .totalIncome(income)
                    .totalExpense(expense)
                    .net(net)
                    .savingsRate(savingsRate)
                    .build();

            trends.add(trend);

            if (bestNet == null || net.compareTo(bestNet) > 0) {
                bestNet = net;
                bestMonth = trend;
            }
            if (worstNet == null || net.compareTo(worstNet) < 0) {
                worstNet = net;
                worstMonth = trend;
            }
        }

        DashboardTrendsResponse.TrendDirection overallTrend = DashboardTrendsResponse.TrendDirection.STABLE;
        if (trends.size() >= 3) {
            int size = trends.size();
            BigDecimal recentNet = trends.get(size - 1).getNet();
            BigDecimal previousNet = trends.get(size - 2).getNet();
            BigDecimal olderNet = trends.get(size - 3).getNet();

            BigDecimal avgRecent = recentNet.add(previousNet).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal avgOlder = previousNet.add(olderNet).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            BigDecimal trendDelta = avgRecent.subtract(avgOlder);
            if (trendDelta.compareTo(TREND_THRESHOLD) > 0) {
                overallTrend = DashboardTrendsResponse.TrendDirection.IMPROVING;
            } else if (trendDelta.compareTo(TREND_THRESHOLD.negate()) < 0) {
                overallTrend = DashboardTrendsResponse.TrendDirection.DECLINING;
            }
        }

        return DashboardTrendsResponse.builder()
                .monthlyTrends(trends)
                .overallTrend(overallTrend)
                .bestMonth(bestMonth)
                .worstMonth(worstMonth)
                .build();
    }

    public DashboardCategoriesBreakdownResponse getCategoriesBreakdown(Long ledgerId, User authenticatedUser) {
        return getCategoriesBreakdown(ledgerId, authenticatedUser, null, null);
    }

    public DashboardCategoriesBreakdownResponse getCategoriesBreakdown(Long ledgerId, User authenticatedUser, YearMonth targetMonth) {
        return getCategoriesBreakdown(ledgerId, authenticatedUser, targetMonth, null);
    }

    public DashboardCategoriesBreakdownResponse getCategoriesBreakdown(
            Long ledgerId,
            User authenticatedUser,
            YearMonth targetMonth,
            Long createdByUserId
    ) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        YearMonth currentMonth = resolveMonth(targetMonth);
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        List<Object[]> categoryResults = transactionRepository.getCategoryBreakdown(ledgerId, startDate, endDate, createdByUserId);
        Object[] uncategorized = transactionRepository.getUncategorizedTotals(ledgerId, startDate, endDate, createdByUserId);

        BigDecimal uncategorizedTotal = BigDecimal.ZERO;
        Integer uncategorizedCount = 0;
        if (uncategorized != null && uncategorized.length >= 2) {
            uncategorizedTotal = abs(toBigDecimal(uncategorized[0]));
            uncategorizedCount = uncategorized[1] != null ? ((Number) uncategorized[1]).intValue() : 0;
        }

        BigDecimal categorizedExpenses = BigDecimal.ZERO;
        for (Object[] row : categoryResults) {
            BigDecimal spent = abs(toBigDecimal(row[3]));
            categorizedExpenses = categorizedExpenses.add(spent);
        }
        BigDecimal totalExpenses = categorizedExpenses.add(uncategorizedTotal);

        List<DashboardCategoriesBreakdownResponse.CategoryDetail> categories = new ArrayList<>();
        String mostFrequentCategory = null;
        int maxCount = 0;
        DashboardCategoriesBreakdownResponse.CategoryDetail topCategory = null;
        BigDecimal maxSpent = BigDecimal.ZERO;

        for (Object[] row : categoryResults) {
            Long categoryId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String color = (String) row[2];
            BigDecimal totalSpent = abs(toBigDecimal(row[3]));
            Integer count = ((Number) row[4]).intValue();
            BigDecimal avgTransaction = toBigDecimal(row[5]);

            BigDecimal percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                    ? totalSpent.multiply(HUNDRED).divide(totalExpenses, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            DashboardCategoriesBreakdownResponse.CategoryDetail detail = 
                    DashboardCategoriesBreakdownResponse.CategoryDetail.builder()
                    .categoryId(categoryId)
                    .name(name)
                    .color(color)
                    .totalSpent(totalSpent)
                    .percentageOfTotal(percentage)
                    .transactionCount(count)
                    .averageTransactionValue(avgTransaction)
                    .build();

            categories.add(detail);

            if (totalSpent.compareTo(maxSpent) > 0) {
                maxSpent = totalSpent;
                topCategory = detail;
            }

            if (count > maxCount) {
                maxCount = count;
                mostFrequentCategory = name;
            }
        }

        return DashboardCategoriesBreakdownResponse.builder()
                .categories(categories)
                .totalExpenses(totalExpenses)
                .topCategory(topCategory)
                .uncategorized(DashboardCategoriesBreakdownResponse.UncategorizedSummary.builder()
                        .total(uncategorizedTotal)
                        .count(uncategorizedCount)
                        .build())
                .mostFrequentCategory(mostFrequentCategory)
                .build();
    }

    public DashboardPulseResponse getPulse(Long ledgerId, User authenticatedUser) {
        return getPulse(ledgerId, authenticatedUser, null, null);
    }

    public DashboardPulseResponse getPulse(Long ledgerId, User authenticatedUser, YearMonth targetMonth) {
        return getPulse(ledgerId, authenticatedUser, targetMonth, null);
    }

    public DashboardPulseResponse getPulse(
            Long ledgerId,
            User authenticatedUser,
            YearMonth targetMonth,
            Long createdByUserId
    ) {
        validateLedgerAccess(ledgerId, authenticatedUser);
        validateCreatedByFilter(ledgerId, createdByUserId);

        YearMonth month = resolveMonth(targetMonth);
        LocalDate startDate = month.atDay(1);
        LocalDate monthEndDate = month.atEndOfMonth();
        LocalDate today = LocalDate.now();
        LocalDate metricsEndDate = month.equals(YearMonth.from(today)) ? today : monthEndDate;

        List<Object[]> dailyResults = transactionRepository.getDailySpending(ledgerId, startDate, monthEndDate, createdByUserId);

        List<DashboardPulseResponse.DailySpending> dailySpending = new ArrayList<>();
        Map<LocalDate, BigDecimal> spendingByDate = new LinkedHashMap<>();

        for (Object[] row : dailyResults) {
            LocalDate date = toLocalDate(row[0]);
            BigDecimal amount = abs(toBigDecimal(row[1]));
            if (date != null) {
                spendingByDate.put(date, amount);
            }
        }

        LocalDate current = startDate;
        while (!current.isAfter(monthEndDate)) {
            BigDecimal amount = spendingByDate.getOrDefault(current, BigDecimal.ZERO);
            dailySpending.add(DashboardPulseResponse.DailySpending.builder()
                    .date(current.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .totalExpense(amount)
                    .build());
            current = current.plusDays(1);
        }

        int size = dailySpending.size();
        BigDecimal rollingAverage = BigDecimal.ZERO;
        int metricsEndIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, metricsEndDate);
        int metricsSize = Math.min(size, Math.max(0, metricsEndIndex + 1));

        if (metricsSize >= 7) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = metricsSize - 7; i < metricsSize; i++) {
                DashboardPulseResponse.DailySpending day = dailySpending.get(i);
                sum = sum.add(day.getTotalExpense());
            }
            rollingAverage = sum.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
        } else if (metricsSize > 0) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < metricsSize; i++) {
                sum = sum.add(dailySpending.get(i).getTotalExpense());
            }
            rollingAverage = sum.divide(BigDecimal.valueOf(metricsSize), 2, RoundingMode.HALF_UP);
        }

        DashboardPulseResponse.HighestSpendingDay highestDay = null;
        BigDecimal maxSpending = BigDecimal.ZERO;
        LocalDate maxDate = null;
        for (Object[] row : dailyResults) {
            BigDecimal amount = abs(toBigDecimal(row[1]));
            LocalDate date = toLocalDate(row[0]);
            if (amount.compareTo(maxSpending) > 0) {
                maxSpending = amount;
                maxDate = date;
            }
        }
        if (maxDate != null) {
            highestDay = DashboardPulseResponse.HighestSpendingDay.builder()
                    .date(maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .amount(maxSpending)
                    .build();
        }

        int zeroDays = 0;
        for (DashboardPulseResponse.DailySpending ds : dailySpending) {
            if (ds.getTotalExpense().compareTo(BigDecimal.ZERO) == 0) {
                zeroDays++;
            }
        }

        int streak = 0;
        for (int i = metricsSize - 1; i >= 0; i--) {
            if (dailySpending.get(i).getTotalExpense().compareTo(BigDecimal.ZERO) > 0) {
                streak++;
            } else {
                break;
            }
        }

        return DashboardPulseResponse.builder()
                .dailySpending(dailySpending)
                .sevenDayRollingAverage(rollingAverage)
                .highestSpendingDay(highestDay)
                .zeroSpendingDays(zeroDays)
                .currentSpendingStreak(streak)
                .build();
    }

    private void validateLedgerAccess(Long ledgerId, User authenticatedUser) {
        if (!ledgerRepository.existsById(ledgerId)) {
            throw new ResourceNotFoundException("Ledger not found");
        }
        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, authenticatedUser.getId())) {
            throw new AccessDeniedException("You are not a member of this ledger");
        }
    }

    private void validateCreatedByFilter(Long ledgerId, Long createdByUserId) {
        if (createdByUserId == null) {
            return;
        }
        if (!ledgerMemberRepository.existsByLedgerIdAndUserId(ledgerId, createdByUserId)) {
            throw new AccessDeniedException("Invalid member filter for this ledger");
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal abs(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.abs();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        return null;
    }

    private YearMonth resolveMonth(YearMonth targetMonth) {
        return targetMonth != null ? targetMonth : YearMonth.now();
    }
}
