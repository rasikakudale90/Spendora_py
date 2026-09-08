package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.GoalDto
import com.spendora.app.data.model.GoalListResponse
import com.spendora.app.data.repository.GoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GoalUiState(
    val goalsResponse: GoalListResponse? = null,
    val selectedStatusFilter: String? = null, // null for all, or "active", "completed", "paused"
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val goalRepo = GoalRepository(application)

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadGoals()
    }

    fun selectStatusFilter(status: String?) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
        loadGoals(status)
    }

    fun loadGoals(status: String? = _uiState.value.selectedStatusFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = goalRepo.listGoals(status = status)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    goalsResponse = response,
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

    fun createGoal(
        name: String,
        targetAmount: Double,
        currentAmount: Double = 0.0,
        targetDate: String? = null,
        category: String = "Savings",
        color: String = "emerald",
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = goalRepo.createGoal(
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate,
                category = category,
                color = color,
                notes = notes
            )
            result.onSuccess {
                _toastEvents.emit("Goal created successfully!")
                loadGoals()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to create goal")
            }
        }
    }

    fun updateGoal(
        id: String,
        name: String? = null,
        targetAmount: Double? = null,
        currentAmount: Double? = null,
        targetDate: String? = null,
        category: String? = null,
        color: String? = null,
        status: String? = null,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = goalRepo.updateGoal(
                id = id,
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                targetDate = targetDate,
                category = category,
                color = color,
                status = status,
                notes = notes
            )
            result.onSuccess {
                _toastEvents.emit("Goal updated successfully!")
                loadGoals()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to update goal")
            }
        }
    }

    fun contribute(
        id: String,
        amount: Double,
        action: String,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = goalRepo.contributeToGoal(
                id = id,
                amount = amount,
                action = action,
                notes = notes
            )
            result.onSuccess {
                val actionVerb = if (action == "deposit") "Added funds to" else "Withdrew funds from"
                _toastEvents.emit("$actionVerb goal!")
                loadGoals()
                onSuccess()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Contribution failed")
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            val result = goalRepo.deleteGoal(id)
            result.onSuccess {
                _toastEvents.emit("Goal deleted")
                loadGoals()
            }.onFailure {
                _toastEvents.emit(it.message ?: "Failed to delete goal")
            }
        }
    }
}
