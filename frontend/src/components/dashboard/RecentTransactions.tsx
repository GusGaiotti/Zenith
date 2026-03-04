import Link from "next/link";
import { AmountBadge } from "@/components/shared/AmountBadge";
import { InfoTooltip } from "@/components/shared/InfoTooltip";
import { formatRelative } from "@/lib/utils/date";
import type { TransactionResponse } from "@/types/api";

interface RecentTransactionsProps {
  items?: TransactionResponse[];
}

export function RecentTransactions({ items = [] }: RecentTransactionsProps) {
  const lastFive = items.slice(0, 5);

  return (
    <section className="surface futura-card data-reveal hover-grow p-6">
      <div className="mb-4 flex items-center justify-between gap-3">
        <h3 className="font-display text-2xl italic">Recentes</h3>
        <div className="flex items-center gap-2">
          <InfoTooltip text="Lista as ultimas movimentacoes registradas dentro do filtro atual para consulta rapida." />
          <Link className="text-xs text-[var(--accent)]" href="/transactions">
            Ver todas
          </Link>
        </div>
      </div>
      <ul className="space-y-3">
        {lastFive.length ? (
          lastFive.map((item) => (
            <li key={item.id} className="flex items-center justify-between text-sm transition-transform duration-150 hover:scale-[1.01]">
              <div>
                <p>{item.description ?? "Sem descricao"}</p>
                <p className="text-xs text-[var(--text-secondary)]">{formatRelative(item.date)}</p>
              </div>
              <AmountBadge amount={item.amount} type={item.type} />
            </li>
          ))
        ) : (
          <li className="text-sm text-[var(--text-secondary)]">Sem transacoes recentes.</li>
        )}
      </ul>
    </section>
  );
}
