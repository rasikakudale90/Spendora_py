package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.IncomeDto
import com.spendora.app.data.model.MonthlyIncomeSummary
import com.spendora.app.data.repository.IncomeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncomeUiState(
    val incomes: List<IncomeDto> = emptyList(),
    val monthlySummary: MonthlyIncomeSummary? = null,
    val selectedSource: String? = null,
    val searchQuery: String = "",
    val page: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val incomeRepo = IncomeRepository(application)

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadIncomes()
        loadMonthlySummary()
    }

    fun loadMonthlySummary() {
        viewModelScope.launch {
            val result = incomeRepo.getMonthlySummary()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(monthlySummary = it)
            }
        }
    }

    fun loadIncomes(page: Int = 1) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = incomeRepo.getIncomes(
                source = state.selectedSource,
                search = state.searchQuery,
                sortBy = "income_date",
                sortOrder = "desc",
                page = page,
                pageSize = 20
            )

            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    incomes = res.items,
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

    fun onSourceFilterSelect(source: String?) {
        _uiState.value = _uiState.value.copy(selectedSource = source)
        loadIncomes(page = 1)
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadIncomes(page = 1)
    }

    fun saveIncome(
        id: Int? = null,
        title: String,
        amount: Double,
        incomeDate: String,
        source: String,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = if (id == null) {
                incomeRepo.createIncome(
                    title = title,
                    amount = amount,
                    incomeDate = incomeDate,
                    source = source,
                    notes = notes
                )
            } else {
                incomeRepo.updateIncome(
                    id = id,
                    title = title,
                    amount = amount,
                    incomeDate = incomeDate,
                    source = source,
                    notes = notes
                )
            }

            result.onSuccess {
                _toastEvents.emit(if (id == null) "Income added successfully!" else "Income updated successfully!")
                loadIncomes(page = _uiState.value.page)
                loadMonthlySummary()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to save income")
            }
        }
    }

    fun deleteIncome(id: Int) {
        viewModelScope.launch {
            val result = incomeRepo.deleteIncome(id)
            result.onSuccess {
                _toastEvents.emit("Income entry deleted")
                loadIncomes(page = _uiState.value.page)
                loadMonthlySummary()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to delete income")
            }
        }
    }
}
