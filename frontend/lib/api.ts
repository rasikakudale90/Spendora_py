const API_BASE_URL = (
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8000"
).replace(/\/+$/, "");

export interface Category {
  id: string;
  name: string;
  created_at: string;
  updated_at: string;
  expense_count?: number;
}

export type PaymentMode = "Cash" | "Card" | "UPI" | "Net Banking" | "Other";

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
}

export interface ExpenseListResponse {
  items: Expense[];
  total: number;
  page: number;
  page_size: number;
  total_pages: number;
}

export interface Budget {
  id: string;
  scope: "overall" | "category";
  category_id?: string | null;
  category_name?: string | null;
  amount: string;
  period_month: string;
  spent: string;
  remaining: string;
  percentage_used: number;
  status: "on_track" | "near_limit" | "over_budget";
  created_at: string;
  updated_at: string;
}

export interface BudgetListResponse {
  overall_budget?: Budget | null;
  category_budgets: Budget[];
}

export interface DashboardSummary {
  period_month: string;
  total_spent: string;
  total_budget?: string | null;
  remaining_budget?: string | null;
  percentage_used: number;
  status: "on_track" | "near_limit" | "over_budget" | "no_budget";
  expense_count: number;
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

async function fetchJson<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
  });

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
  getBudgets: (periodMonth?: string) =>
    fetchJson<BudgetListResponse>(
      `/api/v1/budgets${periodMonth ? `?period_month=${periodMonth}` : ""}`
    ),
  setBudget: (data: {
    scope: "overall" | "category";
    category_id?: string | null;
    amount: string | number;
    period_month: string;
  }) =>
    fetchJson<Budget>("/api/v1/budgets", {
      method: "POST",
      body: JSON.stringify(data),
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
};
