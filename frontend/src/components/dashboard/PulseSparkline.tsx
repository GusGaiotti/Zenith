import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency } from "@/lib/utils/currency";
import type { DashboardPulseResponse } from "@/types/api";

interface PulseSparklineProps {
  data?: DashboardPulseResponse;
}

export function PulseSparkline({ data }: PulseSparklineProps) {
  const bars = data?.dailySpending ?? [];
  const maxValue = Math.max(1, ...bars.map((bar) => Number(bar.totalExpense)));

  return (
    <section className="surface futura-card data-reveal hover-grow p-6">
      <div className="flex items-center justify-between">
        <h3 className="font-display text-2xl italic">Ritmo diario de gastos</h3>
        <InfoTooltip text="Mostra a intensidade diaria das saidas da fatura." />
      </div>
      <div className="mt-4 flex h-28 items-end gap-1 overflow-hidden">
        {(bars.length ? bars : new Array(16).fill({ totalExpense: 0 })).map((bar, index) => {
          const value = Number(bar.totalExpense);
          const height = Math.max(4, Math.min(100, Math.round((value / maxValue) * 100)));
          return (
            <div
              key={index}
              className={`bar-rise w-full rounded-sm ${value === 0 ? "bg-[var(--bg-muted)]" : index % 3 === 0 ? "bg-[color-mix(in_srgb,var(--accent)_72%,transparent)]" : index % 3 === 1 ? "bg-[color-mix(in_srgb,var(--accent-emerald)_72%,transparent)]" : "bg-[color-mix(in_srgb,var(--accent-amber)_72%,transparent)]"}`}
              style={{ height: `${height}%`, animationDelay: `${index * 35}ms` }}
              title={formatCurrency(value)}
            />
          );
        })}
      </div>
      <div className="mt-4 grid grid-cols-2 gap-2 text-sm text-[var(--text-secondary)]">
        <p>Media 7 dias: {formatCurrency(data?.sevenDayRollingAverage ?? 0)}</p>
        <p>Maior dia: {formatCurrency(data?.highestSpendingDay?.amount ?? 0)}</p>
        <p>Dias sem gasto: {data?.zeroSpendingDays ?? 0}</p>
        <p>Sequencia: {data?.currentSpendingStreak ?? 0} dias</p>
      </div>
    </section>
  );
}
