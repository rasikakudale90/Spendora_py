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

data class DailyBudgetAlert(
    @SerializedName("exceeded") val isBreached: Boolean = false,
    @SerializedName("limit_amount") val dailyLimit: Double = 0.0,
    @SerializedName("total_spent") val todaySpent: Double = 0.0,
    @SerializedName("exceeded_amount") val exceededAmount: Double = 0.0,
    @SerializedName("percentage_used") val percentageUsed: Double = 0.0,
    @SerializedName("message") val message: String = ""
)

data class ExpenseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("expense_date") val expenseDate: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category") val category: CategoryDto? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("payment_mode") val paymentMode: PaymentMode = PaymentMode.OTHER,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("daily_budget_alert") val dailyBudgetAlert: DailyBudgetAlert? = null
) {
    val displayCategoryName: String
        get() = categoryName ?: category?.name ?: "General"
}

data class ExpenseCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("expense_date") val expenseDate: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("payment_mode") val paymentMode: PaymentMode = PaymentMode.OTHER,
    @SerializedName("notes") val notes: String? = null
)

data class ExpenseListResponse(
    @SerializedName("items") val items: List<ExpenseDto> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("page_size") val pageSize: Int = 20,
    @SerializedName("total_pages") val totalPages: Int = 1
)
