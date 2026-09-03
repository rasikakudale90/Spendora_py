const API_BASE_URL = (
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8000"
).replace(/\/+$/, "");

// ── In-Memory Token Management ──────────────────────────────────────────────
let inMemoryAccessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
  inMemoryAccessToken = token;
};

export const getAccessToken = (): string | null => {
  return inMemoryAccessToken;
};

// ── Auth Types ──────────────────────────────────────────────────────────────
export interface User {
  id: string;
  email: string;
  full_name?: string | null;
  avatar_url?: string | null;
  auth_provider: string;
  is_active: boolean;
  is_verified: boolean;
  created_at: string;
}

export interface AuthSuccessResponse {
  user: User;
  access_token: string;
  token_type: string;
  expires_in: number;
}

export interface Category {
  id: string;
  name: string;
  created_at: string;
  updated_at: string;
  expense_count?: number;
}

export type PaymentMode = "Cash" | "Card" | "UPI" | "Net Banking" | "Other";

export interface DailyBudgetAlert {
  exceeded: boolean;
  limit_amount: string;
  total_spent: string;
  exceeded_amount: string;
  percentage_used: number;
  message: string;
}

export interface Expense {
  id: string;
  title: string;
  category_id: string;
  amount: string;
  expense_date: string;
  notes?: string | null;
  payment_mode?: PaymentMode | null;
  created_at: string;
  updated_at: string;
  category?: Category;
  daily_budget_alert?: DailyBudgetAlert | null;
}

export interface ExpenseListResponse {
  items: Expense[];
  total: number;
  page: number;
  page_size: number;
  total_pages: number;
}

export type PeriodType = "daily" | "weekly" | "monthly" | "yearly";

export interface Budget {
  id: string;
  scope: "overall" | "category";
  category_id?: string | null;
  category_name?: string | null;
  amount: string;
  period_type: PeriodType;
  period_start: string;
  period_end: string;
  period_month: string;
  spent: string;
  remaining: string;
  percentage_used: number;
  status: "on_track" | "near_limit" | "over_budget";
  created_at: string;
  updated_at: string;
}

export interface BudgetListResponse {
  period_type: PeriodType;
  period_start: string;
  period_end: string;
  overall_budget?: Budget | null;
  category_budgets: Budget[];
}

export interface Income {
  id: string;
  title: string;
  amount: string;
  income_date: string;
  source: string;
  payment_mode?: string | null;
  notes?: string | null;
  created_at: string;
  updated_at: string;
}

export interface IncomeListResponse {
  items: Income[];
  total: number;
  page: number;
  page_size: number;
  total_pages: number;
}

export interface SourceBreakdownItem {
  source: string;
  total_amount: string;
  percentage: number;
  count: number;
}

export interface IncomeSummaryResponse {
  period_month: string;
  total_income: string;
  income_count: number;
  breakdown_by_source: SourceBreakdownItem[];
}

export interface DashboardSummary {
  period_month: string;
  total_spent: string;
  total_budget?: string | null;
  remaining_budget?: string | null;
  percentage_used: number;
  status: "on_track" | "near_limit" | "over_budget" | "no_budget";
  expense_count: number;
  total_income: string;
  net_savings: string;
  savings_rate: number;
}

export interface CategoryBreakdownItem {
  category_id: string;
  category_name: string;
  amount: string;
  percentage: number;
}

export interface TrendItem {
  label: string;
  amount: string;
  expense_count: number;
}

export interface MonthComparison {
  current_month_spend: string;
  previous_month_spend: string;
  difference_amount: string;
  percentage_change: number;
  trend: "increased" | "decreased" | "unchanged";
}

export interface TopCategoryItem {
  rank: number;
  category_id: string;
  category_name: string;
  amount: string;
  percentage: number;
}

export interface DashboardStats {
  period_month: string;
  avg_daily_spend: string;
  avg_weekly_spend: string;
  highest_expense_amount?: string | null;
  highest_expense_title?: string | null;
  total_expense_count: number;
}

// ── Refresh Mutex / Single-flight Promise ───────────────────────────────────
let refreshPromise: Promise<string | null> | null = null;

async function silentRefresh(): Promise<string | null> {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (!res.ok) {
        setAccessToken(null);
        return null;
      }
      const data: AuthSuccessResponse = await res.json();
      setAccessToken(data.access_token);
      return data.access_token;
    } catch {
      setAccessToken(null);
      return null;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

async function fetchJson<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options?.headers as Record<string, string> || {}),
  };

  if (inMemoryAccessToken) {
    headers["Authorization"] = `Bearer ${inMemoryAccessToken}`;
  }

  let response = await fetch(url, {
    ...options,
    credentials: "include", // Send HttpOnly refresh cookies
    headers,
  });

  // Handle 401: attempt transparent token refresh once if not an auth endpoint
  if (
    response.status === 401 &&
    !endpoint.startsWith("/api/v1/auth/login") &&
    !endpoint.startsWith("/api/v1/auth/register") &&
    !endpoint.startsWith("/api/v1/auth/refresh")
  ) {
    const newToken = await silentRefresh();
    if (newToken) {
      headers["Authorization"] = `Bearer ${newToken}`;
      response = await fetch(url, {
        ...options,
        credentials: "include",
        headers,
      });
    }
  }

  if (!response.ok) {
    let errorDetail = "An unexpected error occurred";
    try {
      const err = await response.json();
      errorDetail = err.detail || err.message || errorDetail;
    } catch {
      errorDetail = response.statusText;
    }
    throw new Error(errorDetail);
  }

  return response.json();
}

