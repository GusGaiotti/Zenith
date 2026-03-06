import api from "@/lib/api/axios";
import type { AskAiRequest, AskAiResponse } from "@/types/api";

export function askAi(ledgerId: number, payload: AskAiRequest) {
  return api.post<AskAiResponse>(`/ledgers/${ledgerId}/ai/ask`, payload);
}
