"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createCategory,
  deleteCategory,
  getCategories,
  updateCategory,
} from "@/lib/api/categories";
import { queryKeys } from "@/lib/api/query-keys";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";
import type { CreateCategoryRequest, UpdateCategoryRequest } from "@/types/api";

export function useCategories() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useQuery({
    queryKey: ledgerId ? queryKeys.categories(ledgerId) : ["categories", "none"],
    queryFn: () => getCategories(requireLedgerId(ledgerId)).then((response) => response.data),
    enabled: Boolean(ledgerId),
  });
}

export function useCreateCategory() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CreateCategoryRequest) =>
      createCategory(requireLedgerId(ledgerId), body).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.categories(ledgerId) });
      }
    },
  });
}

export function useUpdateCategory() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: UpdateCategoryRequest }) =>
      updateCategory(requireLedgerId(ledgerId), id, body).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.categories(ledgerId) });
      }
    },
  });
}

export function useDeleteCategory() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deleteCategory(requireLedgerId(ledgerId), id),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.categories(ledgerId) });
      }
    },
  });
}
