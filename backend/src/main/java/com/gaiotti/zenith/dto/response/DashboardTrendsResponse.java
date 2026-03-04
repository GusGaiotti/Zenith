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
public class DashboardTrendsResponse {

    private List<MonthlyTrend> monthlyTrends;
    private TrendDirection overallTrend;
    private MonthlyTrend bestMonth;
    private MonthlyTrend worstMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String yearMonth;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal net;
        private BigDecimal savingsRate;
    }

    public enum TrendDirection {
        IMPROVING,
        DECLINING,
        STABLE
    }
}
