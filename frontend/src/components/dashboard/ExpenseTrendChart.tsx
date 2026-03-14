import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency, formatCurrencyShort } from "@/lib/utils/currency";
import type { DashboardTrendsResponse } from "@/types/api";

interface ExpenseTrendChartProps {
  trends?: DashboardTrendsResponse;
}

const chartWidth = 520;
const chartHeight = 220;
const padding = { top: 16, right: 16, bottom: 34, left: 52 };
const plotWidth = chartWidth - padding.left - padding.right;
const plotHeight = chartHeight - padding.top - padding.bottom;
const months = [
  { value: "01", label: "Jan" },
  { value: "02", label: "Fev" },
  { value: "03", label: "Mar" },
  { value: "04", label: "Abr" },
  { value: "05", label: "Mai" },
  { value: "06", label: "Jun" },
  { value: "07", label: "Jul" },
  { value: "08", label: "Ago" },
  { value: "09", label: "Set" },
  { value: "10", label: "Out" },
  { value: "11", label: "Nov" },
  { value: "12", label: "Dez" },
] as const;

type ChartPoint = {
  yearMonth: string;
  label: string;
  totalExpense: number | null;
  totalIncome: number | null;
  net: number | null;
};

function pointTooltipLabel(point: ChartPoint, kind: "income" | "expense") {
  const value = kind === "income" ? point.totalIncome : point.totalExpense;
  const prefix = kind === "income" ? "Entradas" : "Saidas";

  if (value === null) {
    return `${point.label}: sem dados`;
  }

  return `${point.label}: ${prefix} ${formatCurrency(value)}`;
}

function linePath(values: Array<number | null>, maxValue: number) {
  let path = "";

  values.forEach((value, index) => {
    if (value === null) {
      return;
    }

    const x = padding.left + (plotWidth / (values.length - 1)) * index;
    const y = padding.top + plotHeight - (value / maxValue) * plotHeight;
    path += `${path ? " L" : "M"} ${x} ${y}`;
  });

  return path;
}

function yTickValues(maxValue: number) {
  return Array.from({ length: 4 }, (_, index) => (maxValue / 3) * index).reverse();
}

function MetricTooltip({ label, value, description, colorClassName }: { label: string; value: string; description: string; colorClassName: string }) {
  return (
    <div className="group relative rounded-2xl border border-[var(--surface-edge)] bg-[var(--card-strong)] px-4 py-3">
      <p className="cursor-help text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">{label}</p>
      <p className={`mt-2 font-mono text-sm ${colorClassName}`}>{value}</p>
      <div className="pointer-events-none absolute bottom-full left-0 z-50 mb-3 w-64 translate-y-1 rounded-xl border border-[var(--surface-edge)] bg-[var(--tooltip-bg)] px-3 py-2 text-left text-xs leading-5 text-white opacity-0 shadow-[0_18px_50px_rgba(0,0,0,0.45)] transition-all duration-150 group-hover:translate-y-0 group-hover:opacity-100" role="tooltip">
        {description}
      </div>
    </div>
  );
}

