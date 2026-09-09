package com.spendora.app.data.api

import com.spendora.app.data.model.StatementSummaryDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportApi {

    @Streaming
    @GET("api/v1/reports/expenses/csv")
    suspend fun exportExpensesCsv(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("category_id") categoryId: String? = null,
    ): Response<ResponseBody>

    @Streaming
    @GET("api/v1/reports/incomes/csv")
    suspend fun exportIncomesCsv(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("source") source: String? = null,
    ): Response<ResponseBody>

    @GET("api/v1/reports/statement")
    suspend fun getMonthlyStatement(
        @Query("period_month") periodMonth: String? = null,
    ): Response<StatementSummaryDto>
}
