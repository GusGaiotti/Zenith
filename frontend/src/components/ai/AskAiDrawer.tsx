"use client";

import { useEffect, useId, useState } from "react";
import { MonthPicker } from "@/components/shared/MonthPicker";
import { useAskAi, useAskAiUsage } from "@/hooks/useAskAi";
import { askAiSchema } from "@/lib/validators/ai.schemas";
import type { AskAiResponse } from "@/types/api";

interface AskAiDrawerProps {
  open: boolean;
  onClose: () => void;
}

function getCurrentYearMonth() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${now.getFullYear()}-${month}`;
}

export function AskAiDrawer({ open, onClose }: AskAiDrawerProps) {
  const titleId = useId();
  const [question, setQuestion] = useState("");
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [includeTransactions, setIncludeTransactions] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<AskAiResponse | null>(null);
  const askMutation = useAskAi();
  const usageQuery = useAskAiUsage(open);
  const usage = usageQuery.data;

  const blockedByAccess = usage ? !usage.accessAllowed : false;
  const modeLabel = usage?.mode === "openai" ? "OpenAI" : usage?.mode === "off" ? "Off" : "Ollama local";

  useEffect(() => {
    if (!open) {
      return;
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [open, onClose]);

  if (!open) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-end bg-black/55 p-2 sm:p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="surface h-[95vh] w-full max-w-2xl overflow-y-auto p-5 shadow-[0_24px_60px_rgba(5,10,24,0.6)] sm:p-6"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id={titleId} className="font-display text-3xl">
              Perguntar para IA
            </h2>
            <p className="mt-2 text-sm text-[var(--text-secondary)]">
              Resposta baseada em contexto financeiro resumido. A IA pode cometer erros.
            </p>
          </div>
          <button
            type="button"
            className="focusable rounded-xl border border-[var(--border)] px-3 py-2 text-sm text-[var(--text-secondary)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]"
            onClick={onClose}
          >
            Fechar
          </button>
        </div>

        <div className="mt-5 space-y-2 rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] p-4 text-sm text-[var(--text-secondary)]">
          <p className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Uso e modo</p>
          <p>
            Modo atual: <span className="font-semibold text-[var(--text-primary)]">{modeLabel}</span>
          </p>
          {usage ? (
            <>
              <p>
                Cota diaria: <span className="font-semibold text-[var(--text-primary)]">{usage.perUserDailyRemaining}/{usage.perUserDailyQuota}</span> restantes.
              </p>
              <p>Janela por minuto (usuario): {usage.perUserCurrentMinuteUsed}/{usage.perUserPerMinuteLimit}</p>
              <p>{usage.note}</p>
              {!usage.accessAllowed ? (
                <p className="text-red-200">Acesso a IA bloqueado para seu usuario neste ambiente.</p>
              ) : null}
            </>
          ) : (
            <p>Carregando status de cota...</p>
          )}
        </div>

        <div className="mt-4 space-y-2 rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] p-4 text-sm text-[var(--text-secondary)]">
          <p className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Niveis de contexto</p>
          <p><span className="font-semibold text-[var(--text-primary)]">SUMMARY:</span> resumo mensal (menor custo).</p>
          <p>
            <span className="font-semibold text-[var(--text-primary)]">EXTENDED:</span> comparacao de varios meses.
            O mes escolhido e a ancora final. Exemplo: selecionar 2026-03 e perguntar compare ultimos 3 meses
            usa 2026-01, 2026-02 e 2026-03.
          </p>
          <p>
            <span className="font-semibold text-[var(--text-primary)]">SAMPLED_TRANSACTIONS:</span> inclui amostra
            limitada de transacoes (ate 50) para analise mais detalhada.
          </p>
        </div>

        <form
          className="mt-6 space-y-4"
          onSubmit={(event) => {
            event.preventDefault();
            setError(null);

            const parsed = askAiSchema.safeParse({ question, yearMonth, includeTransactions });
            if (!parsed.success) {
              setError(parsed.error.issues[0]?.message ?? "Dados invalidos.");
              return;
            }

            askMutation.mutate(parsed.data, {
              onSuccess: (data) => {
                setResult(data);
                usageQuery.refetch();
              },
              onError: (requestError) => {
                setResult(null);
                setError(requestError instanceof Error ? requestError.message : "Nao foi possivel obter resposta da IA.");
              },
            });
          }}
        >
          <label className="block">
            <span className="mb-1 block text-sm text-[var(--text-secondary)]">Pergunta</span>
            <textarea
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              maxLength={300}
              required
              placeholder="Ex: Onde estamos gastando acima do esperado neste mes?"
              className="focusable min-h-28 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 py-3 text-[var(--text-primary)] outline-none placeholder:text-[var(--text-secondary)]"
            />
            <span className="mt-1 block text-right font-mono text-xs text-[var(--text-muted)]">{question.length}/300</span>
          </label>

          <MonthPicker value={yearMonth} onChange={setYearMonth} label="Mes de referencia" />

          <label className="flex items-center gap-3 rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 py-3 text-sm text-[var(--text-primary)]">
            <input
              type="checkbox"
              checked={includeTransactions}
              onChange={(event) => setIncludeTransactions(event.target.checked)}
              className="h-4 w-4"
            />
            Incluir amostra limitada de transacoes
          </label>

          <div className="flex gap-2">
            <button
              type="submit"
              disabled={askMutation.isPending || blockedByAccess}
              className="focusable h-11 rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
            >
              {askMutation.isPending ? "Consultando..." : "Enviar pergunta"}
            </button>
            <button
              type="button"
              className="focusable h-11 rounded-xl border border-[var(--border)] px-4 text-sm text-[var(--text-secondary)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]"
              onClick={() => {
                setQuestion("");
                setResult(null);
                setError(null);
                setIncludeTransactions(false);
                setYearMonth(getCurrentYearMonth());
              }}
            >
              Limpar
            </button>
          </div>
        </form>

        {error ? (
          <div className="mt-5 rounded-xl border border-red-400/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">{error}</div>
        ) : null}

        {result ? (
          <div className="mt-5 space-y-3 rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] p-4">
            <p className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Resposta</p>
            <p className="text-sm leading-6 text-[var(--text-primary)]">{result.answer}</p>
            <p className="text-xs text-[var(--text-secondary)]">Nivel de contexto: {result.contextLevelUsed}</p>
            {result.disclaimer ? <p className="text-xs text-[var(--text-secondary)]">{result.disclaimer}</p> : null}
          </div>
        ) : null}
      </section>
    </div>
  );
}