// ── Auth API ─────────────────────────────────────────────────────────────────
export const authApi = {
  register: async (data: { email: string; password: string; full_name?: string }) => {
    return fetchJson<{ message: string; user: User }>("/api/v1/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  login: async (data: { email: string; password: string }) => {
    const res = await fetchJson<AuthSuccessResponse>("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
    setAccessToken(res.access_token);
    return res;
  },

  googleSignIn: async (credential: string) => {
    const res = await fetchJson<AuthSuccessResponse>("/api/v1/auth/google", {
      method: "POST",
      body: JSON.stringify({ credential }),
    });
    setAccessToken(res.access_token);
    return res;
  },

  refreshToken: async () => {
    const res = await fetchJson<AuthSuccessResponse>("/api/v1/auth/refresh", {
      method: "POST",
    });
    setAccessToken(res.access_token);
    return res;
  },

  logout: async () => {
    try {
      await fetchJson<{ message: string }>("/api/v1/auth/logout", {
        method: "POST",
      });
    } finally {
      setAccessToken(null);
    }
  },

  logoutAll: async () => {
    try {
      await fetchJson<{ message: string }>("/api/v1/auth/logout-all", {
        method: "POST",
      });
    } finally {
      setAccessToken(null);
    }
  },

  getMe: () => fetchJson<User>("/api/v1/auth/me"),

  forgotPassword: (email: string) =>
    fetchJson<{ message: string; dev_otp?: string; dev_reset_token?: string }>("/api/v1/auth/forgot-password", {
      method: "POST",
      body: JSON.stringify({ email }),
    }),

  verifyOtp: (email: string, otp: string) =>
    fetchJson<{ valid: boolean; message: string }>("/api/v1/auth/verify-otp", {
      method: "POST",
      body: JSON.stringify({ email, otp }),
    }),

  resetPassword: (email: string, otp: string, new_password: string) =>
    fetchJson<{ message: string }>("/api/v1/auth/reset-password", {
      method: "POST",
      body: JSON.stringify({ email, otp, new_password }),
    }),

  changePassword: (current_password: string, new_password: string) =>
    fetchJson<{ message: string }>("/api/v1/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ current_password, new_password }),
    }),
};

// ── Application Resources API ────────────────────────────────────────────────
export const api = {
  // Categories
  getCategories: () => fetchJson<Category[]>("/api/v1/categories"),
  createCategory: (name: string) =>
    fetchJson<Category>("/api/v1/categories", {
      method: "POST",
      body: JSON.stringify({ name }),
    }),
  renameCategory: (id: string, name: string) =>
    fetchJson<Category>(`/api/v1/categories/${id}`, {
      method: "PATCH",
      body: JSON.stringify({ name }),
    }),
  deleteCategory: (id: string, reassignTo?: string) =>
    fetchJson<{ message: string }>(
      `/api/v1/categories/${id}${reassignTo ? `?reassign_to=${reassignTo}` : ""}`,
      { method: "DELETE" }
    ),

  // Expenses
  getExpenses: (params?: Record<string, any>) => {
    const query = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
          query.append(key, String(value));
        }
      });
    }
    const qs = query.toString() ? `?${query.toString()}` : "";
    return fetchJson<ExpenseListResponse>(`/api/v1/expenses${qs}`);
  },
  getExpense: (id: string) => fetchJson<Expense>(`/api/v1/expenses/${id}`),
  createExpense: (data: {
    title: string;
    category_id: string;
    amount: string | number;
    expense_date: string;
    notes?: string;
    payment_mode?: PaymentMode;
  }) =>
    fetchJson<Expense>("/api/v1/expenses", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateExpense: (
    id: string,
    data: Partial<{
      title: string;
      category_id: string;
      amount: string | number;
      expense_date: string;
      notes?: string;
      payment_mode?: PaymentMode;
    }>
  ) =>
    fetchJson<Expense>(`/api/v1/expenses/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  deleteExpense: (id: string) =>
    fetchJson<{ message: string }>(`/api/v1/expenses/${id}`, {
      method: "DELETE",
    }),

  // Budgets
  getBudgets: (periodDate?: string, periodType: PeriodType = "monthly") => {
    const params = new URLSearchParams();
    if (periodDate) params.append("period_date", periodDate);
    if (periodType) params.append("period_type", periodType);
    const qs = params.toString() ? `?${params.toString()}` : "";
    return fetchJson<BudgetListResponse>(`/api/v1/budgets${qs}`);
  },
  setBudget: (data: {
    scope: "overall" | "category";
    category_id?: string | null;
    amount: string | number;
    period_type?: PeriodType;
    period_start?: string;
    period_month?: string;
  }) =>
    fetchJson<Budget>("/api/v1/budgets", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateBudget: (id: string, amount: string | number) =>
    fetchJson<Budget>(`/api/v1/budgets/${id}`, {
      method: "PATCH",
      body: JSON.stringify({ amount }),
    }),
  deleteBudget: (id: string) =>
    fetchJson<{ message: string }>(`/api/v1/budgets/${id}`, {
      method: "DELETE",
    }),

  // Dashboard
  getDashboardSummary: (periodMonth?: string) =>
    fetchJson<DashboardSummary>(
      `/api/v1/dashboard/summary${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
  getRecentExpenses: (limit: number = 5) =>
    fetchJson<Expense[]>(`/api/v1/dashboard/recent-expenses?limit=${limit}`),
  getCategoryBreakdown: (periodMonth?: string) =>
    fetchJson<CategoryBreakdownItem[]>(
      `/api/v1/dashboard/category-breakdown${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
  getTrend: (periodMonth?: string) =>
    fetchJson<TrendItem[]>(
      `/api/v1/dashboard/trend${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
  getComparison: (periodMonth?: string) =>
    fetchJson<MonthComparison>(
      `/api/v1/dashboard/comparison${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
  getTopCategories: (periodMonth?: string, limit: number = 5) =>
    fetchJson<TopCategoryItem[]>(
      `/api/v1/dashboard/top-categories?limit=${limit}${
        periodMonth ? `&period_month=${periodMonth}` : ""
      }`
    ),
  getDashboardStats: (periodMonth?: string) =>
    fetchJson<DashboardStats>(
      `/api/v1/dashboard/stats${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),

  // Incomes
  getIncomes: (params?: {
    search?: string;
    date_from?: string;
    date_to?: string;
    source?: string;
    min_amount?: number;
    max_amount?: number;
    sort_by?: string;
    sort_order?: "asc" | "desc";
    page?: number;
    page_size?: number;
  }) => {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== "") {
          searchParams.append(key, String(val));
        }
      });
    }
    const qs = searchParams.toString() ? `?${searchParams.toString()}` : "";
    return fetchJson<IncomeListResponse>(`/api/v1/incomes${qs}`);
  },
  getIncome: (id: string) => fetchJson<Income>(`/api/v1/incomes/${id}`),
  createIncome: (data: {
    title: string;
    amount: string | number;
    income_date: string;
    source?: string;
    payment_mode?: string;
    notes?: string;
  }) =>
    fetchJson<Income>("/api/v1/incomes", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateIncome: (
    id: string,
    data: Partial<{
      title: string;
      amount: string | number;
      income_date: string;
      source: string;
      payment_mode?: string;
      notes?: string;
    }>
  ) =>
    fetchJson<Income>(`/api/v1/incomes/${id}`, {
      method: "PATCH",
      body: JSON.stringify(data),
    }),
  deleteIncome: (id: string) =>
    fetchJson<{ message: string }>(`/api/v1/incomes/${id}`, {
      method: "DELETE",
    }),
  getIncomeSummary: (periodMonth?: string) =>
    fetchJson<IncomeSummaryResponse>(
      `/api/v1/incomes/summary${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
};

export interface PurchaseSimulationResponse {
  verdict: "safe" | "caution" | "over_budget";
  verdict_title: string;
  verdict_summary: string;
  item_title: string;
  item_amount: string | number;
  current_cash_flow: string | number;
  projected_cash_flow: string | number;
  current_savings_rate: number;
  projected_savings_rate: number;
  current_spent: string | number;
  projected_spent: string | number;
  overall_budget?: string | number | null;
  remaining_overall_budget?: string | number | null;
  projected_remaining_budget?: string | number | null;
  daily_safe_spend_before: string | number;
  daily_safe_spend_after: string | number;
  ai_analysis: string;
  actionable_tips: string[];
  provider_used: string;
}

export interface SubscriptionItem {
  title: string;
  average_amount: string | number;
  occurrence_count: number;
  last_date: string;
  estimated_monthly_cost: string | number;
  category_name?: string;
}

export interface MicroSpendingLeak {
  category_or_label: string;
  transaction_count: number;
  average_amount: string | number;
  monthly_total: string | number;
  annual_projected_drain: string | number;
  example_items: string[];
}

export interface LeakAnalysisResponse {
  total_monthly_leak: string | number;
  total_annual_projected_leak: string | number;
  total_monthly_subscriptions: string | number;
  total_annual_subscriptions: string | number;
  subscription_count: number;
  micro_leak_count: number;
  detected_subscriptions: SubscriptionItem[];
  micro_spending_leaks: MicroSpendingLeak[];
  ai_summary: string;
  actionable_savings_tips: string[];
  provider_used: string;
}

export const aiApi = {
  simulatePurchase: (data: {
    title: string;
    amount: string | number;
    category_id?: string | null;
  }) =>
    fetchJson<PurchaseSimulationResponse>("/api/v1/ai/simulate-purchase", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  getLeakAnalysis: () => fetchJson<LeakAnalysisResponse>("/api/v1/ai/leak-analysis"),
};
