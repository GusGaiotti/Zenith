"use client";

interface TransactionFiltersProps {
  active: string;
  onChange: (value: string) => void;
}

const labels: Record<string, string> = {
  ALL: "Todos",
  INCOME: "Entrada",
  EXPENSE: "Saida",
};

export function TransactionFilters({ active, onChange }: TransactionFiltersProps) {
  return (
    <div className="surface mb-4 flex flex-wrap gap-2 p-4">
      {["ALL", "INCOME", "EXPENSE"].map((option) => {
        const isActive = active === option;
        const className = option === "INCOME"
          ? isActive
            ? "border-emerald-400/80 bg-emerald-500/15 text-emerald-200"
            : "border-emerald-500/25 bg-emerald-500/5 text-emerald-300 hover:border-emerald-400/50 hover:bg-emerald-500/10"
          : option === "EXPENSE"
            ? isActive
              ? "border-red-400/80 bg-red-500/15 text-red-200"
              : "border-red-500/25 bg-red-500/5 text-red-300 hover:border-red-400/50 hover:bg-red-500/10"
            : isActive
              ? "border-[var(--accent)] bg-[var(--accent-muted)] text-[var(--accent)]"
              : "border-[var(--surface-edge)] bg-white/5 text-[var(--text-secondary)] hover:bg-white/10";

        return (
          <button
            key={option}
            onClick={() => onChange(option)}
            className={`focusable rounded-full border px-3 py-1.5 text-xs transition-all duration-150 ${className}`}
          >
            {labels[option]}
          </button>
        );
      })}
    </div>
  );
}
