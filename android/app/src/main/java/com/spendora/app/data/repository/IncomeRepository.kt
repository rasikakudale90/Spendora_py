package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class IncomeRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val incomeApi = apiClient.incomeApi
    private val gson = apiClient.gson

    suspend fun getIncomes(
        source: String? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        sortBy: String = "income_date",
        sortOrder: String = "desc",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<IncomeListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = incomeApi.getIncomes(
                source = source?.ifBlank { null },
                search = search?.ifBlank { null },
                startDate = startDate?.ifBlank { null },
                endDate = endDate?.ifBlank { null },
                sortBy = sortBy,
                sortOrder = sortOrder,
                page = page,
                pageSize = pageSize
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch incomes"))
        }
    }

    suspend fun createIncome(
        title: String,
        amount: Double,
        incomeDate: String,
        source: String,
        notes: String? = null
    ): Result<IncomeDto> = withContext(Dispatchers.IO) {
        try {
            val request = IncomeCreateRequest(
                title = title.trim(),
                amount = amount,
                incomeDate = incomeDate,
                source = source,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = incomeApi.createIncome(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to save income"))
        }
    }

    suspend fun updateIncome(
        id: Int,
        title: String,
        amount: Double,
        incomeDate: String,
        source: String,
        notes: String? = null
    ): Result<IncomeDto> = withContext(Dispatchers.IO) {
        try {
            val request = IncomeCreateRequest(
                title = title.trim(),
                amount = amount,
                incomeDate = incomeDate,
                source = source,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = incomeApi.updateIncome(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update income"))
        }
    }

    suspend fun deleteIncome(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = incomeApi.deleteIncome(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to delete income"))
        }
    }

    suspend fun getMonthlySummary(month: String? = null): Result<MonthlyIncomeSummary> = withContext(Dispatchers.IO) {
        try {
            val response = incomeApi.getMonthlySummary(month)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch income summary"))
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
