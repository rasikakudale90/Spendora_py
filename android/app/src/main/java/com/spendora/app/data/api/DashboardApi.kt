package com.spendora.app.data.api

import com.spendora.app.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {

    @GET("api/v1/dashboard/summary")
    suspend fun getSummary(
        @Query("period") period: String? = null,
        @Query("period_type") periodType: String? = "monthly"
    ): Response<DashboardSummary>

    @GET("api/v1/dashboard/spending-by-category")
    suspend fun getSpendingByCategory(
        @Query("month") month: String? = null
    ): Response<List<CategorySpendItem>>

    @GET("api/v1/dashboard/trends")
    suspend fun getSpendingTrends(
        @Query("month") month: String? = null
    ): Response<List<DailySpendPoint>>

    @GET("api/v1/dashboard/comparison")
    suspend fun getComparison(
        @Query("month") month: String? = null
    ): Response<MonthOverMonthStats>
}
