package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskAiServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private AiContextBuilder aiContextBuilder;

    @InjectMocks
    private AskAiService askAiService;

    private User member;

    @BeforeEach
    void setUp() {
        member = User.builder().id(1L).email("user@example.com").displayName("User").build();
    }

    @Test
    void ask_LedgerNotFound_ThrowsResourceNotFoundException() {
        when(ledgerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> askAiService.ask(99L, member, new AskAiRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ledger not found");
    }

    @Test
    void ask_NotMember_ThrowsAccessDeniedException() {
        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> askAiService.ask(1L, member, new AskAiRequest()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this ledger");
    }

    @Test
    void ask_ReturnsDeterministicResponse() {
        AskAiRequest request = new AskAiRequest();
        request.setQuestion("Compare 6 meses");

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(aiContextBuilder.build(any(), any())).thenReturn(new AiContextBuilder.AiContext(
                java.time.YearMonth.of(2026, 3),
                com.gaiotti.zenith.dto.response.AskAiResponse.ContextLevel.SUMMARY,
                new BigDecimal("5000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("2000.00"),
                java.util.List.of(new AiContextBuilder.CategoryTotal("Moradia", new BigDecimal("1300.00"))),
                java.util.List.of(),
                java.util.List.of()
        ));

        var response = askAiService.ask(1L, member, request);

        assertThat(response.getContextLevelUsed()).isEqualTo(com.gaiotti.zenith.dto.response.AskAiResponse.ContextLevel.SUMMARY);
        assertThat(response.getAnswer()).contains("saldo=2000.00");
    }
}
