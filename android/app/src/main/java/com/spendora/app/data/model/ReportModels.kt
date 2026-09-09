package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class CategoryAllocationDto(
    val category: String,
    val amount: Double,
    val percentage: Double
)

data class StatementSummaryDto(
    @SerializedName("period_month")
    val periodMonth: String,
    @SerializedName("period_start")
    val periodStart: String,
    @SerializedName("period_end")
    val periodEnd: String,
    @SerializedName("total_income")
    val totalIncome: Double,
    @SerializedName("total_spent")
    val totalSpent: Double,
    @SerializedName("net_cash_flow")
    val netCashFlow: Double,
    @SerializedName("savings_rate_pct")
    val savingsRatePct: Double,
    @SerializedName("expense_count")
    val expenseCount: Int,
    @SerializedName("income_count")
    val incomeCount: Int,
    @SerializedName("category_allocations")
    val categoryAllocations: List<CategoryAllocationDto> = emptyList()
)
