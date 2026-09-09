package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class DashboardSummary(
    @SerializedName("period_type") val periodType: String? = "monthly",
    @SerializedName("period_key") val periodKey: String? = null,
    @SerializedName("total_spent") val totalSpent: Double = 0.0,
    @SerializedName("total_income") val totalIncome: Double = 0.0,
    @SerializedName("net_savings") val netSavings: Double = 0.0,
    @SerializedName("savings_rate") val savingsRate: Double = 0.0,
    @SerializedName("active_budget_total") val activeBudgetTotal: Double = 0.0,
    @SerializedName("budget_remaining") val budgetRemaining: Double = 0.0,
    @SerializedName("budget_status") val budgetStatus: String = "ok",
    @SerializedName("expense_count") val expenseCount: Int = 0
)

data class CategorySpendItem(
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("percentage") val percentage: Double
)

data class DailySpendPoint(
    @SerializedName("date") val date: String,
    @SerializedName("amount") val amount: Double
)

data class MonthOverMonthStats(
    @SerializedName("current_month") val currentMonth: String,
    @SerializedName("current_spent") val currentSpent: Double,
    @SerializedName("previous_month") val previousMonth: String,
    @SerializedName("previous_spent") val previousSpent: Double,
    @SerializedName("percentage_change") val percentageChange: Double
)
