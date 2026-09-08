package com.spendora.app.data.api

import com.spendora.app.data.model.BudgetCreateRequest
import com.spendora.app.data.model.BudgetDto
import com.spendora.app.data.model.BudgetListResponse
import com.spendora.app.data.model.BudgetUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface BudgetApi {

    @GET("api/v1/budgets")
    suspend fun getBudgets(
        @Query("period_date") periodDate: String? = null,
        @Query("period_month") periodMonth: String? = null,
        @Query("period_type") periodType: String = "monthly"
    ): Response<BudgetListResponse>

    @POST("api/v1/budgets")
    suspend fun setBudget(
        @Body request: BudgetCreateRequest
    ): Response<BudgetDto>

    @PATCH("api/v1/budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: BudgetUpdateRequest
    ): Response<BudgetDto>

    @DELETE("api/v1/budgets/{id}")
    suspend fun deleteBudget(
        @Path("id") id: String
    ): Response<Map<String, String>>
}
