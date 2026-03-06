package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.dto.request.AskAiRequest;
import com.gaiotti.zenith.dto.response.AskAiResponse;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.LedgerMemberRepository;
import com.gaiotti.zenith.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskAiService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final AiContextBuilder aiContextBuilder;

    public AskAiResponse ask(Long ledgerId, User authenticatedUser, AskAiRequest request) {
        validateLedgerAccess(ledgerId, authenticatedUser);

        AiContextBuilder.AiContext context = aiContextBuilder.build(ledgerId, request);

        String answer = "Resumo de " + context.targetMonth()
                + ": entradas=" + context.totalIncome().toPlainString()
                + ", saidas=" + context.totalExpense().toPlainString()
                + ", saldo=" + context.net().toPlainString()
                + ".";

        if (!context.topExpenseCategories().isEmpty()) {
            AiContextBuilder.CategoryTotal topCategory = context.topExpenseCategories().get(0);
            answer += " Maior categoria de despesa: " + topCategory.name() + " (" + topCategory.total().toPlainString() + ").";
        }

        if (context.contextLevel() == AskAiResponse.ContextLevel.EXTENDED && !context.monthlyAggregates().isEmpty()) {
            answer += " Comparacao com " + context.monthlyAggregates().size() + " meses incluida.";
        }

        if (context.contextLevel() == AskAiResponse.ContextLevel.SAMPLED_TRANSACTIONS) {
            answer += " Analise com amostra limitada de " + context.sampledTransactions().size() + " transacoes.";
        }

        return AskAiResponse.builder()
                .answer(answer)
                .contextLevelUsed(context.contextLevel())
                .disclaimer("Resposta deterministica de integracao. Provedor de IA sera conectado nas proximas fases.")
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
}
