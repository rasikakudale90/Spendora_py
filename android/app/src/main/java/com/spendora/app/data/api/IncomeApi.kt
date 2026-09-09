package com.spendora.app.data.api

import com.spendora.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface IncomeApi {

    @GET("api/v1/incomes")
    suspend fun getIncomes(
        @Query("source") source: String? = null,
        @Query("search") search: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("sort_by") sortBy: String? = "income_date",
        @Query("sort_order") sortOrder: String? = "desc",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<IncomeListResponse>

    @POST("api/v1/incomes")
    suspend fun createIncome(
        @Body request: IncomeCreateRequest
    ): Response<IncomeDto>

    @PATCH("api/v1/incomes/{id}")
    suspend fun updateIncome(
        @Path("id") id: String,
        @Body request: IncomeCreateRequest
    ): Response<IncomeDto>

    @DELETE("api/v1/incomes/{id}")
    suspend fun deleteIncome(
        @Path("id") id: String
    ): Response<GenericMessageResponse>

    @GET("api/v1/incomes/summary/monthly")
    suspend fun getMonthlySummary(
        @Query("month") month: String? = null
    ): Response<MonthlyIncomeSummary>
}
