"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { askAi, getAskAiUsage } from "@/lib/api/ai";
import type { AskAiRequest } from "@/types/api";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";

export function useAskAi() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useMutation({
    mutationFn: (payload: AskAiRequest) => askAi(requireLedgerId(ledgerId), payload).then((response) => response.data),
  });
}

export function useAskAiUsage(enabled: boolean) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useQuery({
    queryKey: ledgerId ? ["ask-ai", "usage", ledgerId] : ["ask-ai", "usage", "none"],
    queryFn: () => getAskAiUsage(requireLedgerId(ledgerId)).then((response) => response.data),
    enabled: enabled && Boolean(ledgerId),
    staleTime: 30_000,
    refetchInterval: enabled ? 30_000 : false,
  });
}
