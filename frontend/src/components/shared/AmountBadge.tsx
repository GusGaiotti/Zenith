interface AmountBadgeProps {
  amount: number;
  type: "INCOME" | "EXPENSE";
}

import { formatCurrency } from "@/lib/utils/currency";

export function AmountBadge({ amount, type }: AmountBadgeProps) {
  const positive = type === "INCOME";
  return (
    <span className={`font-mono text-sm ${positive ? "text-[var(--income)]" : "text-[var(--expense)]"}`}>
      {positive ? "+" : "-"}
      {formatCurrency(Math.abs(amount))}
    </span>
  );
}

