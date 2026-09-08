package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.*
import com.spendora.app.data.repository.AiRepository
import com.spendora.app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AiUiState(
    val safeToSpend: SafeToSpendResponse? = null,
    val financialHealth: FinancialHealthResponse? = null,
    val leakAnalysis: LeakAnalysisResponse? = null,
    val simulationResult: PurchaseSimulationResponse? = null,
    val extractionResult: TransactionExtractionResponse? = null,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "assistant",
            content = "👋 Hello! I'm Spendora AI, your personal financial advisor. Ask me anything about your spending, affordability, safe daily limits, or budget breaches!"
        )
    ),
    val suggestedPrompts: List<String> = listOf(
        "What is my safe daily limit?",
        "Show my highest expense this month",
        "Am I overspending on food?",
        "Check my subscription leaks"
    ),
    val isChatLoading: Boolean = false,
    val isSimulating: Boolean = false,
    val isScanning: Boolean = false,
    val isLeakLoading: Boolean = false,
    val isHealthLoading: Boolean = false,
    val isSafeToSpendLoading: Boolean = false,
    val errorMessage: String? = null
)

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepo = AiRepository(application)
    private val expenseRepo = ExpenseRepository(application)

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadSafeToSpend()
        loadFinancialHealth()
    }

    fun loadSafeToSpend() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSafeToSpendLoading = true)
            val result = aiRepo.getSafeToSpend()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    safeToSpend = it,
                    isSafeToSpendLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSafeToSpendLoading = false)
            }
        }
    }

    fun loadFinancialHealth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHealthLoading = true)
            val result = aiRepo.getFinancialHealthScore()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    financialHealth = it,
                    isHealthLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isHealthLoading = false)
            }
        }
    }

    fun loadLeakAnalysis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLeakLoading = true)
            val result = aiRepo.getLeakAnalysis()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    leakAnalysis = it,
                    isLeakLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLeakLoading = false,
                    errorMessage = it.message
                )
            }
        }
    }

    fun simulatePurchase(title: String, amount: Double, categoryId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSimulating = true, simulationResult = null)
            val result = aiRepo.simulatePurchase(title, amount, categoryId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    simulationResult = it,
                    isSimulating = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSimulating = false,
                    errorMessage = it.message
                )
                _toastEvents.emit(it.message ?: "Simulation failed")
            }
        }
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        val currentHistory = _uiState.value.chatMessages.toMutableList()
        val userMsg = ChatMessage(role = "user", content = message.trim())
        currentHistory.add(userMsg)

        _uiState.value = _uiState.value.copy(
            chatMessages = currentHistory,
            isChatLoading = true
        )

        viewModelScope.launch {
            val result = aiRepo.chat(
                message = message,
                history = currentHistory.takeLast(10)
            )
            result.onSuccess { res ->
                val updated = _uiState.value.chatMessages.toMutableList()
                updated.add(ChatMessage(role = "assistant", content = res.reply))
                _uiState.value = _uiState.value.copy(
                    chatMessages = updated,
                    suggestedPrompts = if (res.suggestedPrompts.isNotEmpty()) res.suggestedPrompts else _uiState.value.suggestedPrompts,
                    isChatLoading = false
                )
            }.onFailure { error ->
                val updated = _uiState.value.chatMessages.toMutableList()
                updated.add(ChatMessage(role = "assistant", content = "⚠️ I ran into an issue retrieving data: ${error.message}. Please try again."))
                _uiState.value = _uiState.value.copy(
                    chatMessages = updated,
                    isChatLoading = false
                )
            }
        }
    }

    fun extractTransaction(text: String? = null, imageBase64: String? = null, sourceType: String = "sms_text") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, extractionResult = null)
            val result = aiRepo.extractTransaction(text, imageBase64, sourceType)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    extractionResult = it,
                    isScanning = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = it.message
                )
                _toastEvents.emit(it.message ?: "Failed to extract transaction details")
            }
        }
    }

    fun clearExtractionResult() {
        _uiState.value = _uiState.value.copy(extractionResult = null)
    }

    fun clearSimulationResult() {
        _uiState.value = _uiState.value.copy(simulationResult = null)
    }
}
