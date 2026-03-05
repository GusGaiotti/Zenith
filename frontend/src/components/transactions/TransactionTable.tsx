import { formatCurrency } from "@/lib/utils/currency";
import { formatDate } from "@/lib/utils/date";
import type { TransactionResponse } from "@/types/api";

interface TransactionTableProps {
  items: TransactionResponse[];
  onEdit: (item: TransactionResponse) => void;
  onDelete: (item: TransactionResponse) => void;
  deletingId?: number | null;
}

export function TransactionTable({ items, onEdit, onDelete, deletingId }: TransactionTableProps) {
  return (
    <div className="surface overflow-hidden">
      <div className="hidden grid-cols-6 gap-3 border-b border-[var(--border)] px-4 py-3 text-xs uppercase tracking-[0.08em] text-[var(--text-muted)] md:grid">
        <span>Data</span>
        <span>Descricao</span>
        <span>Categoria</span>
        <span>Pessoa</span>
        <span className="text-right">Valor</span>
        <span className="text-right">Acoes</span>
      </div>
      <ul>
        {items.map((item) => (
          <li
            key={item.id}
            className="border-b border-[var(--border)] px-4 py-3 text-sm transition-all duration-150 hover:translate-x-[2px] hover:bg-white/5"
          >
            <div className="space-y-3 md:hidden">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate font-medium text-[var(--text-primary)]">{item.description ?? "Sem descricao"}</p>
                  <p className="mt-1 text-xs text-[var(--text-secondary)]">{formatDate(item.date)}</p>
                </div>
                <p className={`shrink-0 font-mono ${item.type === "INCOME" ? "text-[var(--income)]" : "text-[var(--expense)]"}`}>
                  {item.type === "INCOME" ? "+" : "-"}
                  {formatCurrency(Math.abs(item.amount))}
                </p>
              </div>
              <div className="flex flex-wrap gap-2 text-xs text-[var(--text-secondary)]">
                <span className="rounded-full border border-[var(--surface-edge)] px-2 py-1">{item.categoryName ?? "Sem categoria"}</span>
                <span className="rounded-full border border-[var(--surface-edge)] px-2 py-1">{item.createdByDisplayName}</span>
                <span className="rounded-full border border-[var(--surface-edge)] px-2 py-1">
                  {item.type === "INCOME" ? "Entrada" : "Saida"}
                </span>
              </div>
              <div className="flex gap-2 text-xs">
                <button
                  type="button"
                  className="focusable rounded-full border border-[var(--surface-edge)] px-3 py-1 text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--text-primary)]"
                  onClick={() => onEdit(item)}
                >
                  Editar
                </button>
                <button
                  type="button"
                  className="focusable rounded-full border border-red-500/30 px-3 py-1 text-red-300 transition-colors duration-150 hover:border-red-400/60 hover:bg-red-500/10"
                  onClick={() => onDelete(item)}
                >
                  {deletingId === item.id ? "Excluindo..." : "Excluir"}
                </button>
              </div>
            </div>

            <div className="hidden gap-3 md:grid md:grid-cols-6">
              <span>{formatDate(item.date)}</span>
              <span>{item.description ?? "Sem descricao"}</span>
              <span className="text-[var(--text-secondary)]">{item.categoryName ?? "Sem categoria"}</span>
              <span className="text-[var(--text-secondary)]">{item.createdByDisplayName}</span>
              <span className={`text-right font-mono ${item.type === "INCOME" ? "text-[var(--income)]" : "text-[var(--expense)]"}`}>
                {item.type === "INCOME" ? "+" : "-"}
                {formatCurrency(Math.abs(item.amount))}
              </span>
              <span className="flex justify-end gap-2 text-xs">
                <button
                  type="button"
                  className="focusable rounded-full border border-[var(--surface-edge)] px-3 py-1 text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--text-primary)]"
                  onClick={() => onEdit(item)}
                >
                  Editar
                </button>
                <button
                  type="button"
                  className="focusable rounded-full border border-red-500/30 px-3 py-1 text-red-300 transition-colors duration-150 hover:border-red-400/60 hover:bg-red-500/10"
                  onClick={() => onDelete(item)}
                >
                  {deletingId === item.id ? "Excluindo..." : "Excluir"}
                </button>
              </span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
