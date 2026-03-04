import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency } from "@/lib/utils/currency";
import { clampPercentage, formatPercentage, normalizePercentage } from "@/lib/utils/percent";
import type { DashboardCoupleSplitResponse } from "@/types/api";

interface CoupleSplitPanelProps {
  data?: DashboardCoupleSplitResponse;
}

function renderShare(value: number) {
  return value === 0 ? null : formatPercentage(value);
}

export function CoupleSplitPanel({ data }: CoupleSplitPanelProps) {
  const rows = data?.userContributions ?? [];

  return (
    <section className="surface futura-card data-reveal hover-grow p-6">
      <div className="flex items-center justify-between">
        <h3 className="font-display text-2xl italic">Divisao da fatura</h3>
        <InfoTooltip text="Mostra quanto cada pessoa participou nas entradas e nas saidas do mes e destaca quem contribuiu ou gastou proporcionalmente mais." />
      </div>
      <div className="mt-5 space-y-5">
        {rows.length ? (
          rows.map((row) => {
            const delta = Number(row.netFairnessDelta);
            const incomePercentage = normalizePercentage(row.incomePercentage);
            const expensePercentage = normalizePercentage(row.expensePercentage);
            const incomeShare = renderShare(incomePercentage);
            const expenseShare = renderShare(expensePercentage);
            const status = delta > 0 ? "Contribuiu proporcionalmente mais" : delta < 0 ? "Gastou proporcionalmente mais" : "Equilibrado";
            const statusClassName = delta > 0
              ? "border border-emerald-400/40 bg-emerald-500/15 text-emerald-300"
              : delta < 0
                ? "border border-red-400/45 bg-red-500/20 text-red-200"
                : "border border-amber-400/35 bg-amber-500/15 text-amber-300";

            return (
              <div key={row.userId} className="rounded-md bg-white/5 p-4 transition-transform duration-150 hover:scale-[1.02]">
                <p className="text-sm font-semibold">{row.displayName}</p>
                <p className="mt-2 text-xs text-[var(--text-secondary)]">
                  Entrada <span className="font-mono text-[var(--income)]">{formatCurrency(row.totalIncome)}</span>
                </p>
                <p className="text-xs text-[var(--text-secondary)]">
                  Saida <span className="font-mono text-[var(--expense)]">{formatCurrency(row.totalExpense)}</span>
                </p>
                <div className="mt-3 space-y-2">
                  <div className="space-y-1">
                    <div className="flex items-center justify-between text-[11px] text-[var(--text-muted)]">
                      <span>Entrada</span>
                      {incomeShare ? <span>{incomeShare}</span> : null}
                    </div>
                    <div className="h-2 rounded-full bg-white/10">
                      <div
                        className="h-2 rounded-full bg-[var(--income)]"
                        style={{ width: `${clampPercentage(incomePercentage)}%` }}
                      />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center justify-between text-[11px] text-[var(--text-muted)]">
                      <span>Saida</span>
                      {expenseShare ? <span>{expenseShare}</span> : null}
                    </div>
                    <div className="h-2 rounded-full bg-white/10">
                      <div
                        className="h-2 rounded-full bg-[var(--expense)]"
                        style={{ width: `${clampPercentage(expensePercentage)}%` }}
                      />
                    </div>
                  </div>
                </div>
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-xs text-[var(--text-secondary)]">Equilibrio de contribuicao</span>
                  <span className={`rounded-full px-2 py-1 text-xs ${statusClassName}`}>
                    {status}
                  </span>
                </div>
              </div>
            );
          })
        ) : (
          <p className="text-sm text-[var(--text-secondary)]">Sem dados de contribuicao ainda.</p>
        )}
      </div>
      {data?.highestTransaction ? (
        <p className="mt-5 text-xs text-[var(--text-secondary)]">
          Maior compra: {formatCurrency(data.highestTransaction.amount)} por {data.highestTransaction.userDisplayName}
        </p>
      ) : null}
    </section>
  );
}
