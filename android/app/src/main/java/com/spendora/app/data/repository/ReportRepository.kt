package com.spendora.app.data.repository

import android.content.Context
import com.spendora.app.data.model.StatementSummaryDto
import com.spendora.app.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportRepository(private val context: Context) {

    private val reportApi = ApiClient.getInstance(context).reportApi

    suspend fun getMonthlyStatement(periodMonth: String? = null): Result<StatementSummaryDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = reportApi.getMonthlyStatement(periodMonth)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch monthly statement: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun exportExpensesCsv(
        dateFrom: String? = null,
        dateTo: String? = null,
        categoryId: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val response = reportApi.exportExpensesCsv(dateFrom, dateTo, categoryId)
            if (response.isSuccessful && response.body() != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(context.cacheDir, "spendora_expenses_$timeStamp.csv")
                response.body()!!.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(file)
            } else {
                Result.failure(Exception("Failed to download expenses CSV: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportIncomesCsv(
        dateFrom: String? = null,
        dateTo: String? = null,
        source: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val response = reportApi.exportIncomesCsv(dateFrom, dateTo, source)
            if (response.isSuccessful && response.body() != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(context.cacheDir, "spendora_incomes_$timeStamp.csv")
                response.body()!!.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(file)
            } else {
                Result.failure(Exception("Failed to download incomes CSV: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
