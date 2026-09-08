package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GoalRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val goalApi = apiClient.goalApi
    private val gson = apiClient.gson

    suspend fun listGoals(status: String? = null): Result<GoalListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = goalApi.listGoals(status = status?.ifBlank { null })
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch savings goals"))
        }
    }

    suspend fun createGoal(
        name: String,
        targetAmount: Double,
        currentAmount: Double = 0.0,
        targetDate: String? = null,
        category: String = "Savings",
        color: String = "emerald",
        notes: String? = null
    ): Result<GoalDto> = withContext(Dispatchers.IO) {
        try {
            val request = GoalCreateRequest(
                name = name.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate?.ifBlank { null },
                category = category,
                color = color,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = goalApi.createGoal(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to create savings goal"))
        }
    }

    suspend fun updateGoal(
        id: String,
        name: String? = null,
        targetAmount: Double? = null,
        currentAmount: Double? = null,
        targetDate: String? = null,
        category: String? = null,
        color: String? = null,
        status: String? = null,
        notes: String? = null
    ): Result<GoalDto> = withContext(Dispatchers.IO) {
        try {
            val request = GoalUpdateRequest(
                name = name?.trim()?.ifBlank { null },
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate?.ifBlank { null },
                category = category,
                color = color,
                status = status,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = goalApi.updateGoal(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update savings goal"))
        }
    }

    suspend fun contributeToGoal(
        id: String,
        amount: Double,
        action: String = "deposit",
        notes: String? = null
    ): Result<GoalDto> = withContext(Dispatchers.IO) {
        try {
            val request = GoalContributionRequest(
                amount = amount,
                action = action,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = goalApi.contributeToGoal(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update goal contribution"))
        }
    }

    suspend fun deleteGoal(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = goalApi.deleteGoal(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to delete savings goal"))
        }
    }

    private fun <T> parseError(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val errorObj = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorObj.getErrorMessage()
            } else {
                "Request failed with HTTP ${response.code()}"
            }
        } catch (e: Exception) {
            "An unexpected error occurred (${response.code()})"
        }
    }
}
