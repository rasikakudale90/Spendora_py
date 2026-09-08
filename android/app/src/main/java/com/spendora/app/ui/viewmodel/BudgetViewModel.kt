package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.BudgetDto
import com.spendora.app.data.model.BudgetListResponse
import com.spendora.app.data.model.CategoryDto
import com.spendora.app.data.repository.BudgetRepository
import com.spendora.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BudgetUiState(
    val selectedPeriod: String = "monthly", // "daily", "weekly", "monthly", "yearly"
    val budgetData: BudgetListResponse? = null,
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val budgetRepo = BudgetRepository(application)
    private val expenseRepo = ExpenseRepository(application)

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadCategories()
        loadBudgets()
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = expenseRepo.getCategories()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(categories = it)
            }
        }
    }

    fun selectPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadBudgets(periodType = period)
    }

    fun loadBudgets(periodType: String = _uiState.value.selectedPeriod, date: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = budgetRepo.getBudgets(periodDate = date, periodType = periodType)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    budgetData = response,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        }
    }

    fun setBudget(
        scope: String,
        categoryId: String?,
        amount: Double,
        periodType: String = _uiState.value.selectedPeriod,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = budgetRepo.setBudget(
                scope = scope,
                categoryId = categoryId,
                amount = amount,
                periodType = periodType
            )
            result.onSuccess {
                _toastEvents.emit("Budget saved successfully!")
                loadBudgets()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to set budget")
            }
        }
    }

    fun updateBudget(id: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = budgetRepo.updateBudget(id, amount)
            result.onSuccess {
                _toastEvents.emit("Budget updated successfully!")
                loadBudgets()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to update budget")
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            val result = budgetRepo.deleteBudget(id)
            result.onSuccess {
                _toastEvents.emit("Budget deleted successfully")
                loadBudgets()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to delete budget")
            }
        }
    }
}
