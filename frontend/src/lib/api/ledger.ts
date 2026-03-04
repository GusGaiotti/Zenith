import api from "@/lib/api/axios";
import type { CreateLedgerRequest, InvitationResponse, InviteUserRequest, LedgerResponse } from "@/types/api";

export const createLedger = (body: CreateLedgerRequest) => api.post<LedgerResponse>("/ledgers", body);
export const getLedger = (id: number) => api.get<LedgerResponse>(`/ledgers/${id}`);
export const getMyLedger = () => api.get<LedgerResponse>("/ledgers/me");
export const inviteToLedger = (id: number, body: InviteUserRequest) =>
  api.post<InvitationResponse>(`/ledgers/${id}/invitations`, body);
export const acceptInvitation = (token: string) => api.patch<LedgerResponse>(`/ledgers/invitations/${token}/accept`);
export const declineInvitation = (token: string) =>
  api.patch<InvitationResponse>(`/ledgers/invitations/${token}/decline`);
