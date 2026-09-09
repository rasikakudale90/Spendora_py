package com.spendora.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.spendora.app.BuildConfig
import com.spendora.app.data.api.AuthApi
import com.spendora.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient private constructor(context: Context) {

    private val sessionManager = SessionManager.getInstance(context)
    val gson: Gson = GsonBuilder().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionManager))
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator(sessionManager, gson))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL.let { if (it.endsWith("/")) it else "$it/" })
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val expenseApi: com.spendora.app.data.api.ExpenseApi by lazy { retrofit.create(com.spendora.app.data.api.ExpenseApi::class.java) }
    val incomeApi: com.spendora.app.data.api.IncomeApi by lazy { retrofit.create(com.spendora.app.data.api.IncomeApi::class.java) }
    val dashboardApi: com.spendora.app.data.api.DashboardApi by lazy { retrofit.create(com.spendora.app.data.api.DashboardApi::class.java) }
    val budgetApi: com.spendora.app.data.api.BudgetApi by lazy { retrofit.create(com.spendora.app.data.api.BudgetApi::class.java) }
    val goalApi: com.spendora.app.data.api.GoalApi by lazy { retrofit.create(com.spendora.app.data.api.GoalApi::class.java) }
    val aiApi: com.spendora.app.data.api.AiApi by lazy { retrofit.create(com.spendora.app.data.api.AiApi::class.java) }
    val reportApi: com.spendora.app.data.api.ReportApi by lazy { retrofit.create(com.spendora.app.data.api.ReportApi::class.java) }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
