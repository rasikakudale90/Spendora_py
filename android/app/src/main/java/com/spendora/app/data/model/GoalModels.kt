package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class GoalRunwayForecast(
    @SerializedName("required_monthly_savings") val requiredMonthlySavings: Double,
    @SerializedName("projected_completion_date") val projectedCompletionDate: String? = null,
    @SerializedName("months_remaining") val monthsRemaining: Double? = null,
    @SerializedName("days_remaining") val daysRemaining: Int? = null,
    @SerializedName("pacing_status") val pacingStatus: String, // "on_track", "ahead", "at_risk", "behind", "completed", "no_deadline"
    @SerializedName("pacing_message") val pacingMessage: String,
    @SerializedName("speedup_suggestion") val speedupSuggestion: String? = null
)

data class GoalDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double = 0.0,
    @SerializedName("remaining_amount") val remainingAmount: Double = 0.0,
    @SerializedName("progress_percentage") val progressPercentage: Double = 0.0,
    @SerializedName("target_date") val targetDate: String? = null,
    @SerializedName("category") val category: String = "Savings",
    @SerializedName("color") val color: String = "emerald",
    @SerializedName("status") val status: String = "active", // "active", "completed", "paused"
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("runway") val runway: GoalRunwayForecast? = null
)

data class GoalCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double = 0.0,
    @SerializedName("target_date") val targetDate: String? = null,
    @SerializedName("category") val category: String = "Savings",
    @SerializedName("color") val color: String = "emerald",
    @SerializedName("notes") val notes: String? = null
)

data class GoalUpdateRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("target_amount") val targetAmount: Double? = null,
    @SerializedName("current_amount") val currentAmount: Double? = null,
    @SerializedName("target_date") val targetDate: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class GoalContributionRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("action") val action: String = "deposit", // "deposit" or "withdraw"
    @SerializedName("notes") val notes: String? = null
)

data class GoalListResponse(
    @SerializedName("items") val items: List<GoalDto> = emptyList(),
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("total_target_amount") val totalTargetAmount: Double = 0.0,
    @SerializedName("total_saved_amount") val totalSavedAmount: Double = 0.0,
    @SerializedName("total_remaining_amount") val totalRemainingAmount: Double = 0.0,
    @SerializedName("overall_progress_percentage") val overallProgressPercentage: Double = 0.0
)
