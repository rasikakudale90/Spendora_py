package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

// ── Feature 1: Purchase Decision Simulator ──────────────────────────
data class PurchaseSimulationRequest(
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category_id") val categoryId: String? = null
)

data class PurchaseSimulationResponse(
    @SerializedName("verdict") val verdict: String, // "safe", "caution", "over_budget"
    @SerializedName("verdict_title") val verdictTitle: String,
    @SerializedName("verdict_summary") val verdictSummary: String,
    @SerializedName("item_title") val itemTitle: String,
    @SerializedName("item_amount") val itemAmount: Double,
    @SerializedName("current_cash_flow") val currentCashFlow: Double,
    @SerializedName("projected_cash_flow") val projectedCashFlow: Double,
    @SerializedName("current_savings_rate") val currentSavingsRate: Double,
    @SerializedName("projected_savings_rate") val projectedSavingsRate: Double,
    @SerializedName("current_spent") val currentSpent: Double,
    @SerializedName("projected_spent") val projectedSpent: Double,
    @SerializedName("overall_budget") val overallBudget: Double? = null,
    @SerializedName("remaining_overall_budget") val remainingOverallBudget: Double? = null,
    @SerializedName("projected_remaining_budget") val projectedRemainingBudget: Double? = null,
    @SerializedName("daily_safe_spend_before") val dailySafeSpendBefore: Double,
    @SerializedName("daily_safe_spend_after") val dailySafeSpendAfter: Double,
    @SerializedName("ai_analysis") val aiAnalysis: String,
    @SerializedName("actionable_tips") val actionableTips: List<String> = emptyList(),
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)

// ── Feature 2: Leak Hunter & Subscription Audit ─────────────────────
data class SubscriptionItem(
    @SerializedName("title") val title: String,
    @SerializedName("average_amount") val averageAmount: Double,
    @SerializedName("occurrence_count") val occurrenceCount: Int,
    @SerializedName("last_date") val lastDate: String,
    @SerializedName("estimated_monthly_cost") val estimatedMonthlyCost: Double,
    @SerializedName("category_name") val categoryName: String? = "Subscription"
)

data class MicroSpendingLeak(
    @SerializedName("category_or_label") val categoryOrLabel: String,
    @SerializedName("transaction_count") val transactionCount: Int,
    @SerializedName("average_amount") val averageAmount: Double,
    @SerializedName("monthly_total") val monthlyTotal: Double,
    @SerializedName("annual_projected_drain") val annualProjectedDrain: Double,
    @SerializedName("example_items") val exampleItems: List<String> = emptyList()
)

data class LeakAnalysisResponse(
    @SerializedName("total_monthly_leak") val totalMonthlyLeak: Double,
    @SerializedName("total_annual_projected_leak") val totalAnnualProjectedLeak: Double,
    @SerializedName("total_monthly_subscriptions") val totalMonthlySubscriptions: Double,
    @SerializedName("total_annual_subscriptions") val totalAnnualSubscriptions: Double,
    @SerializedName("subscription_count") val subscriptionCount: Int,
    @SerializedName("micro_leak_count") val microLeakCount: Int,
    @SerializedName("detected_subscriptions") val detectedSubscriptions: List<SubscriptionItem> = emptyList(),
    @SerializedName("micro_spending_leaks") val microSpendingLeaks: List<MicroSpendingLeak> = emptyList(),
    @SerializedName("ai_summary") val aiSummary: String,
    @SerializedName("actionable_savings_tips") val actionableSavingsTips: List<String> = emptyList(),
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)

// ── Feature 3: Safe-to-Spend Speedometer & Burn Forecaster ──────────
data class SafeToSpendResponse(
    @SerializedName("daily_safe_spend") val dailySafeSpend: Double,
    @SerializedName("burn_rate_status") val burnRateStatus: String, // "optimal", "warning", "danger"
    @SerializedName("current_burn_rate_per_day") val currentBurnRatePerDay: Double,
    @SerializedName("days_remaining_in_month") val daysRemainingInMonth: Int,
    @SerializedName("days_passed") val daysPassed: Int,
    @SerializedName("total_monthly_income") val totalMonthlyIncome: Double,
    @SerializedName("total_spent_so_far") val totalSpentSoFar: Double,
    @SerializedName("remaining_buffer") val remainingBuffer: Double,
    @SerializedName("projected_month_end_balance") val projectedMonthEndBalance: Double,
    @SerializedName("projected_zero_cash_day") val projectedZeroCashDay: Int? = null,
    @SerializedName("burn_pace_percentage") val burnPacePercentage: Double,
    @SerializedName("ai_recommendation") val aiRecommendation: String,
    @SerializedName("actionable_tips") val actionableTips: List<String> = emptyList(),
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)

// ── Feature 4: Natural Language Conversational Assistant ────────────
data class ChatMessage(
    @SerializedName("role") val role: String, // "user", "assistant", "system"
    @SerializedName("content") val content: String,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("action_intent") val actionIntent: FinancialActionIntent? = null
)

