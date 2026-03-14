import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency } from "@/lib/utils/currency";
import { clampPercentage, formatPercentage } from "@/lib/utils/percent";
import type { DashboardCategoriesBreakdownResponse } from "@/types/api";

interface CategoryBreakdownChartProps {
  data?: DashboardCategoriesBreakdownResponse;
}

export function CategoryBreakdownChart({ data }: CategoryBreakdownChartProps) {
  const categories = data?.categories ?? [];
  const compact = categories.length <= 1;

  return (
    <section className="surface futura-card data-reveal hover-grow self-start p-6">
      <div className="relative pr-10">
        <h3 className="font-display text-2xl italic">Quebra por categoria</h3>
        <div className="absolute right-0 top-0">
          <InfoTooltip text="Mostra a concentração percentual dos gastos por categoria." />
        </div>
      </div>
      <div className={`mt-4 ${compact ? "space-y-3" : "space-y-2"}`}>
        {categories.length ? (
          categories.map((category) => (
            <div
              key={category.categoryId}
              className={`transition-transform duration-150 hover:scale-[1.01] ${compact ? "rounded-2xl border border-[var(--surface-edge)] bg-[var(--card-strong)] p-4" : "space-y-1"}`}
            >
              <div className="flex justify-between text-sm">
                <span>{category.name}</span>
                <span className="font-mono text-[var(--text-secondary)]">
                  {formatPercentage(category.percentageOfTotal)}
                </span>
              </div>
              <div className="mt-2 h-2.5 rounded-full bg-[var(--metric-track)]">
                <div
                  className="h-2.5 rounded-full"
                  style={{ width: `${clampPercentage(category.percentageOfTotal)}%`, backgroundColor: category.color }}
                />
              </div>
            </div>
          ))
        ) : (
          <p className="text-sm text-[var(--text-secondary)]">Nenhuma categoria ainda.</p>
        )}
      </div>
      <p className="mt-4 font-mono text-xl text-[var(--expense)]">{formatCurrency(data?.totalExpenses ?? 0)}</p>
    </section>
  );
}
