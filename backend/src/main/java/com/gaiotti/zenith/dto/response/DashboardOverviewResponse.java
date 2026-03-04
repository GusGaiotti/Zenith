package com.gaiotti.zenith.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;
    private BigDecimal dailyBurnRate;
    private BigDecimal projectedEndOfMonthExpense;
    private BigDecimal projectedEndOfMonthBalance;
    private BigDecimal monthOverMonthExpenseChange;
    private BigDecimal monthOverMonthIncomeChange;
}
