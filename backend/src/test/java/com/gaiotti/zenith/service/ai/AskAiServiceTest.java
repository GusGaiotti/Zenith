package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.config.AiProperties;
import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.dto.response.AskAiResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import com.gaiotti.zenith.service.ai.provider.AiProvider;
import com.gaiotti.zenith.service.ai.provider.AiProviderException;
import com.gaiotti.zenith.service.ai.provider.AiProviderResult;
import com.gaiotti.zenith.service.ai.provider.AiProviderRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskAiServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMemberRepository ledgerMemberRepository;

    @Mock
    private AiContextBuilder aiContextBuilder;

    @Mock
    private AiProviderRouter aiProviderRouter;

    @Mock
    private AiProperties aiProperties;

    @Mock
    private AiProvider aiProvider;

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
    void ask_ProviderSuccess_ReturnsProviderAnswer() {
        AskAiRequest request = new AskAiRequest();
        request.setQuestion("Compare 6 meses");

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(aiContextBuilder.build(any(), any())).thenReturn(sampleContext());
        when(aiProviderRouter.resolveActiveProvider()).thenReturn(aiProvider);
        when(aiProperties.getMaxResponseTokens()).thenReturn(220);
        when(aiProvider.ask(any(), any(), eq(220))).thenReturn(new AiProviderResult("Resposta do provider", "ollama"));

        AskAiResponse response = askAiService.ask(1L, member, request);

        assertThat(response.getAnswer()).isEqualTo("Resposta do provider");
        assertThat(response.getContextLevelUsed()).isEqualTo(AskAiResponse.ContextLevel.SUMMARY);
    }

    @Test
    void ask_ProviderFailure_ReturnsSafeFallbackWithoutSensitiveError() {
        AskAiRequest request = new AskAiRequest();
        request.setQuestion("Compare 6 meses");

        when(ledgerRepository.existsById(1L)).thenReturn(true);
        when(ledgerMemberRepository.existsByLedgerIdAndUserId(1L, 1L)).thenReturn(true);
        when(aiContextBuilder.build(any(), any())).thenReturn(sampleContext());
        when(aiProviderRouter.resolveActiveProvider()).thenReturn(aiProvider);
        when(aiProperties.getMaxResponseTokens()).thenReturn(220);
        when(aiProvider.ask(any(), any(), eq(220)))
                .thenThrow(new AiProviderException("OpenAI provider request failed: sk-prod-secret"));

        AskAiResponse response = askAiService.ask(1L, member, request);

        assertThat(response.getAnswer()).contains("Resumo de 2026-03");
        assertThat(response.getDisclaimer()).contains("IA indisponivel");
        assertThat(response.getAnswer()).doesNotContain("sk-prod-secret");
    }

    private AiContextBuilder.AiContext sampleContext() {
        return new AiContextBuilder.AiContext(
                YearMonth.of(2026, 3),
                AskAiResponse.ContextLevel.SUMMARY,
                new BigDecimal("5000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("2000.00"),
                List.of(new AiContextBuilder.CategoryTotal("Moradia", new BigDecimal("1300.00"))),
                List.of(),
                List.of()
        );
    }
}