data class FinancialActionIntent(
    @SerializedName("action") val action: String, // "simulate_purchase", "view_leaks", "navigate", "set_budget", "add_expense", "none"
    @SerializedName("label") val label: String,
    @SerializedName("payload") val payload: Map<String, Any>? = null
)

data class FinancialChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("history") val history: List<ChatMessage> = emptyList()
)

data class FinancialChatResponse(
    @SerializedName("reply") val reply: String,
    @SerializedName("suggested_prompts") val suggestedPrompts: List<String> = emptyList(),
    @SerializedName("action_intent") val actionIntent: FinancialActionIntent? = null,
    @SerializedName("context_summary") val contextSummary: Map<String, Any>? = null,
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)

// ── Feature 5: Smart Receipt & UPI SMS Parser ───────────────────────
data class ExtractedItem(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("category_name") val categoryName: String? = null
)

data class TransactionExtractionRequest(
    @SerializedName("text") val text: String? = null,
    @SerializedName("image_base64") val imageBase64: String? = null,
    @SerializedName("source_type") val sourceType: String = "sms_text" // "sms_text" or "receipt_image"
)

data class TransactionExtractionResponse(
    @SerializedName("type") val type: String = "expense", // "expense" or "income"
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("payment_mode") val paymentMode: String = "UPI",
    @SerializedName("raw_reference") val rawReference: String? = null,
    @SerializedName("is_potential_duplicate") val isPotentialDuplicate: Boolean = false,
    @SerializedName("duplicate_warning") val duplicateWarning: String? = null,
    @SerializedName("items") val items: List<ExtractedItem> = emptyList(),
    @SerializedName("confidence_score") val confidenceScore: Double = 0.95,
    @SerializedName("extraction_method") val extractionMethod: String = "regex_engine",
    @SerializedName("sanitized_input") val sanitizedInput: String
)

// ── Feature 6: AI Financial Health Score & 5-Pillar Radar ───────────
data class PillarScore(
    @SerializedName("name") val name: String,
    @SerializedName("score") val score: Double, // 0 to 100
    @SerializedName("weight_pct") val weightPct: Int,
    @SerializedName("benchmark_label") val benchmarkLabel: String,
    @SerializedName("status") val status: String, // "optimal", "good", "fair", "critical"
    @SerializedName("insight") val insight: String
)

data class ScoreBoosterAction(
    @SerializedName("pillar") val pillar: String,
    @SerializedName("impact_points") val impactPoints: Int,
    @SerializedName("action_text") val actionText: String,
    @SerializedName("category_hint") val categoryHint: String? = null
)

data class FinancialHealthResponse(
    @SerializedName("composite_score") val compositeScore: Int, // 0 to 100
    @SerializedName("tier") val tier: String, // "elite", "healthy", "vulnerable", "critical"
    @SerializedName("tier_title") val tierTitle: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("pillars") val pillars: List<PillarScore> = emptyList(),
    @SerializedName("score_boosters") val scoreBoosters: List<ScoreBoosterAction> = emptyList(),
    @SerializedName("monthly_income") val monthlyIncome: Double = 0.0,
    @SerializedName("monthly_spent") val monthlySpent: Double = 0.0,
    @SerializedName("monthly_net_savings") val monthlyNetSavings: Double = 0.0,
    @SerializedName("savings_rate_pct") val savingsRatePct: Double = 0.0,
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)

// ── Feature 7: Smart Goal Runway & Acceleration Intelligence ────────
data class GoalRunwayAnalysisItem(
    @SerializedName("goal_id") val goalId: String,
    @SerializedName("name") val name: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double,
    @SerializedName("remaining_amount") val remainingAmount: Double,
    @SerializedName("target_date") val targetDate: String? = null,
    @SerializedName("required_monthly") val requiredMonthly: Double,
    @SerializedName("projected_completion_date") val projectedCompletionDate: String? = null,
    @SerializedName("pacing_status") val pacingStatus: String,
    @SerializedName("pacing_message") val pacingMessage: String,
    @SerializedName("speedup_suggestion") val speedupSuggestion: String? = null
)

data class GoalRunwayAnalysisResponse(
    @SerializedName("monthly_surplus") val monthlySurplus: Double,
    @SerializedName("active_goals_count") val activeGoalsCount: Int,
    @SerializedName("total_monthly_required") val totalMonthlyRequired: Double,
    @SerializedName("surplus_coverage_pct") val surplusCoveragePct: Double,
    @SerializedName("is_fully_funded") val isFullyFunded: Boolean,
    @SerializedName("goals") val goals: List<GoalRunwayAnalysisItem> = emptyList(),
    @SerializedName("ai_runway_summary") val aiRunwaySummary: String,
    @SerializedName("discretionary_reduction_tip") val discretionaryReductionTip: String? = null,
    @SerializedName("provider_used") val providerUsed: String = "deterministic_rules"
)
