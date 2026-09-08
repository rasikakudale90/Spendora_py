package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AiRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val aiApi = apiClient.aiApi
    private val gson = apiClient.gson

    suspend fun simulatePurchase(
        title: String,
        amount: Double,
        categoryId: String? = null
    ): Result<PurchaseSimulationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = PurchaseSimulationRequest(
                title = title.trim(),
                amount = amount,
                categoryId = categoryId
            )
            val response = aiApi.simulatePurchase(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to simulate purchase"))
        }
    }

    suspend fun getLeakAnalysis(): Result<LeakAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val response = aiApi.getLeakAnalysis()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to perform leak analysis"))
        }
    }

    suspend fun getSafeToSpend(): Result<SafeToSpendResponse> = withContext(Dispatchers.IO) {
        try {
            val response = aiApi.getSafeToSpend()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch Safe-to-Spend forecast"))
        }
    }

    suspend fun chat(
        message: String,
        history: List<ChatMessage> = emptyList()
    ): Result<FinancialChatResponse> = withContext(Dispatchers.IO) {
        try {
            val request = FinancialChatRequest(
                message = message.trim(),
                history = history
            )
            val response = aiApi.chat(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to send chat message"))
        }
    }

    suspend fun extractTransaction(
        text: String? = null,
        imageBase64: String? = null,
        sourceType: String = "sms_text"
    ): Result<TransactionExtractionResponse> = withContext(Dispatchers.IO) {
        try {
            val request = TransactionExtractionRequest(
                text = text?.trim(),
                imageBase64 = imageBase64,
                sourceType = sourceType
            )
            val response = aiApi.extractTransaction(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to parse transaction"))
        }
    }

    suspend fun getFinancialHealthScore(): Result<FinancialHealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = aiApi.getFinancialHealthScore()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch Financial Health score"))
        }
    }

    suspend fun getGoalsRunway(): Result<GoalRunwayAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val response = aiApi.getGoalsRunway()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch Goals Runway analysis"))
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
