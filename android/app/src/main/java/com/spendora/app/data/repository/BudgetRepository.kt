package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class BudgetRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val budgetApi = apiClient.budgetApi
    private val gson = apiClient.gson

    suspend fun getBudgets(
        periodDate: String? = null,
        periodMonth: String? = null,
        periodType: String = "monthly"
    ): Result<BudgetListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = budgetApi.getBudgets(
                periodDate = periodDate,
                periodMonth = periodMonth,
                periodType = periodType
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch budgets"))
        }
    }

    suspend fun setBudget(
        scope: String,
        categoryId: String? = null,
        amount: Double,
        periodType: String = "monthly",
        periodStart: String? = null
    ): Result<BudgetDto> = withContext(Dispatchers.IO) {
        try {
            val request = BudgetCreateRequest(
                scope = scope,
                categoryId = if (scope == "category") categoryId else null,
                amount = amount,
                periodType = periodType,
                periodStart = periodStart
            )
            val response = budgetApi.setBudget(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to set budget"))
        }
    }

    suspend fun updateBudget(id: String, amount: Double): Result<BudgetDto> = withContext(Dispatchers.IO) {
        try {
            val response = budgetApi.updateBudget(id, BudgetUpdateRequest(amount = amount))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update budget"))
        }
    }

    suspend fun deleteBudget(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = budgetApi.deleteBudget(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to delete budget"))
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
