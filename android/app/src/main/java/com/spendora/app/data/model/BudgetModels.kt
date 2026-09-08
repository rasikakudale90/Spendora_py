package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class BudgetDto(
    @SerializedName("id") val id: String,
    @SerializedName("scope") val scope: String, // "overall" or "category"
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("period_type") val periodType: String = "monthly", // "daily", "weekly", "monthly", "yearly"
    @SerializedName("period_start") val periodStart: String? = null,
    @SerializedName("period_end") val periodEnd: String? = null,
    @SerializedName("period_month") val periodMonth: String? = null,
    @SerializedName("spent") val spent: Double = 0.0,
    @SerializedName("remaining") val remaining: Double = 0.0,
    @SerializedName("percentage_used") val percentageUsed: Double = 0.0,
    @SerializedName("status") val status: String = "on_track", // "on_track", "near_limit", "over_budget"
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class BudgetCreateRequest(
    @SerializedName("scope") val scope: String,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("period_type") val periodType: String = "monthly",
    @SerializedName("period_start") val periodStart: String? = null
)

data class BudgetUpdateRequest(
    @SerializedName("amount") val amount: Double
)

data class BudgetListResponse(
    @SerializedName("period_type") val periodType: String = "monthly",
    @SerializedName("period_start") val periodStart: String,
    @SerializedName("period_end") val periodEnd: String,
    @SerializedName("overall_budget") val overallBudget: BudgetDto? = null,
    @SerializedName("category_budgets") val categoryBudgets: List<BudgetDto> = emptyList()
)
