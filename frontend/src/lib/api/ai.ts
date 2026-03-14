import api from "@/lib/api/axios";
import type { AskAiRequest, AskAiResponse, AskAiUsageResponse } from "@/types/api";

export function askAi(ledgerId: number, payload: AskAiRequest) {
  return api.post<AskAiResponse>(`/ledgers/${ledgerId}/ai/ask`, payload, {
    timeout: 25_000,
  });
}

export function getAskAiUsage(ledgerId: number) {
  return api.get<AskAiUsageResponse>(`/ledgers/${ledgerId}/ai/usage`);
}
