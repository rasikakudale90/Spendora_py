package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.*
import com.spendora.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExpenseUiState(
    val expenses: List<ExpenseDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val selectedPaymentMode: PaymentMode? = null,
    val sortOrder: String = "desc",
    val page: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dailyLimitAlert: DailyBudgetAlert? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepo = ExpenseRepository(application)

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadCategories()
        loadExpenses()
    }

    fun loadCategories() {
        viewModelScope.launch {
            val result = expenseRepo.getCategories()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(categories = it)
            }
        }
    }

    fun loadExpenses(page: Int = 1) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = expenseRepo.getExpenses(
                search = state.searchQuery,
                categoryId = state.selectedCategoryId,
                paymentMode = state.selectedPaymentMode?.value,
                sortBy = "expense_date",
                sortOrder = state.sortOrder,
                page = page,
                pageSize = 20
            )

            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    expenses = res.items,
                    totalCount = res.totalCount,
                    page = res.page,
                    totalPages = res.totalPages,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadExpenses(page = 1)
    }

    fun onCategoryFilterSelect(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadExpenses(page = 1)
    }

    fun onPaymentModeFilterSelect(mode: PaymentMode?) {
        _uiState.value = _uiState.value.copy(selectedPaymentMode = mode)
        loadExpenses(page = 1)
    }

    fun toggleSortOrder() {
        val newOrder = if (_uiState.value.sortOrder == "desc") "asc" else "desc"
        _uiState.value = _uiState.value.copy(sortOrder = newOrder)
        loadExpenses(page = 1)
    }

    fun saveExpense(
        id: Int? = null,
        title: String,
        amount: Double,
        expenseDate: String,
        categoryId: Int,
        paymentMode: PaymentMode,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = if (id == null) {
                expenseRepo.createExpense(
                    title = title,
                    amount = amount,
                    expenseDate = expenseDate,
                    categoryId = categoryId,
                    paymentMode = paymentMode,
                    notes = notes
                )
            } else {
                expenseRepo.updateExpense(
                    id = id,
                    title = title,
                    amount = amount,
                    expenseDate = expenseDate,
                    categoryId = categoryId,
                    paymentMode = paymentMode,
                    notes = notes
                )
            }

            result.onSuccess { res ->
                _toastEvents.emit(if (id == null) "Expense added successfully!" else "Expense updated successfully!")
                if (res.dailyBudgetAlert?.isBreached == true) {
                    _uiState.value = _uiState.value.copy(dailyLimitAlert = res.dailyBudgetAlert)
                }
                loadExpenses(page = _uiState.value.page)
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to save expense")
            }
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            val result = expenseRepo.deleteExpense(id)
            result.onSuccess {
                _toastEvents.emit("Expense deleted")
                loadExpenses(page = _uiState.value.page)
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to delete expense")
            }
        }
    }

    fun dismissDailyAlert() {
        _uiState.value = _uiState.value.copy(dailyLimitAlert = null)
    }
}
