package com.spendora.app.data.api

import com.spendora.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AiApi {

    @POST("api/v1/ai/simulate-purchase")
    suspend fun simulatePurchase(
        @Body request: PurchaseSimulationRequest
    ): Response<PurchaseSimulationResponse>

    @GET("api/v1/ai/leak-analysis")
    suspend fun getLeakAnalysis(): Response<LeakAnalysisResponse>

    @GET("api/v1/ai/safe-to-spend")
    suspend fun getSafeToSpend(): Response<SafeToSpendResponse>

    @POST("api/v1/ai/chat")
    suspend fun chat(
        @Body request: FinancialChatRequest
    ): Response<FinancialChatResponse>

    @POST("api/v1/ai/extract-transaction")
    suspend fun extractTransaction(
        @Body request: TransactionExtractionRequest
    ): Response<TransactionExtractionResponse>

    @GET("api/v1/ai/health-score")
    suspend fun getFinancialHealthScore(): Response<FinancialHealthResponse>

    @GET("api/v1/ai/goals-runway")
    suspend fun getGoalsRunway(): Response<GoalRunwayAnalysisResponse>
}
