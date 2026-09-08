package com.spendora.app.data.remote

import com.spendora.app.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Do not attach token for public auth endpoints
        if (path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/forgot-password") ||
            path.contains("/auth/verify-otp") ||
            path.contains("/auth/reset-password")
        ) {
            return chain.proceed(originalRequest)
        }

        val token = sessionManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Add cookie header for refresh token if available
        val refreshToken = sessionManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            requestBuilder.addHeader("Cookie", "spendora_refresh_token=$refreshToken")
        }

        requestBuilder.addHeader("Accept", "application/json")
        return chain.proceed(requestBuilder.build())
    }
}
