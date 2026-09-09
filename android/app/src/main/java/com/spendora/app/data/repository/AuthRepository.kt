package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.local.SessionManager
import com.spendora.app.data.model.*
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(context: Context) {

    private val apiClient = ApiClient.getInstance(context)
    private val sessionManager = SessionManager.getInstance(context)
    private val authApi = apiClient.authApi
    private val gson = apiClient.gson

    val isLoggedIn = sessionManager.isLoggedIn
    val currentUser = sessionManager.currentUser
    val themeMode = sessionManager.themeMode
    val isBiometricEnabledFlow = sessionManager.isBiometricEnabledFlow

    fun setThemeMode(mode: String) {
        sessionManager.setThemeMode(mode)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sessionManager.setBiometricEnabled(enabled)
    }

    fun isBiometricEnabled(): Boolean {
        return sessionManager.isBiometricEnabled()
    }


    suspend fun login(email: String, password: String): Result<AuthSuccessResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(UserLoginRequest(email = email.trim(), password = password))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                
                // Extract refresh token from Set-Cookie header if present
                val setCookieHeaders = response.headers().values("Set-Cookie")
                var refreshToken: String? = null
                for (cookie in setCookieHeaders) {
                    if (cookie.startsWith("spendora_refresh_token=")) {
                        refreshToken = cookie.substringAfter("spendora_refresh_token=").substringBefore(";")
                        break
                    }
                }
                
                sessionManager.saveAuth(authData.accessToken, authData.user, refreshToken)
                Result.success(authData)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to connect to Spendora server"))
        }
    }

    suspend fun googleAuth(idToken: String): Result<AuthSuccessResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.googleAuth(GoogleAuthRequest(credential = idToken))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                
                val setCookieHeaders = response.headers().values("Set-Cookie")
                var refreshToken: String? = null
                for (cookie in setCookieHeaders) {
                    if (cookie.startsWith("spendora_refresh_token=")) {
                        refreshToken = cookie.substringAfter("spendora_refresh_token=").substringBefore(";")
                        break
                    }
                }
                
                sessionManager.saveAuth(authData.accessToken, authData.user, refreshToken)
                Result.success(authData)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Google sign-in failed"))
        }
    }

    suspend fun register(fullName: String, email: String, password: String): Result<UserRegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.register(
                UserRegisterRequest(
                    fullName = fullName.trim(),
                    email = email.trim(),
                    password = password
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to connect to Spendora server"))
        }
    }

    suspend fun forgotPassword(email: String): Result<PasswordResetResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.forgotPassword(PasswordResetRequest(email = email.trim()))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to send reset OTP"))
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<VerifyOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.verifyOtp(VerifyOtpRequest(email = email.trim(), otp = otp.trim()))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "OTP verification failed"))
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<GenericMessageResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.resetPassword(
                PasswordResetConfirm(
                    email = email.trim(),
                    otp = otp.trim(),
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Password reset failed"))
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Ignore server logout errors on client teardown
        } finally {
            sessionManager.clearSession()
        }
        Result.success(Unit)
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
