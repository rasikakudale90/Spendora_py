package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ExpenseRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val expenseApi = apiClient.expenseApi
    private val gson = apiClient.gson

    suspend fun getCategories(): Result<List<CategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = expenseApi.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch categories"))
        }
    }

    suspend fun createCategory(name: String): Result<CategoryDto> = withContext(Dispatchers.IO) {
        try {
            val response = expenseApi.createCategory(CategoryCreateRequest(name = name.trim()))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to create category"))
        }
    }

    suspend fun getExpenses(
        search: String? = null,
        categoryId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        paymentMode: String? = null,
        sortBy: String = "expense_date",
        sortOrder: String = "desc",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<ExpenseListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = expenseApi.getExpenses(
                search = search?.ifBlank { null },
                categoryId = categoryId,
                startDate = startDate?.ifBlank { null },
                endDate = endDate?.ifBlank { null },
                paymentMode = paymentMode?.ifBlank { null },
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
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch expenses"))
        }
    }

    suspend fun createExpense(
        title: String,
        amount: Double,
        expenseDate: String,
        categoryId: String,
        paymentMode: PaymentMode,
        notes: String? = null
    ): Result<ExpenseCreateResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ExpenseCreateRequest(
                title = title.trim(),
                amount = amount,
                expenseDate = expenseDate,
                categoryId = categoryId,
                paymentMode = paymentMode,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = expenseApi.createExpense(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to save expense"))
        }
    }

    suspend fun updateExpense(
        id: String,
        title: String,
        amount: Double,
        expenseDate: String,
        categoryId: String,
        paymentMode: PaymentMode,
        notes: String? = null
    ): Result<ExpenseCreateResponse> = withContext(Dispatchers.IO) {
        try {
            val request = ExpenseCreateRequest(
                title = title.trim(),
                amount = amount,
                expenseDate = expenseDate,
                categoryId = categoryId,
                paymentMode = paymentMode,
                notes = notes?.trim()?.ifBlank { null }
            )
            val response = expenseApi.updateExpense(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update expense"))
        }
    }

    suspend fun deleteExpense(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = expenseApi.deleteExpense(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to delete expense"))
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
