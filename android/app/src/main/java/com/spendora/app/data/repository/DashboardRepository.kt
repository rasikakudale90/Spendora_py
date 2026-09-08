package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class DashboardRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val dashboardApi = apiClient.dashboardApi
    private val gson = apiClient.gson

    suspend fun getSummary(period: String? = null, periodType: String = "monthly"): Result<DashboardSummary> = withContext(Dispatchers.IO) {
        try {
            val response = dashboardApi.getSummary(period = period, periodType = periodType)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch dashboard summary"))
        }
    }

    suspend fun getSpendingByCategory(month: String? = null): Result<List<CategorySpendItem>> = withContext(Dispatchers.IO) {
        try {
            val response = dashboardApi.getSpendingByCategory(month)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch category breakdown"))
        }
    }

    suspend fun getSpendingTrends(month: String? = null): Result<List<DailySpendPoint>> = withContext(Dispatchers.IO) {
        try {
            val response = dashboardApi.getSpendingTrends(month)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch spending trends"))
        }
    }

    suspend fun getComparison(month: String? = null): Result<MonthOverMonthStats> = withContext(Dispatchers.IO) {
        try {
            val response = dashboardApi.getComparison(month)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch comparison stats"))
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
