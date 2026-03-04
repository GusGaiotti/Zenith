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
public class DashboardCategoriesBreakdownResponse {

    private List<CategoryDetail> categories;
    private BigDecimal totalExpenses;
    private CategoryDetail topCategory;
    private UncategorizedSummary uncategorized;
    private String mostFrequentCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetail {
        private Long categoryId;
        private String name;
        private String color;
        private BigDecimal totalSpent;
        private BigDecimal percentageOfTotal;
        private Integer transactionCount;
        private BigDecimal averageTransactionValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UncategorizedSummary {
        private BigDecimal total;
        private Integer count;
    }
}
