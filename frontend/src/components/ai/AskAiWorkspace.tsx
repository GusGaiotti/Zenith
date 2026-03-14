"use client";

import { AxiosError } from "axios";
import { useEffect, useMemo, useRef, useState } from "react";
import { MonthPicker } from "@/components/shared/MonthPicker";
import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { LoadingSpinner } from "@/components/shared/LoadingSpinner";
import { PageHeader } from "@/components/shared/PageHeader";
import { useAskAi, useAskAiUsage } from "@/hooks/useAskAi";
import { askAiSchema } from "@/lib/validators/ai.schemas";
import type { AskAiRequest, AskAiResponse } from "@/types/api";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  headline?: string;
  highlights?: string[];
  recommendedActions?: string[];
  contextLevel?: AskAiResponse["contextLevelUsed"];
  disclaimer?: string;
};

const starterQuestions = [
  "Onde estamos gastando mais neste mes?",
  "Qual categoria merece corte imediato neste mes?",
  "Tem algum padrao de gasto que chama atencao?",
] as const;

function getCurrentYearMonth() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${now.getFullYear()}-${month}`;
}

function getContextLabel(level?: AskAiResponse["contextLevelUsed"]) {
  switch (level) {
    case "EXTENDED":
      return "Comparativo de meses";
    case "SAMPLED_TRANSACTIONS":
      return "Resumo com amostra de lancamentos";
    case "SUMMARY":
    default:
      return "Resumo do mes";
  }
}

function getModeLabel(mode?: string) {
  if (mode === "off") return "Indisponivel";
  return "Assistente ativo";
}

function getUsageSummary(mode?: string, accessAllowed?: boolean) {
  if (!accessAllowed) return "Assistente restrito para sua conta no momento";
  if (mode === "off") return "Assistente temporariamente indisponivel";
  return "Pronto para responder com base nos seus dados";
}

function getAskAiErrorMessage(error: unknown) {
  if (error instanceof AxiosError) {
    if (error.code === "ECONNABORTED") {
      return "A IA demorou mais que o esperado. Tente novamente ou reduza o contexto desta pergunta.";
    }

    const responseMessage = error.response?.data;
    if (typeof responseMessage === "string" && responseMessage.trim()) {
      return responseMessage;
    }
  }

  return error instanceof Error ? error.message : "Nao foi possivel obter resposta da IA.";
}

export function AskAiWorkspace() {
  const [question, setQuestion] = useState("");
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [includeTransactions, setIncludeTransactions] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const timelineRef = useRef<HTMLDivElement>(null);

  const askMutation = useAskAi();
  const usageQuery = useAskAiUsage(true);
  const usage = usageQuery.data;

  const blockedByAccess = usage ? !usage.accessAllowed : false;
  const remainingDaily = usage?.perUserDailyRemaining ?? 0;
  const dailyUsed = usage?.perUserDailyUsed ?? 0;

  useEffect(() => {
    timelineRef.current?.scrollTo({
      top: timelineRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages, askMutation.isPending]);

  const helperTone = useMemo(() => {
    if (!usage) return "Carregando disponibilidade";
    return getUsageSummary(usage.mode, usage.accessAllowed);
  }, [usage]);

  function submitQuestion(payload: AskAiRequest) {
    setError(null);
    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      content: payload.question,
    };

    setMessages((current) => [...current, userMessage]);

    askMutation.mutate(payload, {
      onSuccess: (data) => {
        const assistantMessage: ChatMessage = {
          id: `assistant-${Date.now()}`,
          role: "assistant",
          headline: data.headline,
          content: data.answer,
          highlights: data.highlights,
          recommendedActions: data.recommendedActions,
          contextLevel: data.contextLevelUsed,
          disclaimer: data.disclaimer,
        };

        setMessages((current) => [...current, assistantMessage]);
        setQuestion("");
        void usageQuery.refetch();
      },
      onError: (requestError) => {
        setError(getAskAiErrorMessage(requestError));
      },
    });
  }

  function handleSubmit() {
    const parsed = askAiSchema.safeParse({ question, yearMonth, includeTransactions });
    if (!parsed.success) {
      setError(parsed.error.issues[0]?.message ?? "Dados invalidos.");
      return;
    }

    submitQuestion(parsed.data);
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Pergunte a IA"
        subtitle="Transforme movimentacoes do mes em respostas claras, com foco no que pesa mais e no que vale ajustar."
      />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px]">
        <section className="surface flex min-h-[72vh] flex-col overflow-hidden">
          <div className="border-b border-[var(--border)] px-5 py-4">
            <div className="flex flex-wrap items-center gap-3">
              <span className="info-badge text-[var(--accent-hover)]">
                {getModeLabel(usage?.mode)}
              </span>
              <span className="info-badge text-[var(--income)]">
                {remainingDaily} restantes hoje
              </span>
              <span className="text-sm text-[var(--text-secondary)]">{helperTone}</span>
            </div>
          </div>

          <div ref={timelineRef} className="flex-1 space-y-4 overflow-y-auto px-5 py-5">
            {messages.length === 0 ? (
              <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
                <div className="elevated p-5">
                  <p className="text-xs uppercase tracking-[0.12em] text-[var(--text-muted)]">Comece por aqui</p>
                  <h2 className="mt-2 text-2xl font-semibold text-[var(--text-primary)]">Use o assistente como uma leitura rapida do seu mes</h2>
                  <p className="mt-3 max-w-2xl text-sm leading-6 text-[var(--text-secondary)]">
                    Escolha o mes de referencia, escreva uma pergunta objetiva e receba uma resposta curta com prioridades, achados e proximos passos. Ele considera saldo, categorias e padroes do periodo para evitar respostas soltas.
                  </p>
                  <div className="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    <div className="rounded-2xl border border-[color-mix(in_srgb,var(--accent)_20%,transparent)] bg-[color-mix(in_srgb,var(--accent)_7%,white_93%)] px-4 py-4">
                      <p className="text-sm font-semibold text-[var(--text-primary)]">Mes de referencia</p>
                      <p className="mt-1 text-sm leading-6 text-[var(--text-secondary)]">Troque o mes para revisar um fechamento anterior ou comparar comportamentos recentes.</p>
                    </div>
                    <div className="rounded-2xl border border-[color-mix(in_srgb,var(--accent-emerald)_20%,transparent)] bg-[color-mix(in_srgb,var(--accent-emerald)_7%,white_93%)] px-4 py-4">
                      <p className="text-sm font-semibold text-[var(--text-primary)]">Amostra de lancamentos</p>
                      <p className="mt-1 text-sm leading-6 text-[var(--text-secondary)]">Ative quando quiser uma resposta mais detalhada. O sistema escolhe alguns lancamentos relevantes do periodo para aprofundar a leitura.</p>
                    </div>
                    <div className="rounded-2xl border border-[color-mix(in_srgb,var(--accent-amber)_20%,transparent)] bg-[color-mix(in_srgb,var(--accent-amber)_7%,white_93%)] px-4 py-4 sm:col-span-2 xl:col-span-1">
                      <p className="text-sm font-semibold text-[var(--text-primary)]">Como extrair mais valor</p>
                      <p className="mt-1 text-sm leading-6 text-[var(--text-secondary)]">Pergunte por excessos, categorias, comparacoes ou oportunidades de corte. Quanto mais clara a duvida, melhor a recomendacao.</p>
                    </div>
                  </div>
                </div>
                <div className="elevated p-5">
                  <p className="text-xs uppercase tracking-[0.12em] text-[var(--text-muted)]">Boas perguntas</p>
                  <div className="mt-3 flex flex-col gap-2">
                    {starterQuestions.map((item) => (
                      <button
                        key={item}
                        type="button"
                        className="focusable rounded-2xl border border-[var(--border)] bg-[var(--panel-bg)] px-4 py-3 text-left text-sm text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]"
                        onClick={() => setQuestion(item)}
                      >
                        {item}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            ) : null}

            {messages.map((message) => (
              <article
                key={message.id}
                className={`max-w-3xl rounded-3xl border px-4 py-4 shadow-[0_10px_24px_rgba(7,12,28,0.16)] ${
                  message.role === "user"
                    ? "ml-auto border-[var(--accent)] bg-[var(--accent)] text-white"
                    : "border-[var(--surface-edge)] bg-[var(--menu-bg)] text-[var(--text-primary)]"
                }`}
              >
                <div className="flex items-center justify-between gap-3">
                  <p className={`text-xs uppercase tracking-[0.12em] ${message.role === "user" ? "text-white/75" : "text-[var(--text-muted)]"}`}>
                    {message.role === "user" ? "Voce" : "Assistente Zenith"}
                  </p>
                  {message.contextLevel ? (
                    <span className="text-[11px] text-[var(--text-secondary)]">{getContextLabel(message.contextLevel)}</span>
                  ) : null}
                </div>
                {message.headline ? (
                  <p className="mt-3 text-lg font-semibold text-[var(--text-primary)]">{message.headline}</p>
                ) : null}
                {message.highlights?.length ? (
                  <div className="mt-3 grid gap-2 sm:grid-cols-2">
                    {message.highlights.map((item) => (
                      <div key={item} className="rounded-2xl border border-[var(--border)] bg-[var(--panel-bg)] px-3 py-3 text-xs leading-5 text-[var(--text-secondary)]">
                        {item}
                      </div>
                    ))}
                  </div>
                ) : null}
                <p className={`mt-3 whitespace-pre-wrap text-sm leading-7 ${message.role === "user" ? "text-white" : "text-[var(--text-primary)]"}`}>
                  {message.content}
                </p>
                {message.recommendedActions?.length ? (
                  <div className="mt-4">
                    <p className="text-xs uppercase tracking-[0.12em] text-[var(--text-muted)]">Acoes sugeridas</p>
                    <ul className="mt-2 space-y-2 text-sm leading-6 text-[var(--text-secondary)]">
                      {message.recommendedActions.map((item) => (
                        <li key={item} className="rounded-2xl border border-[var(--border)] bg-[var(--panel-bg)] px-3 py-3">
                          {item}
                        </li>
                      ))}
                    </ul>
                  </div>
                ) : null}
                {message.disclaimer ? (
                  <p className="mt-3 text-xs text-[var(--text-secondary)]">{message.disclaimer}</p>
                ) : null}
              </article>
            ))}

            {askMutation.isPending ? (
              <article className="max-w-3xl rounded-3xl border border-[var(--surface-edge)] bg-[var(--menu-bg)] px-4 py-4 text-[var(--text-primary)]">
                <div className="flex items-center gap-3">
                  <LoadingSpinner size="sm" />
                  <span className="text-sm text-[var(--text-secondary)]">Consultando a IA com o contexto selecionado...</span>
                </div>
              </article>
            ) : null}
          </div>

          <div className="border-t border-[var(--border)] bg-[var(--panel-bg)] px-5 py-4">
            {error ? (
              <div className="danger-chip mb-4 rounded-2xl px-4 py-3 text-sm">
                {error}
              </div>
            ) : null}

            <div className="flex flex-col gap-3">
              <label className="block">
                <span className="mb-2 flex items-center gap-2 text-sm text-[var(--text-secondary)]">
                  Mensagem
                  <InfoTooltip align="left" text="Perguntas curtas e diretas costumam gerar respostas mais objetivas." />
                </span>
                <textarea
                  value={question}
                  onChange={(event) => setQuestion(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && !event.shiftKey) {
                      event.preventDefault();
                      handleSubmit();
                    }
                  }}
                  maxLength={300}
                  required
                  placeholder="Ex: Onde estamos gastando acima do esperado neste mes?"
                  className="focusable min-h-28 w-full rounded-3xl border border-[var(--border)] bg-[var(--bg-surface)] px-4 py-4 text-[var(--text-primary)] outline-none placeholder:text-[var(--text-secondary)]"
                />
                <span className="mt-2 block text-right font-mono text-xs text-[var(--text-muted)]">{question.length}/300</span>
              </label>

              <div className="flex flex-wrap gap-3">
                <button
                  type="button"
                  disabled={askMutation.isPending || blockedByAccess}
                  className="focusable h-12 rounded-2xl bg-[var(--accent)] px-5 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
                  onClick={handleSubmit}
                >
                  {askMutation.isPending ? "Consultando..." : "Enviar pergunta"}
                </button>
                <button
                  type="button"
                  className="focusable danger-chip h-12 rounded-2xl px-5 text-sm font-medium transition-colors duration-150 hover:border-[var(--expense)] hover:bg-[color-mix(in_srgb,var(--expense)_14%,transparent)]"
                  onClick={() => {
                    setQuestion("");
                    setError(null);
                    setIncludeTransactions(false);
                    setYearMonth(getCurrentYearMonth());
                  }}
                >
                  Limpar composicao
                </button>
                <button
                  type="button"
                  className="focusable h-12 rounded-2xl border border-[var(--border)] px-5 text-sm font-medium text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]"
                  onClick={() => {
                    setMessages([]);
                    setError(null);
                  }}
                >
                  Nova conversa
                </button>
              </div>
              <p className="text-xs text-[var(--text-muted)]">Pressione Enter para enviar. Use Shift + Enter para quebrar linha.</p>
            </div>
          </div>
        </section>

        <aside className="space-y-4">
          <section className="surface p-5">
            <div className="flex items-center gap-2 text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">
              <span>Uso e cotas</span>
              <InfoTooltip align="left" text="Esses indicadores mostram a disponibilidade atual do assistente para a sua conta." />
            </div>
            {usage ? (
              <div className="mt-4 grid gap-3">
                <div className="elevated p-4">
                  <p className="text-xs text-[var(--text-muted)]">Restantes hoje</p>
                  <p className="mt-1 text-2xl font-semibold text-[var(--text-primary)]">{remainingDaily}</p>
                </div>
                <div className="elevated p-4">
                  <p className="text-xs text-[var(--text-muted)]">Usadas hoje</p>
                  <p className="mt-1 text-2xl font-semibold text-[var(--text-primary)]">{dailyUsed}</p>
                </div>
                <div className="elevated p-4">
                  <p className="text-xs text-[var(--text-muted)]">Janela por minuto</p>
                  <p className="mt-1 text-2xl font-semibold text-[var(--text-primary)]">
                    {usage.perUserCurrentMinuteUsed}/{usage.perUserPerMinuteLimit}
                  </p>
                </div>
                <p className="text-sm leading-6 text-[var(--text-secondary)]">{getUsageSummary(usage.mode, usage.accessAllowed)}</p>
                {!usage.accessAllowed ? (
                  <p className="danger-chip rounded-2xl px-4 py-3 text-sm">
                    O assistente nao esta disponivel para a sua conta agora.
                  </p>
                ) : null}
              </div>
            ) : (
              <div className="mt-4">
                <LoadingSpinner label="Carregando cotas" />
              </div>
            )}
          </section>

          <section className="surface p-5">
            <p className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Contexto da consulta</p>
            <div className="mt-4 space-y-4">
              <MonthPicker value={yearMonth} onChange={setYearMonth} label="Mes de referencia" />
              <label className="flex items-start gap-3 rounded-2xl border border-[var(--border)] bg-[var(--panel-bg)] px-4 py-3 text-sm text-[var(--text-primary)]">
                <input
                  type="checkbox"
                  checked={includeTransactions}
                  onChange={(event) => setIncludeTransactions(event.target.checked)}
                  className="mt-1 h-4 w-4"
                />
                <span className="space-y-1">
                  <span className="block font-medium">Incluir amostra automatica de lancamentos</span>
                  <span className="block text-[13px] leading-5 text-[var(--text-secondary)]">
                    O sistema seleciona ate 50 lancamentos do periodo para enriquecer a resposta quando a pergunta pede mais contexto.
                  </span>
                </span>
              </label>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