export function ExpenseTrendChart({ trends }: ExpenseTrendChartProps) {
  const sourcePoints = trends?.monthlyTrends ?? [];
  const inferredYear = Number(sourcePoints.at(-1)?.yearMonth.split("-")[0] ?? new Date().getFullYear());
  const pointsByMonth = new Map(sourcePoints.map((point) => [point.yearMonth, point]));
  const points: ChartPoint[] = months.map((month) => {
    const yearMonth = `${inferredYear}-${month.value}`;
    const point = pointsByMonth.get(yearMonth);

    return {
      yearMonth,
      label: month.label,
      totalExpense: point ? Number(point.totalExpense) : null,
      totalIncome: point ? Number(point.totalIncome) : null,
      net: point ? Number(point.net) : null,
    };
  });

  const expenses = points.map((point) => point.totalExpense);
  const incomes = points.map((point) => point.totalIncome);
  const numericValues = points.flatMap((point) => [point.totalExpense ?? 0, point.totalIncome ?? 0]);
  const maxAmount = Math.max(1, ...numericValues);
  const trendLabel = trends?.overallTrend === "IMPROVING"
    ? "MELHORANDO"
    : trends?.overallTrend === "DECLINING"
      ? "PIORANDO"
      : "ESTAVEL";
  const expensePath = linePath(expenses, maxAmount);
  const incomePath = linePath(incomes, maxAmount);
  const ticks = yTickValues(maxAmount);
  const populatedPoints = points.filter((point) => point.totalExpense !== null && point.totalIncome !== null);
  const latestPoint = [...populatedPoints].reverse()[0];
  const averageExpense = populatedPoints.length
    ? populatedPoints.reduce((total, point) => total + Number(point.totalExpense), 0) / populatedPoints.length
    : 0;
  const averageIncome = populatedPoints.length
    ? populatedPoints.reduce((total, point) => total + Number(point.totalIncome), 0) / populatedPoints.length
    : 0;

  return (
    <section className="surface futura-card data-reveal hover-grow p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="relative pr-10">
          <h3 className="font-display text-2xl italic">Tendencia de gastos</h3>
          <div className="absolute right-0 top-0">
            <InfoTooltip text="Grafico anual com todos os meses de janeiro a dezembro. Os pontos aparecem apenas nos meses que ja possuem dados, mantendo o contexto anual completo." />
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs">
          <span className="inline-flex items-center gap-2 rounded-full border border-[color-mix(in_srgb,var(--income)_32%,transparent)] bg-[color-mix(in_srgb,var(--income)_10%,transparent)] px-3 py-1 text-[var(--income)]">
            <span className="h-2 w-2 rounded-full bg-[var(--income)]" />
            Entradas
          </span>
          <span className="inline-flex items-center gap-2 rounded-full border border-[color-mix(in_srgb,var(--expense)_32%,transparent)] bg-[color-mix(in_srgb,var(--expense)_10%,transparent)] px-3 py-1 text-[var(--expense)]">
            <span className="h-2 w-2 rounded-full bg-[var(--expense)]" />
            Saidas
          </span>
          <span className="rounded-full bg-[var(--panel-bg)] px-2 py-1 text-[var(--text-secondary)]">{trendLabel}</span>
        </div>
      </div>

      {populatedPoints.length ? (
        <>
          <div className="rounded-2xl border border-[var(--surface-edge)] bg-[var(--card-strong)] p-3">
            <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} className="h-auto w-full" role="img" aria-label="Grafico anual de entradas e saidas">
              {ticks.map((tick) => {
                const y = padding.top + plotHeight - (tick / maxAmount) * plotHeight;

                return (
                  <g key={tick}>
                    <line x1={padding.left} y1={y} x2={chartWidth - padding.right} y2={y} stroke="var(--chart-grid)" strokeDasharray="4 4" />
                    <text x={padding.left - 10} y={y + 4} fill="var(--chart-label)" fontSize="10" textAnchor="end">
                      {formatCurrencyShort(tick)}
                    </text>
                  </g>
                );
              })}

              {expensePath ? (
                <path d={expensePath} fill="none" stroke="rgba(248, 113, 113, 0.92)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
              ) : null}
              {incomePath ? (
                <path d={incomePath} fill="none" stroke="rgba(74, 222, 128, 0.92)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
              ) : null}

              {points.map((point, index) => {
                const x = padding.left + (plotWidth / (points.length - 1)) * index;
                const expenseY = point.totalExpense === null ? null : padding.top + plotHeight - (Number(point.totalExpense) / maxAmount) * plotHeight;
                const incomeY = point.totalIncome === null ? null : padding.top + plotHeight - (Number(point.totalIncome) / maxAmount) * plotHeight;

                return (
                  <g key={point.yearMonth}>
                    {incomeY !== null ? (
                      <g>
                        <circle cx={x} cy={incomeY} r="10" fill="transparent">
                          <title>{pointTooltipLabel(point, "income")}</title>
                        </circle>
                        <circle cx={x} cy={incomeY} r="4" fill="rgba(74, 222, 128, 1)" />
                      </g>
                    ) : null}
                    {expenseY !== null ? (
                      <g>
                        <circle cx={x} cy={expenseY} r="10" fill="transparent">
                          <title>{pointTooltipLabel(point, "expense")}</title>
                        </circle>
                        <circle cx={x} cy={expenseY} r="4" fill="rgba(248, 113, 113, 1)" />
                      </g>
                    ) : null}
                    <text x={x} y={chartHeight - 10} fill="var(--chart-label)" fontSize="10" textAnchor="middle">
                      {point.label}
                    </text>
                  </g>
                );
              })}
            </svg>
          </div>

          <div className="mt-3 grid gap-3 md:grid-cols-3">
            <MetricTooltip
              label="Media saida"
              value={formatCurrency(averageExpense)}
              description="Media das saidas considerando apenas os meses que ja possuem movimentacao registrada."
              colorClassName="text-[var(--expense)]"
            />
            <MetricTooltip
              label="Media entrada"
              value={formatCurrency(averageIncome)}
              description="Media das entradas considerando apenas os meses que ja possuem movimentacao registrada."
              colorClassName="text-[var(--income)]"
            />
            <MetricTooltip
              label="Saldo do ultimo mes"
              value={formatCurrency(Number(latestPoint?.net ?? 0))}
              description="Saldo do mes mais recente com dados, calculado como entradas menos saidas."
              colorClassName={Number(latestPoint?.net ?? 0) >= 0 ? "text-[var(--income)]" : "text-[var(--expense)]"}
            />
          </div>
        </>
      ) : (
        <p className="text-sm text-[var(--text-secondary)]">Sem dados para o periodo selecionado.</p>
      )}
    </section>
  );
}
