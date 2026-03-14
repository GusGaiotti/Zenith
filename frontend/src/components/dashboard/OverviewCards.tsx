import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency } from "@/lib/utils/currency";
import { formatPercentage, normalizePercentage } from "@/lib/utils/percent";
import type { DashboardOverviewResponse } from "@/types/api";

interface OverviewCardsProps {
  overview?: DashboardOverviewResponse;
}

function renderDelta(value: number | undefined) {
  const normalized = normalizePercentage(value ?? 0);

  if (normalized === 0) {
    return null;
  }

  const prefix = normalized > 0 ? "+" : "";
  return `${prefix}${formatPercentage(normalized)}`;
}

function getNetBalanceClassName(value: number) {
  if (value > 0) {
    return "text-[var(--income)]";
  }

  if (value < 0) {
    return "text-[var(--expense)]";
  }

  return "text-[var(--accent-amber)]";
}

function DeltaBadge({ value, description }: { value: string; description: string }) {
  return (
    <div className="group relative inline-flex items-center">
      <span className="cursor-help rounded-full border border-[var(--surface-edge)] bg-[var(--panel-bg)] px-2.5 py-1 text-xs font-medium text-[var(--text-secondary)]">
        {value}
      </span>
      <div className="pointer-events-none absolute bottom-full left-0 z-50 mb-3 w-64 translate-y-1 rounded-xl border border-[var(--surface-edge)] bg-[var(--tooltip-bg)] px-3 py-2 text-left text-xs leading-5 text-white opacity-0 shadow-[0_18px_50px_rgba(0,0,0,0.45)] transition-all duration-150 group-hover:translate-y-0 group-hover:opacity-100" role="tooltip">
        {description}
      </div>
    </div>
  );
}

export function OverviewCards({ overview }: OverviewCardsProps) {
  const stats = [
    {
      label: "Total de entradas",
      value: overview?.totalIncome ?? 0,
      delta: renderDelta(overview?.monthOverMonthIncomeChange),
      deltaDescription: "Variacao percentual das entradas em relacao ao mes anterior.",
      color: "text-[var(--income)]",
      toneClassName: "border-[color-mix(in_srgb,var(--income)_20%,transparent)] bg-[color-mix(in_srgb,var(--income)_7%,transparent)]",
      format: "currency",
      infoText: "Soma de todas as entradas registradas no mes filtrado.",
    },
    {
      label: "Total de saidas",
      value: overview?.totalExpense ?? 0,
      delta: renderDelta(overview?.monthOverMonthExpenseChange),
      deltaDescription: "Variacao percentual das saidas em relacao ao mes anterior.",
      color: "text-[var(--expense)]",
      toneClassName: "border-[color-mix(in_srgb,var(--expense)_20%,transparent)] bg-[color-mix(in_srgb,var(--expense)_7%,transparent)]",
      format: "currency",
      infoText: "Soma de todas as saidas registradas no mes filtrado.",
    },
    {
      label: "Saldo liquido",
      value: overview?.netBalance ?? 0,
      delta: "Mes atual",
      deltaDescription: "Resultado de entradas menos saidas no periodo atual.",
      color: getNetBalanceClassName(overview?.netBalance ?? 0),
      toneClassName: "border-[color-mix(in_srgb,var(--accent-amber)_20%,transparent)] bg-[color-mix(in_srgb,var(--accent-amber)_7%,transparent)]",
      format: "currency",
      infoText: "Mostra o saldo final do periodo. Fica verde quando positivo, vermelho quando negativo e amarelo quando zerado.",
    },
    {
      label: "Taxa de poupanca",
      value: overview?.savingsRate ?? 0,
      delta: "Eficiencia",
      deltaDescription: "Percentual das entradas que nao foi consumido pelas saidas.",
      color: "text-[var(--accent)]",
      toneClassName: "border-[color-mix(in_srgb,var(--accent)_20%,transparent)] bg-[color-mix(in_srgb,var(--accent)_7%,transparent)]",
      format: "percent",
      infoText: "Representa a proporcao de valor preservado no mes depois de descontar as saidas.",
    },
  ] as const;

  return (
    <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
      {stats.map((stat, index) => (
        <article
          key={stat.label}
          className="surface futura-card data-reveal relative flex min-h-[172px] flex-col p-5"
          style={{ animationDelay: `${index * 80}ms` }}
        >
          <div className="absolute right-5 top-5">
            <InfoTooltip text={stat.infoText} />
          </div>
          <div className={`inline-flex w-fit items-center rounded-full border px-3 py-1 text-[11px] uppercase tracking-[0.08em] text-[var(--text-secondary)] ${stat.toneClassName}`}>
            {stat.label}
          </div>
          <p className={`mt-5 font-display tabular-nums text-3xl whitespace-nowrap xl:text-[2.15rem] ${stat.color}`}>
            {stat.format === "currency" ? formatCurrency(stat.value) : formatPercentage(stat.value)}
          </p>
          <div className="mt-auto pt-3">
            {stat.delta ? <DeltaBadge value={stat.delta} description={stat.deltaDescription} /> : null}
          </div>
        </article>
      ))}
    </div>
  );
}
