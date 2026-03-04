import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatCurrency } from "@/lib/utils/currency";
import { clampPercentage, formatPercentage } from "@/lib/utils/percent";
import type { DashboardCategoriesBreakdownResponse } from "@/types/api";

interface CategoryBreakdownChartProps {
  data?: DashboardCategoriesBreakdownResponse;
}

export function CategoryBreakdownChart({ data }: CategoryBreakdownChartProps) {
  const categories = data?.categories ?? [];

  return (
    <section className="surface futura-card data-reveal hover-grow p-6">
      <div className="flex items-center justify-between">
        <h3 className="font-display text-2xl italic">Quebra por categoria</h3>
        <InfoTooltip text="Mostra a concentracao percentual dos gastos por categoria." />
      </div>
      <div className="mt-4 space-y-2">
        {categories.length ? (
          categories.map((category) => (
            <div key={category.categoryId} className="space-y-1 transition-transform duration-150 hover:scale-[1.01]">
              <div className="flex justify-between text-sm">
                <span>{category.name}</span>
                <span className="font-mono text-[var(--text-secondary)]">
                  {formatPercentage(category.percentageOfTotal)}
                </span>
              </div>
              <div className="h-2 rounded-full bg-white/5">
                <div
                  className="h-2 rounded-full"
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
