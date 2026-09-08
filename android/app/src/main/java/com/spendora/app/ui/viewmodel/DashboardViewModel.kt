package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.*
import com.spendora.app.data.repository.DashboardRepository
import com.spendora.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val recentExpenses: List<ExpenseDto> = emptyList(),
    val categoryBreakdown: List<CategorySpendItem> = emptyList(),
    val comparison: MonthOverMonthStats? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dashboardRepo = DashboardRepository(application)
    private val expenseRepo = ExpenseRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val summaryRes = dashboardRepo.getSummary()
            val recentExpRes = expenseRepo.getExpenses(page = 1, pageSize = 5)
            val categoriesRes = dashboardRepo.getSpendingByCategory()
            val comparisonRes = dashboardRepo.getComparison()

            val summary = summaryRes.getOrDefault(DashboardSummary())
            val recentExpenses = recentExpRes.getOrNull()?.items ?: emptyList()
            val categories = categoriesRes.getOrDefault(emptyList())
            val comparison = comparisonRes.getOrNull()

            _uiState.value = _uiState.value.copy(
                summary = summary,
                recentExpenses = recentExpenses,
                categoryBreakdown = categories,
                comparison = comparison,
                isLoading = false,
                errorMessage = summaryRes.exceptionOrNull()?.message
            )
        }
    }
}
