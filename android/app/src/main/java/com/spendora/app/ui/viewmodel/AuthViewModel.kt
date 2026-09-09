package com.spendora.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendora.app.data.model.UserDto
import com.spendora.app.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiEvent {
    data class ShowToast(val message: String, val isError: Boolean = false) : AuthUiEvent()
    object NavigateToDashboard : AuthUiEvent()
    object NavigateToLogin : AuthUiEvent()
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

data class ForgotOtpUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val step: Int = 1, // 1: Email, 2: OTP + New Password, 3: Success
    val isLoading: Boolean = false,
    val devOtp: String? = null,
    val countdownSeconds: Int = 50,
    val isTimerExpired: Boolean = false,
    val isOtpVerified: Boolean = false,
    val emailError: String? = null,
    val otpError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser
    val themeMode = authRepository.themeMode

    fun setThemeMode(mode: String) {
        authRepository.setThemeMode(mode)
    }

    fun toggleTheme() {
        val current = themeMode.value
        val next = if (current == "dark") "light" else "dark"
        authRepository.setThemeMode(next)
    }

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _forgotState = MutableStateFlow(ForgotOtpUiState())
    val forgotState: StateFlow<ForgotOtpUiState> = _forgotState.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    private var countdownJob: Job? = null

    // Login actions
    fun onLoginEmailChange(email: String) {
        _loginState.value = _loginState.value.copy(email = email, emailError = null, generalError = null)
    }

    fun onLoginPasswordChange(password: String) {
        _loginState.value = _loginState.value.copy(password = password, passwordError = null, generalError = null)
    }

    fun login() {
        val state = _loginState.value
        var hasError = false

        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _loginState.value = _loginState.value.copy(emailError = "Please enter a valid email address")
            hasError = true
        }

        if (state.password.isBlank()) {
            _loginState.value = _loginState.value.copy(passwordError = "Password is required")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, generalError = null)
            val result = authRepository.login(state.email, state.password)
            _loginState.value = _loginState.value.copy(isLoading = false)

            result.onSuccess {
                _events.emit(AuthUiEvent.ShowToast("Welcome back, ${it.user.fullName}!"))
                _events.emit(AuthUiEvent.NavigateToDashboard)
            }.onFailure {
                _loginState.value = _loginState.value.copy(generalError = it.message)
                _events.emit(AuthUiEvent.ShowToast(it.message ?: "Login failed", isError = true))
            }
        }
    }

    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, generalError = null)
            _registerState.value = _registerState.value.copy(isLoading = true, generalError = null)
            
            val result = authRepository.googleAuth(idToken)
            _loginState.value = _loginState.value.copy(isLoading = false)
            _registerState.value = _registerState.value.copy(isLoading = false)

            result.onSuccess {
                _events.emit(AuthUiEvent.ShowToast("Welcome to Spendora, ${it.user.fullName}!"))
                _events.emit(AuthUiEvent.NavigateToDashboard)
            }.onFailure {
                _loginState.value = _loginState.value.copy(generalError = it.message)
                _registerState.value = _registerState.value.copy(generalError = it.message)
                _events.emit(AuthUiEvent.ShowToast(it.message ?: "Google Sign-In failed", isError = true))
            }
        }
    }

    // Register actions
    fun onRegisterNameChange(name: String) {
        _registerState.value = _registerState.value.copy(fullName = name, nameError = null, generalError = null)
    }

    fun onRegisterEmailChange(email: String) {
        _registerState.value = _registerState.value.copy(email = email, emailError = null, generalError = null)
    }

    fun onRegisterPasswordChange(password: String) {
        _registerState.value = _registerState.value.copy(password = password, passwordError = null, generalError = null)
    }

    fun register() {
        val state = _registerState.value
        var hasError = false

        if (state.fullName.trim().length < 2) {
            _registerState.value = _registerState.value.copy(nameError = "Full name must be at least 2 characters")
            hasError = true
        }

        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _registerState.value = _registerState.value.copy(emailError = "Please enter a valid email address")
            hasError = true
        }

        if (state.password.length < 8) {
            _registerState.value = _registerState.value.copy(passwordError = "Password must be at least 8 characters")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _registerState.value = _registerState.value.copy(isLoading = true, generalError = null)
            val result = authRepository.register(state.fullName, state.email, state.password)
            _registerState.value = _registerState.value.copy(isLoading = false)

            result.onSuccess {
                _events.emit(AuthUiEvent.ShowToast("Account created! Please sign in with your credentials."))
                _loginState.value = LoginUiState(email = state.email)
                _events.emit(AuthUiEvent.NavigateToLogin)
            }.onFailure {
                _registerState.value = _registerState.value.copy(generalError = it.message)
                _events.emit(AuthUiEvent.ShowToast(it.message ?: "Registration failed", isError = true))
            }
        }
    }

    // Forgot Password & 4-Digit OTP actions
    fun onForgotEmailChange(email: String) {
        _forgotState.value = _forgotState.value.copy(email = email, emailError = null, generalError = null)
    }

    fun onForgotOtpChange(otp: String) {
        if (otp.length <= 4 && otp.all { it.isDigit() }) {
            _forgotState.value = _forgotState.value.copy(otp = otp, otpError = null, generalError = null)
        }
    }

    fun onForgotNewPasswordChange(pass: String) {
        _forgotState.value = _forgotState.value.copy(newPassword = pass, passwordError = null, generalError = null)
    }

    fun onForgotConfirmPasswordChange(pass: String) {
        _forgotState.value = _forgotState.value.copy(confirmPassword = pass, passwordError = null, generalError = null)
    }

    fun sendForgotOtp() {
        val state = _forgotState.value
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _forgotState.value = _forgotState.value.copy(emailError = "Enter a valid email address")
            return
        }

        viewModelScope.launch {
            _forgotState.value = _forgotState.value.copy(isLoading = true, generalError = null)
            val result = authRepository.forgotPassword(state.email)
            _forgotState.value = _forgotState.value.copy(isLoading = false)

            result.onSuccess {
                _forgotState.value = _forgotState.value.copy(
                    step = 2,
                    devOtp = it.devOtp,
                    countdownSeconds = it.expiresInSeconds ?: 50,
                    isTimerExpired = false,
                    otp = it.devOtp ?: ""
                )
                startCountdownTimer(it.expiresInSeconds ?: 50)
                _events.emit(AuthUiEvent.ShowToast("Verification code sent to ${state.email}"))
            }.onFailure {
                _forgotState.value = _forgotState.value.copy(generalError = it.message)
                _events.emit(AuthUiEvent.ShowToast(it.message ?: "Failed to send code", isError = true))
            }
        }
    }

    private fun startCountdownTimer(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var current = seconds
            while (current > 0) {
                _forgotState.value = _forgotState.value.copy(countdownSeconds = current, isTimerExpired = false)
                delay(1000)
                current--
            }
            _forgotState.value = _forgotState.value.copy(countdownSeconds = 0, isTimerExpired = true)
        }
    }

    fun resetPassword() {
        val state = _forgotState.value
        var hasError = false

        if (state.otp.length != 4) {
            _forgotState.value = _forgotState.value.copy(otpError = "Please enter the complete 4-digit OTP")
            hasError = true
        }

        if (state.newPassword.length < 8) {
            _forgotState.value = _forgotState.value.copy(passwordError = "Password must be at least 8 characters")
            hasError = true
        } else if (state.newPassword != state.confirmPassword) {
            _forgotState.value = _forgotState.value.copy(passwordError = "Passwords do not match")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _forgotState.value = _forgotState.value.copy(isLoading = true, generalError = null)
            val result = authRepository.resetPassword(state.email, state.otp, state.newPassword)
            _forgotState.value = _forgotState.value.copy(isLoading = false)

            result.onSuccess {
                countdownJob?.cancel()
                _forgotState.value = _forgotState.value.copy(step = 3)
                _events.emit(AuthUiEvent.ShowToast("Password reset successfully!"))
            }.onFailure {
                _forgotState.value = _forgotState.value.copy(generalError = it.message)
                _events.emit(AuthUiEvent.ShowToast(it.message ?: "Reset failed", isError = true))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(AuthUiEvent.NavigateToLogin)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
