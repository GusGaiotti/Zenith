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
      <div className="relative pr-10">
        <h3 className="font-display text-2xl italic">Divisao da fatura</h3>
        <div className="absolute right-0 top-0">
          <InfoTooltip text="Mostra quanto cada pessoa participou nas entradas e nas saidas do mes e destaca quem contribuiu ou gastou proporcionalmente mais." />
        </div>
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
              ? "border border-emerald-400/40 bg-emerald-500/12 text-[var(--income)]"
              : delta < 0
                ? "danger-chip"
                : "border border-amber-400/35 bg-amber-500/12 text-[var(--accent-amber)]";

            return (
              <div key={row.userId} className="rounded-3xl border border-[var(--surface-edge)] bg-[var(--card-strong)] p-4 shadow-[var(--elevated-shadow)] transition-transform duration-150 hover:scale-[1.01]">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="text-base font-semibold text-[var(--text-primary)]">{row.displayName}</p>
                    <p className="mt-1 text-xs text-[var(--text-secondary)]">Participacao nas entradas e nas saidas do periodo.</p>
                  </div>
                  <span className={`rounded-full px-3 py-1.5 text-xs font-medium ${statusClassName}`}>
                    {status}
                  </span>
                </div>
                <div className="mt-4 grid gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-[color-mix(in_srgb,var(--income)_22%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] px-4 py-3">
                    <p className="text-[11px] uppercase tracking-[0.08em] text-[var(--text-secondary)]">Entradas</p>
                    <p className="mt-1 font-display tabular-nums text-xl text-[var(--income)]">{formatCurrency(row.totalIncome)}</p>
                  </div>
                  <div className="rounded-2xl border border-[color-mix(in_srgb,var(--expense)_22%,transparent)] bg-[color-mix(in_srgb,var(--expense)_10%,transparent)] px-4 py-3">
                    <p className="text-[11px] uppercase tracking-[0.08em] text-[var(--text-secondary)]">Saidas</p>
                    <p className="mt-1 font-display tabular-nums text-xl text-[var(--expense)]">{formatCurrency(row.totalExpense)}</p>
                  </div>
                </div>
                <div className="mt-4 space-y-3">
                  <div className="space-y-1">
                    <div className="flex items-center justify-between text-[11px] text-[var(--text-muted)]">
                      <span>Participacao nas entradas</span>
                      {incomeShare ? <span>{incomeShare}</span> : null}
                    </div>
                    <div className="h-2.5 rounded-full bg-[var(--metric-track)]">
                      <div
                        className="h-2.5 rounded-full bg-[linear-gradient(90deg,color-mix(in_srgb,var(--income)_88%,white_12%),var(--accent-emerald))]"
                        style={{ width: `${clampPercentage(incomePercentage)}%` }}
                      />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center justify-between text-[11px] text-[var(--text-muted)]">
                      <span>Participacao nas saidas</span>
                      {expenseShare ? <span>{expenseShare}</span> : null}
                    </div>
                    <div className="h-2.5 rounded-full bg-[var(--metric-track)]">
                      <div
                        className="h-2.5 rounded-full bg-[linear-gradient(90deg,color-mix(in_srgb,var(--expense)_88%,white_12%),var(--accent-amber))]"
                        style={{ width: `${clampPercentage(expensePercentage)}%` }}
                      />
                    </div>
                  </div>
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
