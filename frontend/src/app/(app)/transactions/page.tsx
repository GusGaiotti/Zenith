"use client";

import { AxiosError } from "axios";
import { useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { EmptyState } from "@/components/shared/EmptyState";
import { MonthPicker } from "@/components/shared/MonthPicker";
import { SelectMenu } from "@/components/shared/SelectMenu";
import { PageHeader } from "@/components/shared/PageHeader";
import { TransactionDrawer } from "@/components/transactions/TransactionDrawer";
import { TransactionFilters } from "@/components/transactions/TransactionFilters";
import { TransactionTable } from "@/components/transactions/TransactionTable";
import { useCategories } from "@/hooks/useCategories";
import { useLedger } from "@/hooks/useLedger";
import { useCreateTransaction, useDeleteTransaction, useTransactions, useUpdateTransaction } from "@/hooks/useTransactions";
import { useAuthStore } from "@/lib/store/auth.store";
import { formatCurrency } from "@/lib/utils/currency";
import type { TransactionResponse, TransactionType } from "@/types/api";

function getNetBalanceClassName(value: number) {
  if (value > 0) {
    return "text-[var(--income)]";
  }

  if (value < 0) {
    return "text-[var(--expense)]";
  }

  return "text-amber-300";
}

function extractErrorMessage(error: unknown, fallback: string) {
  if (error instanceof AxiosError) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    return message || fallback;
  }

  return fallback;
}

