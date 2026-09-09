package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

enum class PaymentMode(val value: String) {
    @SerializedName("Cash") CASH("Cash"),
    @SerializedName("Card") CARD("Card"),
    @SerializedName("UPI") UPI("UPI"),
    @SerializedName("Net Banking") NET_BANKING("Net Banking"),
    @SerializedName("Other") OTHER("Other");

    override fun toString(): String = value
}

data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("is_default") val isDefault: Boolean = false,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class CategoryCreateRequest(
    @SerializedName("name") val name: String
)

data class ExpenseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("expense_date") val expenseDate: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("payment_mode") val paymentMode: PaymentMode = PaymentMode.OTHER,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ExpenseCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("expense_date") val expenseDate: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("payment_mode") val paymentMode: PaymentMode = PaymentMode.OTHER,
    @SerializedName("notes") val notes: String? = null
)

data class DailyBudgetAlert(
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("daily_limit") val dailyLimit: Double,
    @SerializedName("today_spent") val todaySpent: Double,
    @SerializedName("is_breached") val isBreached: Boolean
)

data class ExpenseCreateResponse(
    @SerializedName("expense") val expense: ExpenseDto,
    @SerializedName("daily_budget_alert") val dailyBudgetAlert: DailyBudgetAlert? = null
)

data class ExpenseListResponse(
    @SerializedName("items") val items: List<ExpenseDto>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int
)
