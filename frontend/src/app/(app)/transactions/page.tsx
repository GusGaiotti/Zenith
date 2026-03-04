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
          title="Transacoes"
          subtitle="Acompanhe cada movimentacao do espaco compartilhado."
        />
        <EmptyState
          title="Nenhuma fatura ativa"
          description="As transacoes ficam disponiveis assim que voce criar uma fatura ou aceitar um convite."
          action={{ label: "Criar fatura", onClick: () => router.push("/onboarding") }}
        />
      </div>
    );
  }

  return (
    <div className="pb-20 md:pb-6">
      <PageHeader
        title="Transacoes"
        subtitle="Acompanhe cada movimentacao do espaco compartilhado."
        actions={
          <div className="flex items-center gap-2">
            <MonthPicker value={yearMonth} onChange={setYearMonth} buttonClassName="min-w-[140px] px-3 py-2" />
            <SelectMenu
              value={memberFilter}
              options={memberOptions}
              onChange={setMemberFilter}
              placeholder="Filtrar por pessoa"
              buttonClassName="min-w-[190px] px-3 py-2"
              align="right"
            />
            <button
              className="focusable rounded-md bg-[var(--accent)] px-3 py-2 text-sm font-medium text-black"
              onClick={() => {
                setEditingTransaction(null);
                setErrorMessage(null);
                setOpen(true);
              }}
            >
              Nova transacao
            </button>
          </div>
        }
      />

      {errorMessage ? (
        <div className="mb-4 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {errorMessage}
        </div>
      ) : null}

      <div className="mb-4 grid gap-2 rounded-lg border border-[var(--border)] bg-white/5 p-4 text-sm md:grid-cols-3">
        <p>
          Entradas <span className="ml-2 font-mono text-[var(--income)]">{formatCurrency(totals.income)}</span>
        </p>
        <p>
          Saidas <span className="ml-2 font-mono text-[var(--expense)]">{formatCurrency(totals.expense)}</span>
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
            <span key={name} className="rounded-full border border-[var(--border)] bg-white/5 px-3 py-1 text-xs text-[var(--text-secondary)]">
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
          const shouldDelete = window.confirm(`Excluir a transacao "${item.description ?? "Sem descricao"}"?`);

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
              setErrorMessage(extractErrorMessage(error, "Nao foi possivel excluir a transacao."));
            },
          });
        }}
      />

      <div className="mt-4 flex items-center gap-2">
        <button
          disabled={!transactions.hasNextPage || transactions.isFetchingNextPage}
          onClick={() => transactions.fetchNextPage()}
          className="focusable rounded-md border px-3 py-2 text-sm text-[var(--text-secondary)] transition-all duration-150 hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-40"
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
                  setErrorMessage(extractErrorMessage(error, "Nao foi possivel atualizar a transacao."));
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
              setErrorMessage(extractErrorMessage(error, "Nao foi possivel criar a transacao."));
            },
          });
        }}
      />
    </div>
  );
}
