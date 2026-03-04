export type TransactionType = "INCOME" | "EXPENSE";

export interface AuthResponse {
  accessToken: string;
  userId: number;
  email: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface MessageResponse {
  message: string;
}

export interface MemberResponse {
  userId: number;
  email: string;
  displayName: string;
  joinedAt: string;
}

export interface LedgerResponse {
  id: number;
  name: string;
  createdAt: string;
  members: MemberResponse[];
  pendingInvitations: InvitationResponse[];
}

export interface InvitationResponse {
  id: number;
  token: string;
  invitedEmail: string;
  invitedUserDisplayName?: string | null;
  invitedByDisplayName?: string | null;
  status: string;
  expiresAt: string;
}

export interface NotificationResponse {
  id: number;
  type: "TRANSACTION_CREATED" | "INVITATION_RECEIVED";
  title: string;
  body: string;
  actorDisplayName: string | null;
  referenceType: "TRANSACTION" | "INVITATION" | null;
  referenceId: number | null;
  invitationToken: string | null;
  createdAt: string;
  seenAt: string | null;
}

export interface NotificationListResponse {
  unreadCount: number;
  items: NotificationResponse[];
}

export interface CategoryResponse {
  id: number;
  name: string;
  color: string;
  createdAt: string;
  createdByUserId: number;
  createdByDisplayName: string;
}

export interface TransactionResponse {
  id: number;
  amount: number;
  type: TransactionType;
  date: string;
  categoryId: number | null;
  categoryName: string | null;
  description: string | null;
  createdByUserId: number;
  createdByDisplayName: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DashboardOverviewResponse {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
  savingsRate: number;
  dailyBurnRate: number;
  projectedEndOfMonthExpense: number;
  projectedEndOfMonthBalance: number;
  monthOverMonthExpenseChange: number;
  monthOverMonthIncomeChange: number;
}

export interface DashboardTrendPoint {
  yearMonth: string;
  totalIncome: number;
  totalExpense: number;
  net: number;
  savingsRate: number;
}

export interface DashboardTrendsResponse {
  monthlyTrends: DashboardTrendPoint[];
  overallTrend: "IMPROVING" | "DECLINING" | "STABLE";
  bestMonth: DashboardTrendPoint | null;
  worstMonth: DashboardTrendPoint | null;
}

export interface DashboardCoupleSplitResponse {
  userContributions: Array<{
    userId: number;
    email: string;
    displayName: string;
    totalIncome: number;
    totalExpense: number;
    incomePercentage: number;
    expensePercentage: number;
    netFairnessDelta: number;
  }>;
  highestTransaction: {
    amount: number;
    userDisplayName: string;
  } | null;
}

export interface DashboardCategoriesBreakdownResponse {
  categories: Array<{
    categoryId: number;
    name: string;
    color: string;
    totalSpent: number;
    percentageOfTotal: number;
    transactionCount: number;
    averageTransactionValue: number;
  }>;
  totalExpenses: number;
  topCategory: {
    categoryId: number;
    name: string;
    color: string;
    totalSpent: number;
    percentageOfTotal: number;
    transactionCount: number;
    averageTransactionValue: number;
  } | null;
  uncategorized: {
    total: number;
    count: number;
  } | null;
  mostFrequentCategory: string | null;
}

export interface DashboardPulseResponse {
  dailySpending: Array<{
    date: string;
    totalExpense: number;
  }>;
  sevenDayRollingAverage: number;
  highestSpendingDay: {
    date: string;
    amount: number;
  } | null;
  zeroSpendingDays: number;
  currentSpendingStreak: number;
}

export interface CreateLedgerRequest {
  name: string;
}

export interface InviteUserRequest {
  email: string;
}

export interface CreateCategoryRequest {
  name: string;
  color: string;
}

export interface UpdateCategoryRequest {
  name: string;
  color: string;
}

export interface CreateTransactionRequest {
  amount: number;
  type: TransactionType;
  date: string;
  categoryId?: number | null;
  description?: string | null;
}

export interface UpdateTransactionRequest {
  amount?: number;
  type?: TransactionType;
  date?: string;
  categoryId?: number | null;
  description?: string | null;
}

export interface TransactionParams {
  startDate?: string;
  endDate?: string;
  categoryId?: number;
  createdBy?: number;
  type?: TransactionType;
  page?: number;
  size?: number;
}

export interface MarkNotificationsSeenRequest {
  ids: number[];
}
