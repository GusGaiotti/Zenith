export const queryKeys = {
  transactionsRoot: (ledgerId: number) => ["transactions", ledgerId] as const,
  transactions: (ledgerId: number, params?: unknown) => ["transactions", ledgerId, params] as const,
  categories: (ledgerId: number) => ["categories", ledgerId] as const,
  dashboardRoot: (ledgerId: number) => ["dashboard", ledgerId] as const,
  dashboard: (ledgerId: number, type: string) => ["dashboard", ledgerId, type] as const,
  ledger: (id: number) => ["ledger", id] as const,
  invitation: (token: string) => ["invitation", token] as const,
} as const;
