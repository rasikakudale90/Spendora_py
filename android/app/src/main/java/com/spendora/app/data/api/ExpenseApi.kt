package com.spendora.app.data.api

import com.spendora.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ExpenseApi {

    @GET("api/v1/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("api/v1/categories")
    suspend fun createCategory(
        @Body request: CategoryCreateRequest
    ): Response<CategoryDto>

    @GET("api/v1/expenses")
    suspend fun getExpenses(
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("date_from") startDate: String? = null,
        @Query("date_to") endDate: String? = null,
        @Query("payment_mode") paymentMode: String? = null,
        @Query("sort_by") sortBy: String? = "expense_date",
        @Query("sort_order") sortOrder: String? = "desc",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ExpenseListResponse>

    @POST("api/v1/expenses")
    suspend fun createExpense(
        @Body request: ExpenseCreateRequest
    ): Response<ExpenseDto>

    @PATCH("api/v1/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: ExpenseCreateRequest
    ): Response<ExpenseDto>

    @DELETE("api/v1/expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: String
    ): Response<GenericMessageResponse>
}
