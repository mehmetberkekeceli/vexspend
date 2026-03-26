export type TransactionType = 'INCOME' | 'EXPENSE';
export type CategoryType = 'INCOME' | 'EXPENSE';
export type AccountType = 'CASH' | 'BANK' | 'CREDIT_CARD' | 'E_WALLET' | 'OTHER';
export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: Record<string, string> | null;
}

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  fullName: string | null;
  profilePhotoUrl: string | null;
  enabled: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  expiresAt: string;
  user: UserProfile;
}

export interface DashboardCategorySpending {
  categoryId: string;
  categoryName: string;
  amount: number;
  percentage: number;
}

export interface DashboardMonthlyTrend {
  month: string;
  totalIncome: number;
  totalExpense: number;
  netCashflow: number;
}

export interface DashboardAccountBalance {
  accountId: string;
  accountName: string;
  currencyCode: string;
  currentBalance: number;
}

export interface DashboardReport {
  periodStart: string;
  periodEnd: string;
  totalIncome: number;
  totalExpense: number;
  netCashflow: number;
  budgetLimitTotal: number;
  budgetSpentTotal: number;
  budgetRemainingTotal: number;
  categorySpending: DashboardCategorySpending[];
  monthlyTrend: DashboardMonthlyTrend[];
  accountBalances: DashboardAccountBalance[];
}

export interface BudgetResponse {
  id: string;
  name: string;
  currencyCode: string;
  periodStart: string;
  periodEnd: string;
  totalLimit: number;
  spentAmount: number;
  remainingAmount: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BudgetItemResponse {
  id: string;
  budgetId: string;
  categoryId: string;
  categoryName: string;
  name: string;
  allocatedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryResponse {
  id: string;
  name: string;
  type: CategoryType;
  colorHex: string | null;
  icon: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AccountResponse {
  id: string;
  name: string;
  type: AccountType;
  currencyCode: string;
  currentBalance: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionResponse {
  id: string;
  type: TransactionType;
  amount: number;
  transactionDate: string;
  accountId: string;
  accountName: string;
  categoryId: string;
  categoryName: string;
  budgetId: string | null;
  budgetItemId: string | null;
  recurringTemplateId: string | null;
  merchant: string | null;
  note: string | null;
  createdAt: string;
}

export interface RecurringTransactionResponse {
  id: string;
  accountId: string;
  accountName: string;
  categoryId: string;
  categoryName: string;
  budgetId: string | null;
  budgetItemId: string | null;
  type: TransactionType;
  amount: number;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate: string | null;
  nextExecutionDate: string;
  lastExecutionDate: string | null;
  merchant: string | null;
  note: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProcessRecurringResult {
  processedDate: string;
  processedTemplateCount: number;
  createdTransactionIds: string[];
}

export interface CreateBudgetPayload {
  name: string;
  currencyCode: string;
  periodStart: string;
  periodEnd: string;
  totalLimit: number;
}

export interface CreateBudgetItemPayload {
  name: string;
  categoryId: string;
  allocatedAmount: number;
}

export interface CreateTransactionPayload {
  accountId: string;
  type: TransactionType;
  amount: number;
  transactionDate: string;
  categoryId: string;
  budgetId?: string;
  budgetItemId?: string;
  merchant?: string;
  note?: string;
}

export interface CreateCategoryPayload {
  name: string;
  type: CategoryType;
  colorHex?: string;
  icon?: string;
}

export interface CreateAccountPayload {
  name: string;
  type: AccountType;
  currencyCode: string;
  initialBalance: number;
}

export interface CreateRecurringPayload {
  accountId: string;
  categoryId: string;
  budgetId?: string;
  budgetItemId?: string;
  type: TransactionType;
  amount: number;
  frequency: RecurrenceFrequency;
  startDate: string;
  endDate?: string;
  merchant?: string;
  note?: string;
}

export interface UpdateMyProfileRequest {
  username?: string;
  email?: string;
  fullName?: string;
  profilePhotoUrl?: string;
}
