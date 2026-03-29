package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.dto.response.AskAiResponse;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AiContextBuilder {

    private static final int MAX_MONTHS_IN_CONTEXT = 6;
    private static final int MAX_SAMPLED_TRANSACTIONS = 50;
    private static final int MAX_TOP_CATEGORIES = 3;
    // Matches Portuguese phrases like "3 meses", "6 meses atrás" to expand context window
    private static final Pattern MONTHS_PATTERN = Pattern.compile("(\\d{1,2})\\s*mes");

    private final TransactionRepository transactionRepository;

    public AiContext build(Long ledgerId, AskAiRequest request) {
        YearMonth targetMonth = parseYearMonthOrDefault(request.getYearMonth());
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();

        BigDecimal income = nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId,
                Transaction.TransactionType.INCOME,
                startDate,
                endDate,
                null
        ));

        BigDecimal expense = abs(nonNull(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(
                ledgerId,
                Transaction.TransactionType.EXPENSE,
                startDate,
                endDate,
                null
        )));

        List<CategoryTotal> topCategories = transactionRepository
                .sumExpensesByCategoryForLedgerAndDateRange(ledgerId, startDate, endDate, null)
                .stream()
                .map(this::toCategoryTotal)
                .sorted(Comparator.comparing(CategoryTotal::total).reversed())
                .limit(MAX_TOP_CATEGORIES)
                .toList();

        AskAiResponse.ContextLevel contextLevel = AskAiResponse.ContextLevel.SUMMARY;
        List<MonthlyAggregate> extendedMonths = List.of();
        List<SampledTransaction> sampledTransactions = List.of();

        int requestedMonths = resolveRequestedMonths(request.getQuestion());
        if (requestedMonths > 1) {
            contextLevel = AskAiResponse.ContextLevel.EXTENDED;
            YearMonth endMonth = targetMonth;
            YearMonth startMonth = endMonth.minusMonths(requestedMonths - 1L);
            List<Object[]> monthlyRows = transactionRepository.getMonthlyTrends(
                    ledgerId,
                    startMonth.atDay(1),
                    endMonth.atEndOfMonth(),
                    null
            );
            extendedMonths = monthlyRows.stream().map(this::toMonthlyAggregate).toList();
        }

        if (Boolean.TRUE.equals(request.getIncludeTransactions())) {
            contextLevel = AskAiResponse.ContextLevel.SAMPLED_TRANSACTIONS;
            sampledTransactions = transactionRepository
                    .findSampleForLedgerAndDateRange(
                            ledgerId,
                            startDate,
                            endDate,
                            PageRequest.of(0, MAX_SAMPLED_TRANSACTIONS)
                    )
                    .stream()
                    .map(this::toSampledTransaction)
                    .toList();
        }

        return new AiContext(
                targetMonth,
                contextLevel,
                income,
                expense,
                income.subtract(expense),
                topCategories,
                extendedMonths,
                sampledTransactions
        );
    }

    private YearMonth parseYearMonthOrDefault(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now();
        }

        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid yearMonth format. Use yyyy-MM");
        }
    }

    private int resolveRequestedMonths(String question) {
        if (question == null || question.isBlank()) {
            return 1;
        }

        Matcher matcher = MONTHS_PATTERN.matcher(question.toLowerCase());
        if (!matcher.find()) {
            return 1;
        }

        int requested = Integer.parseInt(matcher.group(1));
        return Math.max(1, Math.min(MAX_MONTHS_IN_CONTEXT, requested));
    }

    private CategoryTotal toCategoryTotal(Object[] row) {
        String name = row[1] != null ? (String) row[1] : "Sem categoria";
        BigDecimal total = abs(toBigDecimal(row[2]));
        return new CategoryTotal(name, total);
    }

    private MonthlyAggregate toMonthlyAggregate(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        BigDecimal income = nonNull(toBigDecimal(row[2]));
        BigDecimal expense = abs(nonNull(toBigDecimal(row[3])));
        return new MonthlyAggregate(String.format("%d-%02d", year, month), income, expense, income.subtract(expense));
    }

    private SampledTransaction toSampledTransaction(Transaction transaction) {
        return new SampledTransaction(
                transaction.getDate().toString(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getCategory() != null ? transaction.getCategory().getName() : "Sem categoria",
                transaction.getDescription()
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal abs(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.abs();
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record AiContext(
            YearMonth targetMonth,
            AskAiResponse.ContextLevel contextLevel,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal net,
            List<CategoryTotal> topExpenseCategories,
            List<MonthlyAggregate> monthlyAggregates,
            List<SampledTransaction> sampledTransactions
    ) {}

    public record CategoryTotal(String name, BigDecimal total) {}

    public record MonthlyAggregate(String yearMonth, BigDecimal income, BigDecimal expense, BigDecimal net) {}

    public record SampledTransaction(String date, String type, BigDecimal amount, String category, String description) {}
}
