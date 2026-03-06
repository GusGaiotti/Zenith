"use client";

import { useEffect, useId, useMemo, useRef, useState } from "react";
import { SelectMenu } from "@/components/shared/SelectMenu";
import { formatCurrency, normalizeCurrencyInput, parseCurrencyInputValue } from "@/lib/utils/currency";
import type { CategoryResponse, CreateTransactionRequest, TransactionResponse, TransactionType } from "@/types/api";

interface TransactionDrawerProps {
  open: boolean;
  categories: CategoryResponse[];
  onClose: () => void;
  onSubmit: (payload: CreateTransactionRequest) => void;
  isSubmitting?: boolean;
  initialTransaction?: TransactionResponse | null;
}

interface TransactionDrawerContentProps {
  categories: CategoryResponse[];
  onClose: () => void;
  onSubmit: (payload: CreateTransactionRequest) => void;
  isSubmitting: boolean;
  initialTransaction?: TransactionResponse | null;
}

function getTodayValue() {
  return new Date().toISOString().slice(0, 10);
}

function formatInitialAmount(value?: number) {
  if (!value) {
    return "";
  }

  return String(Math.round(Math.abs(value) * 100));
}

function TransactionDrawerContent({
  categories,
  onClose,
  onSubmit,
  isSubmitting,
  initialTransaction,
}: TransactionDrawerContentProps) {
  const [type, setType] = useState<TransactionType>(initialTransaction?.type ?? "EXPENSE");
  const [amountInput, setAmountInput] = useState(formatInitialAmount(initialTransaction?.amount));
  const [date, setDate] = useState(initialTransaction?.date ?? getTodayValue());
  const [categoryId, setCategoryId] = useState(initialTransaction?.categoryId ? String(initialTransaction.categoryId) : "");
  const [description, setDescription] = useState(initialTransaction?.description ?? "");
  const dialogRef = useRef<HTMLElement>(null);
  const titleId = useId();

  const amountValue = useMemo(() => parseCurrencyInputValue(amountInput), [amountInput]);
  const categoryOptions = useMemo(
    () => [
      { value: "", label: "Sem categoria" },
      ...categories.map((category) => ({
        value: String(category.id),
        label: category.name,
      })),
    ],
    [categories],
  );
  const isEditing = Boolean(initialTransaction);

  useEffect(() => {
    dialogRef.current?.focus();

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("keydown", handleEscape);
    };
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <section
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className="surface w-full max-w-lg p-6 shadow-[0_24px_60px_rgba(5,10,24,0.6)]"
      >
        <h2 id={titleId} className="font-display text-3xl">
          {isEditing ? "Editar transacao" : "Nova transacao"}
        </h2>
        <form
          className="mt-6 space-y-4"
          onSubmit={(event) => {
            event.preventDefault();

            if (amountValue <= 0) {
              return;
            }

            onSubmit({
              amount: amountValue,
              type,
              date,
              categoryId: categoryId ? Number(categoryId) : null,
              description: description.trim() || null,
            });
          }}
        >
          <label className="block">
            <span className="mb-1 block text-sm text-[var(--text-secondary)]">Valor</span>
            <input
              name="amountDisplay"
              type="text"
              inputMode="numeric"
              value={amountInput ? formatCurrency(amountValue) : "R$ 0,00"}
              onChange={(event) => setAmountInput(normalizeCurrencyInput(event.target.value))}
              required
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 font-mono text-[var(--text-primary)] outline-none"
            />
          </label>
          <div className="grid gap-2 sm:grid-cols-2">
            {(["INCOME", "EXPENSE"] as const).map((value) => {
              const active = type === value;
              const semanticClassName = value === "INCOME"
                ? active
                  ? "border-emerald-400/80 bg-emerald-500/15 text-emerald-200"
                  : "border-emerald-500/30 bg-emerald-500/5 text-emerald-300 hover:border-emerald-400/60 hover:bg-emerald-500/10"
                : active
                  ? "border-red-400/80 bg-red-500/15 text-red-200"
                  : "border-red-500/30 bg-red-500/5 text-red-300 hover:border-red-400/60 hover:bg-red-500/10";

              return (
                <button
                  type="button"
                  key={value}
                  aria-pressed={active}
                  className={`focusable rounded-full border px-3 py-2 text-sm font-medium transition-colors duration-150 ${semanticClassName}`}
                  onClick={() => setType(value)}
                >
                  {value === "INCOME" ? "Entrada" : "Saida"}
                </button>
              );
            })}
          </div>
          <label className="block">
            <span className="mb-1 block text-sm text-[var(--text-secondary)]">Data</span>
            <input
              name="date"
              type="date"
              required
              value={date}
              onChange={(event) => setDate(event.target.value)}
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 text-[var(--text-primary)] outline-none"
            />
          </label>
          <SelectMenu
            label="Categoria"
            value={categoryId}
            options={categoryOptions}
            onChange={setCategoryId}
            placeholder="Selecionar categoria"
          />
          <label className="block">
            <span className="mb-1 block text-sm text-[var(--text-secondary)]">Descricao</span>
            <input
              name="description"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Descricao"
              className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 text-[var(--text-primary)] outline-none placeholder:text-[var(--text-secondary)]"
            />
          </label>
          <div className="flex gap-2">
            <button
              disabled={isSubmitting || amountValue <= 0}
              className="focusable h-11 flex-1 rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isSubmitting ? "Salvando..." : isEditing ? "Salvar alteracoes" : "Salvar transacao"}
            </button>
            <button
              type="button"
              className="focusable h-11 rounded-xl border border-[var(--border)] px-4 py-2 text-sm text-[var(--text-secondary)] transition-colors duration-150 hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]"
              onClick={onClose}
            >
              Fechar
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

export function TransactionDrawer({
  open,
  categories,
  onClose,
  onSubmit,
  isSubmitting = false,
  initialTransaction,
}: TransactionDrawerProps) {
  if (!open) return null;

  return (
    <TransactionDrawerContent
      key={`drawer-${initialTransaction?.id ?? "new"}-${categories.length}`}
      categories={categories}
      onClose={onClose}
      onSubmit={onSubmit}
      isSubmitting={isSubmitting}
      initialTransaction={initialTransaction}
    />
  );
}
