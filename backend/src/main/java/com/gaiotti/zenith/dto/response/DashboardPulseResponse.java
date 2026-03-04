package com.gaiotti.zenith.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPulseResponse {

    private List<DailySpending> dailySpending;
    private BigDecimal sevenDayRollingAverage;
    private HighestSpendingDay highestSpendingDay;
    private Integer zeroSpendingDays;
    private Integer currentSpendingStreak;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySpending {
        private String date;
        private BigDecimal totalExpense;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighestSpendingDay {
        private String date;
        private BigDecimal amount;
    }
}
