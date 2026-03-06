import type { AskAiRequest, AskAiResponse } from "@/types/api";

function resolveMockContextLevel(includeTransactions: boolean): AskAiResponse["contextLevelUsed"] {
  return includeTransactions ? "SAMPLED_TRANSACTIONS" : "SUMMARY";
}

export async function askAiMock(_: number, payload: AskAiRequest): Promise<AskAiResponse> {
  await new Promise((resolve) => setTimeout(resolve, 700));

  if (payload.question.toLowerCase().includes("falha-ia")) {
    throw new Error("Falha ao consultar IA. Tente novamente em instantes.");
  }

  const contextLevelUsed = resolveMockContextLevel(payload.includeTransactions ?? false);

  return {
    answer:
      contextLevelUsed === "SUMMARY"
        ? `Com base no resumo de ${payload.yearMonth ?? "mes atual"}, voce pode reduzir despesas variaveis em categorias com maior peso e acompanhar o saldo liquido semanalmente.`
        : `Com base no resumo e na amostra de lancamentos de ${payload.yearMonth ?? "mes atual"}, o maior impacto esta nas despesas recorrentes. Priorize renegociacao e defina teto semanal por categoria.`,
    contextLevelUsed,
    disclaimer: "Resposta gerada por IA. Revise antes de tomar decisoes financeiras.",
  };
}
