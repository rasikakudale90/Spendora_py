package com.spendora.app.data.api

import com.spendora.app.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: UserRegisterRequest
    ): Response<UserRegisterResponse>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): Response<AuthSuccessResponse>

    @POST("api/v1/auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest
    ): Response<AuthSuccessResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<AuthSuccessResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<GenericMessageResponse>

    @POST("api/v1/auth/logout-all")
    suspend fun logoutAll(): Response<GenericMessageResponse>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: PasswordResetRequest
    ): Response<PasswordResetResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(
        @Body request: PasswordResetConfirm
    ): Response<GenericMessageResponse>

    @POST("api/v1/auth/change-password")
    suspend fun changePassword(
        @Body request: PasswordChangeRequest
    ): Response<GenericMessageResponse>
}