export default function TransactionsPage() {
  const [filter, setFilter] = useState("ALL");
  const [open, setOpen] = useState(false);
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [memberFilter, setMemberFilter] = useState("all");
  const [editingTransaction, setEditingTransaction] = useState<TransactionResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [pendingDeleteId, setPendingDeleteId] = useState<number | null>(null);
  const searchParams = useSearchParams();
  const router = useRouter();
  const activeLedgerId = useAuthStore((state) => state.activeLedgerId);

  const openByQuery = searchParams.get("new") === "1";
  const drawerOpen = open || openByQuery;

  const typeFilter = filter === "ALL" ? undefined : (filter as TransactionType);
  const createdBy = memberFilter === "all" ? undefined : Number(memberFilter);

  const { startDate, endDate } = useMemo(() => {
    const [year, month] = yearMonth.split("-").map(Number);
    const start = `${yearMonth}-01`;
    const end = `${yearMonth}-${String(new Date(year, month, 0).getDate()).padStart(2, "0")}`;
    return { startDate: start, endDate: end };
  }, [yearMonth]);

  const categories = useCategories();
  const ledger = useLedger();
  const transactions = useTransactions({ type: typeFilter, size: 20, startDate, endDate, createdBy });
  const createTransaction = useCreateTransaction();
  const updateTransaction = useUpdateTransaction();
  const deleteTransaction = useDeleteTransaction();

  const items = useMemo(
    () => transactions.data?.pages.flatMap((page) => page.content) ?? [],
    [transactions.data?.pages],
  );

  const totals = useMemo(() => {
    const income = items.filter((item) => item.type === "INCOME").reduce((acc, item) => acc + Number(item.amount), 0);
    const expense = items
      .filter((item) => item.type === "EXPENSE")
      .reduce((acc, item) => acc + Math.abs(Number(item.amount)), 0);

    const expenseByPerson = items
      .filter((item) => item.type === "EXPENSE")
      .reduce<Record<string, number>>((acc, item) => {
        const name = item.createdByDisplayName || "Sem nome";
        acc[name] = (acc[name] ?? 0) + Math.abs(Number(item.amount));
        return acc;
      }, {});

    return { income, expense, net: income - expense, expenseByPerson };
  }, [items]);

  const memberOptions = [
    { value: "all", label: "Todas as pessoas" },
    ...(ledger.data?.members ?? []).map((member) => ({
      value: String(member.userId),
      label: member.displayName,
    })),
  ];

  const activeMutation = editingTransaction ? updateTransaction : createTransaction;

  if (!activeLedgerId) {
    return (
      <div className="pb-20 md:pb-6">
        <PageHeader
          title="Transações"
          subtitle="Acompanhe cada movimentação do espaço compartilhado."
        />
        <EmptyState
          title="Nenhuma fatura ativa"
          description="As transações ficam disponíveis assim que você criar uma fatura ou aceitar um convite."
          action={{ label: "Criar fatura", onClick: () => router.push("/onboarding") }}
        />
      </div>
    );
  }

  return (
    <div className="pb-20 md:pb-6">
      <PageHeader
        title="Transações"
        subtitle="Acompanhe cada movimentação do espaço compartilhado."
        actions={
          <div className="flex w-full flex-col gap-2 sm:flex-row sm:flex-wrap sm:items-center sm:justify-end">
            <MonthPicker value={yearMonth} onChange={setYearMonth} buttonClassName="w-full px-3 py-2 sm:min-w-[140px] sm:w-auto" />
            <SelectMenu
              value={memberFilter}
              options={memberOptions}
              onChange={setMemberFilter}
              placeholder="Filtrar por pessoa"
              buttonClassName="w-full px-3 py-2 sm:min-w-[190px] sm:w-auto"
              align="right"
            />
            <button
              className="focusable h-11 w-full rounded-xl bg-[var(--accent)] px-4 text-sm font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-colors duration-150 hover:bg-[var(--accent-hover)] sm:w-auto"
              onClick={() => {
                setEditingTransaction(null);
                setErrorMessage(null);
                setOpen(true);
              }}
            >
              Nova transação
            </button>
          </div>
        }
      />

      {errorMessage ? (
        <div className="mb-4 rounded-xl border border-[color-mix(in_srgb,var(--expense)_32%,transparent)] bg-[color-mix(in_srgb,var(--expense)_10%,var(--panel-bg))] px-4 py-3 text-sm text-[color-mix(in_srgb,var(--expense)_82%,var(--text-primary))]">
          {errorMessage}
        </div>
      ) : null}

      <div className="surface mb-4 grid gap-3 p-4 text-sm md:grid-cols-3">
        <p>
          Entradas <span className="ml-2 font-mono text-[var(--income)]">{formatCurrency(totals.income)}</span>
        </p>
        <p>
          Saídas <span className="ml-2 font-mono text-[var(--expense)]">{formatCurrency(totals.expense)}</span>
        </p>
        <p>
          Saldo{" "}
          <span className={`ml-2 inline-block max-w-full align-middle font-mono whitespace-nowrap ${getNetBalanceClassName(totals.net)}`}>
            {formatCurrency(totals.net)}
          </span>
        </p>
      </div>

      {Object.keys(totals.expenseByPerson).length ? (
        <div className="mb-4 flex flex-wrap gap-2">
          {Object.entries(totals.expenseByPerson).map(([name, total]) => (
            <span key={name} className="rounded-full border border-[var(--border)] bg-[var(--bg-elevated)] px-3 py-1 text-xs text-[var(--text-secondary)]">
              {name}: <span className="font-mono text-[var(--expense)]">{formatCurrency(total)}</span>
            </span>
          ))}
        </div>
      ) : null}

      <TransactionFilters active={filter} onChange={setFilter} />
      <TransactionTable
        items={items}
        deletingId={pendingDeleteId}
        onEdit={(item) => {
          setEditingTransaction(item);
          setErrorMessage(null);
          setOpen(true);
        }}
        onDelete={(item) => {
          const shouldDelete = window.confirm(`Excluir a transação "${item.description ?? "Sem descrição"}"?`);

          if (!shouldDelete) {
            return;
          }

          setErrorMessage(null);
          setPendingDeleteId(item.id);
          deleteTransaction.mutate(item.id, {
            onSuccess: () => {
              setPendingDeleteId(null);
            },
            onError: (error) => {
              setPendingDeleteId(null);
              setErrorMessage(extractErrorMessage(error, "Não foi possível excluir a transação."));
            },
          });
        }}
      />

      <div className="mt-4 flex items-center gap-2">
        <button
          disabled={!transactions.hasNextPage || transactions.isFetchingNextPage}
          onClick={() => transactions.fetchNextPage()}
          className="focusable rounded-xl border border-[var(--border)] px-3 py-2 text-sm text-[var(--text-secondary)] transition-all duration-150 hover:bg-[var(--bg-elevated)] disabled:cursor-not-allowed disabled:opacity-40"
        >
          {transactions.isFetchingNextPage ? "Carregando..." : "Carregar mais"}
        </button>
      </div>

      <TransactionDrawer
        open={drawerOpen}
        categories={categories.data ?? []}
        initialTransaction={editingTransaction}
        isSubmitting={activeMutation.isPending}
        onClose={() => {
          setOpen(false);
          setEditingTransaction(null);
          setErrorMessage(null);
          if (openByQuery) {
            router.replace("/transactions");
          }
        }}
        onSubmit={(payload) => {
          setErrorMessage(null);

          if (editingTransaction) {
            updateTransaction.mutate(
              { id: editingTransaction.id, body: payload },
              {
                onSuccess: () => {
                  setOpen(false);
                  setEditingTransaction(null);
                },
                onError: (error) => {
                  setErrorMessage(extractErrorMessage(error, "Não foi possível atualizar a transação."));
                },
              },
            );
            return;
          }

          createTransaction.mutate(payload, {
            onSuccess: () => {
              setOpen(false);
              if (openByQuery) {
                router.replace("/transactions");
              }
            },
            onError: (error) => {
              setErrorMessage(extractErrorMessage(error, "Não foi possível criar a transação."));
            },
          });
        }}
      />
    </div>
  );
}
