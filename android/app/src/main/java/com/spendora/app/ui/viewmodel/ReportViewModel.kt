package com.spendora.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.StatementSummaryDto
import com.spendora.app.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReportRepository(application)

    private val _statement = MutableStateFlow<StatementSummaryDto?>(null)
    val statement: StateFlow<StatementSummaryDto?> = _statement.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        loadStatement(currentMonth)
    }

    fun loadStatement(periodMonth: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMonthlyStatement(periodMonth)
                .onSuccess { summary ->
                    _statement.value = summary
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _message.value = err.localizedMessage
                    _isLoading.value = false
                }
        }
    }

    fun exportExpenses(context: Context, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            repository.exportExpensesCsv()
                .onSuccess { file ->
                    _isExporting.value = false
                    onFileReady(file)
                }
                .onFailure { err ->
                    _isExporting.value = false
                    _message.value = "Export failed: ${err.localizedMessage}"
                }
        }
    }

    fun exportIncomes(context: Context, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            repository.exportIncomesCsv()
                .onSuccess { file ->
                    _isExporting.value = false
                    onFileReady(file)
                }
                .onFailure { err ->
                    _isExporting.value = false
                    _message.value = "Export failed: ${err.localizedMessage}"
                }
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String = "text/csv") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Spendora Financial Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Spendora Export"))
        } catch (e: Exception) {
            _message.value = "Could not share file: ${e.localizedMessage}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
