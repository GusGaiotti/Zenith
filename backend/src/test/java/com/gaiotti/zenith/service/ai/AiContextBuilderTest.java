package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.model.Transaction;
import com.gaiotti.zenith.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiContextBuilderTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AiContextBuilder aiContextBuilder;

    @Test
    void build_ExtendedComparisonCapsMonthsAtSix() {
        AskAiRequest request = new AskAiRequest();
        request.setQuestion("Compare os ultimos 12 meses");
        request.setYearMonth("2026-03");

        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.INCOME), any(), any(), isNull()))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.EXPENSE), any(), any(), isNull()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumExpensesByCategoryForLedgerAndDateRange(eq(1L), any(), any(), isNull()))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "Moradia", new BigDecimal("1200.00")}));
        when(transactionRepository.getMonthlyTrends(eq(1L), any(), any(), isNull()))
                .thenReturn(List.<Object[]>of(new Object[]{2026, 3, new BigDecimal("5000.00"), new BigDecimal("3000.00")}));

        aiContextBuilder.build(1L, request);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(transactionRepository).getMonthlyTrends(eq(1L), startCaptor.capture(), endCaptor.capture(), isNull());

        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2025, 10, 1));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void build_SampledTransactionsCapsAtFifty() {
        AskAiRequest request = new AskAiRequest();
        request.setQuestion("Mostre detalhes");
        request.setYearMonth("2026-03");
        request.setIncludeTransactions(true);

        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.INCOME), any(), any(), isNull()))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByLedgerAndTypeAndDateRange(eq(1L), eq(Transaction.TransactionType.EXPENSE), any(), any(), isNull()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumExpensesByCategoryForLedgerAndDateRange(eq(1L), any(), any(), isNull()))
                .thenReturn(List.of());
        when(transactionRepository.findSampleForLedgerAndDateRange(eq(1L), any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        aiContextBuilder.build(1L, request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findSampleForLedgerAndDateRange(eq(1L), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    }
}
