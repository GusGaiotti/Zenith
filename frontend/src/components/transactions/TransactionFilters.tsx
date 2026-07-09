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
        const className =
          option === "INCOME"
            ? isActive
              ? "border-[color-mix(in_srgb,var(--income)_80%,transparent)] bg-[color-mix(in_srgb,var(--income)_15%,transparent)] text-[var(--income)]"
              : "border-[color-mix(in_srgb,var(--income)_25%,transparent)] bg-[color-mix(in_srgb,var(--income)_5%,transparent)] text-[var(--income)] hover:border-[color-mix(in_srgb,var(--income)_50%,transparent)] hover:bg-[color-mix(in_srgb,var(--income)_10%,transparent)]"
            : option === "EXPENSE"
              ? isActive
                ? "border-[var(--danger-border)] bg-[var(--danger-bg)] text-[var(--danger-text)]"
                : "border-[var(--danger-border)] bg-[var(--danger-bg)] text-[var(--danger-text)] hover:border-[var(--danger-border)] hover:bg-[var(--danger-bg)]"
              : isActive
                ? "border-[var(--accent)] bg-[var(--accent-muted)] text-[var(--accent-hover)]"
                : "border-[var(--surface-edge)] bg-[var(--bg-elevated)] text-[var(--text-secondary)] hover:bg-[var(--bg-muted)]";

        return (
          <button
            key={option}
            onClick={() => onChange(option)}
            className={`focusable rounded-full border px-3 py-1.5 text-xs font-medium transition-all duration-150 ${className}`}
          >
            {labels[option]}
          </button>
        );
      })}
    </div>
  );
}
