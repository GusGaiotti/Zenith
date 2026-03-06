"use client";

import { useMutation } from "@tanstack/react-query";
import { askAi } from "@/lib/api/ai";
import type { AskAiRequest } from "@/types/api";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";

export function useAskAi() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useMutation({
    mutationFn: (payload: AskAiRequest) => askAi(requireLedgerId(ledgerId), payload).then((response) => response.data),
  });
}
