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
public class DashboardCoupleSplitResponse {

    private List<UserContribution> userContributions;
    private HighestTransaction highestTransaction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContribution {
        private Long userId;
        private String email;
        private String displayName;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal incomePercentage;
        private BigDecimal expensePercentage;
        private BigDecimal netFairnessDelta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighestTransaction {
        private BigDecimal amount;
        private String userDisplayName;
    }
}
