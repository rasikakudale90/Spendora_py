package com.spendora.app.data.api

import com.spendora.app.data.model.GoalContributionRequest
import com.spendora.app.data.model.GoalCreateRequest
import com.spendora.app.data.model.GoalDto
import com.spendora.app.data.model.GoalListResponse
import com.spendora.app.data.model.GoalUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface GoalApi {

    @GET("api/v1/goals")
    suspend fun listGoals(
        @Query("status") status: String? = null
    ): Response<GoalListResponse>

    @POST("api/v1/goals")
    suspend fun createGoal(
        @Body request: GoalCreateRequest
    ): Response<GoalDto>

    @GET("api/v1/goals/{id}")
    suspend fun getGoal(
        @Path("id") id: String
    ): Response<GoalDto>

    @PATCH("api/v1/goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: String,
        @Body request: GoalUpdateRequest
    ): Response<GoalDto>

    @POST("api/v1/goals/{id}/contribute")
    suspend fun contributeToGoal(
        @Path("id") id: String,
        @Body request: GoalContributionRequest
    ): Response<GoalDto>

    @DELETE("api/v1/goals/{id}")
    suspend fun deleteGoal(
        @Path("id") id: String
    ): Response<Unit>
}
