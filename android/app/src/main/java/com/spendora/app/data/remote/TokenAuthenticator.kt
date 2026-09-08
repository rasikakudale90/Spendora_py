package com.spendora.app.data.remote

import com.google.gson.Gson
import com.spendora.app.BuildConfig
import com.spendora.app.data.local.SessionManager
import com.spendora.app.data.model.AuthSuccessResponse
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val gson: Gson
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        // If we already tried refreshing and failed, don't loop indefinitely
        if (responseCount(response) >= 3) {
            sessionManager.clearSession()
            return null
        }

        val currentToken = sessionManager.getAccessToken() ?: return null

        // If the request was already made with a newer token, retry with that
        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        if (requestToken != null && requestToken != currentToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        // Attempt synchronous refresh call using basic OkHttpClient without authenticator
        val refreshClient = OkHttpClient.Builder().build()
        val refreshUrl = "${BuildConfig.BASE_URL.trimEnd('/')}/api/v1/auth/refresh"

        val refreshRequestBuilder = Request.Builder()
            .url(refreshUrl)
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))

        val refreshToken = sessionManager.getRefreshToken()
        if (!refreshToken.isNullOrBlank()) {
            refreshRequestBuilder.addHeader("Cookie", "spendora_refresh_token=$refreshToken")
        }

        try {
            val refreshResponse = refreshClient.newCall(refreshRequestBuilder.build()).execute()
            if (refreshResponse.isSuccessful) {
                val bodyString = refreshResponse.body?.string()
                if (bodyString != null) {
                    val authSuccess = gson.fromJson(bodyString, AuthSuccessResponse::class.java)
                    sessionManager.updateAccessToken(authSuccess.accessToken)

                    // Extract new refresh cookie if returned in Set-Cookie
                    val setCookieHeaders = refreshResponse.headers("Set-Cookie")
                    for (cookie in setCookieHeaders) {
                        if (cookie.startsWith("spendora_refresh_token=")) {
                            val newCookieVal = cookie.substringAfter("spendora_refresh_token=").substringBefore(";")
                            sessionManager.saveAuth(authSuccess.accessToken, authSuccess.user, newCookieVal)
                            break
                        }
                    }

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${authSuccess.accessToken}")
                        .build()
                }
            } else {
                // Refresh failed or revoked
                sessionManager.clearSession()
            }
        } catch (e: Exception) {
            // Network error during refresh
        }

        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
