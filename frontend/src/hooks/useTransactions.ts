"use client";

import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createTransaction,
  deleteTransaction,
  getTransactions,
  updateTransaction,
} from "@/lib/api/transactions";
import { queryKeys } from "@/lib/api/query-keys";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";
import type {
  CreateTransactionRequest,
  TransactionParams,
  UpdateTransactionRequest,
} from "@/types/api";

export function useTransactions(params: TransactionParams) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useInfiniteQuery({
    queryKey: ledgerId ? queryKeys.transactions(ledgerId, params) : ["transactions", "none", params],
    queryFn: ({ pageParam = 0 }) =>
      getTransactions(requireLedgerId(ledgerId), { ...params, page: pageParam, size: params.size ?? 20 }).then(
        (response) => response.data,
      ),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    enabled: Boolean(ledgerId),
  });
}

export function useCreateTransaction() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CreateTransactionRequest) =>
      createTransaction(requireLedgerId(ledgerId), body).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.transactionsRoot(ledgerId) });
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboardRoot(ledgerId) });
      }
    },
  });
}

export function useUpdateTransaction() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: UpdateTransactionRequest }) =>
      updateTransaction(requireLedgerId(ledgerId), id, body).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.transactionsRoot(ledgerId) });
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboardRoot(ledgerId) });
      }
    },
  });
}

export function useDeleteTransaction() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deleteTransaction(requireLedgerId(ledgerId), id),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.transactionsRoot(ledgerId) });
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboardRoot(ledgerId) });
      }
    },
  });
}
