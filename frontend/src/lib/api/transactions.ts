import api from "@/lib/api/axios";
import type {
  CreateTransactionRequest,
  PageResponse,
  TransactionParams,
  TransactionResponse,
  UpdateTransactionRequest,
} from "@/types/api";

export const getTransactions = (ledgerId: number, params: TransactionParams) =>
  api.get<PageResponse<TransactionResponse>>(`/ledgers/${ledgerId}/transactions`, { params });

export const createTransaction = (ledgerId: number, body: CreateTransactionRequest) =>
  api.post<TransactionResponse>(`/ledgers/${ledgerId}/transactions`, body);

export const updateTransaction = (ledgerId: number, id: number, body: UpdateTransactionRequest) =>
  api.put<TransactionResponse>(`/ledgers/${ledgerId}/transactions/${id}`, body);

export const deleteTransaction = (ledgerId: number, id: number) =>
  api.delete<void>(`/ledgers/${ledgerId}/transactions/${id}`);

export const exportTransactionsExcel = (
  ledgerId: number,
  params: { startDate?: string; endDate?: string; createdBy?: number },
) =>
  api.get<Blob>(`/ledgers/${ledgerId}/transactions/export.xlsx`, {
    params,
    responseType: "blob",
    headers: {
      Accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    },
  });
