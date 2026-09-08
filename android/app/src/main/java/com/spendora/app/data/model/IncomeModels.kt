package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class IncomeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("income_date") val incomeDate: String,
    @SerializedName("source") val source: String = "Salary",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class IncomeCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("income_date") val incomeDate: String,
    @SerializedName("source") val source: String = "Salary",
    @SerializedName("notes") val notes: String? = null
)

data class IncomeListResponse(
    @SerializedName("items") val items: List<IncomeDto>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class MonthlyIncomeSummary(
    @SerializedName("month") val month: String,
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("income_count") val incomeCount: Int
)
