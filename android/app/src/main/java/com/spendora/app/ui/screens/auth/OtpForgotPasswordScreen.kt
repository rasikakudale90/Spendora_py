package com.spendora.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.components.PasswordStrengthIndicator
import com.spendora.app.ui.components.SpendoraButton
import com.spendora.app.ui.components.SpendoraCard
import com.spendora.app.ui.components.SpendoraTextField
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AuthUiEvent
import com.spendora.app.ui.viewmodel.AuthViewModel

@Composable
fun OtpForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val forgotState by viewModel.forgotState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, if (event.isError) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(targetState = forgotState.step, label = "OtpStepTransition") { step ->
                when (step) {
                    1 -> Step1EmailView(viewModel = viewModel, onNavigateToLogin = onNavigateToLogin)
                    2 -> Step2OtpView(viewModel = viewModel, onNavigateToLogin = onNavigateToLogin)
                    3 -> Step3SuccessView(onNavigateToLogin = onNavigateToLogin)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step1EmailView(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val state by viewModel.forgotState.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AmberWarning.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔑", fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Reset Your Password",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Text(
            text = "Enter your registered email to receive a 4-digit code",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
            textAlign = TextAlign.Center
        )

        SpendoraCard {
            SpendoraTextField(
                value = state.email,
                onValueChange = viewModel::onForgotEmailChange,
                label = "Account Email",
                placeholder = "you@example.com",
                leadingIcon = Icons.Default.Email,
                errorMessage = state.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.sendForgotOtp()
                    }
                )
            )

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.generalError ?: "",
                    color = RoseDanger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = "Send 4-Digit Code",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.sendForgotOtp()
                },
                isLoading = state.isLoading,
                containerColor = AmberWarning
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Remember your password? Sign In",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
    }
}

@Composable
private fun Step2OtpView(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val state by viewModel.forgotState.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PrimaryIndigo.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✉️", fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter Verification Code",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Text(
            text = "A 4-digit code was sent to ${state.email}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        SpendoraCard {
            // 4-Box discrete OTP inputs
            Text(
                text = "4-Digit Code",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OtpBoxes(
                otp = state.otp,
                onOtpChange = viewModel::onForgotOtpChange,
                isError = state.otpError != null
            )

            // Timer & Resend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isTimerExpired) {
                    Text(
                        text = "Code Expired",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = RoseDanger
                    )
                    Row(
                        modifier = Modifier.clickable { viewModel.sendForgotOtp() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PrimaryIndigoLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Resend Code", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryIndigoLight)
                    }
                } else {
                    Text(
                        text = "Valid for: ${state.countdownSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberWarning
                    )
                    Text(
                        text = "Expires in 50s",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Dev quick fill chip if available
            if (state.devOtp != null && state.otp != state.devOtp) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo.copy(alpha = 0.12f),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.onForgotOtpChange(state.devOtp ?: "") }
                ) {
                    Text(
                        text = "Dev Quickfill: ${state.devOtp}",
                        color = PrimaryIndigoLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SpendoraTextField(
                value = state.newPassword,
                onValueChange = viewModel::onForgotNewPasswordChange,
                label = "New Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = state.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            if (state.newPassword.isNotEmpty()) {
                PasswordStrengthIndicator(password = state.newPassword)
            }

            Spacer(modifier = Modifier.height(14.dp))

            SpendoraTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onForgotConfirmPasswordChange,
                label = "Confirm New Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.resetPassword()
                })
            )

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.generalError ?: "",
                    color = RoseDanger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = "Update Password",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.resetPassword()
                },
                isLoading = state.isLoading,
                enabled = state.otp.length == 4 && state.newPassword.length >= 8 && !state.isTimerExpired
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Cancel and return to Sign In",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
    }
}

@Composable
private fun OtpBoxes(
    otp: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean
) {
    BasicTextField(
        value = otp,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 0 until 4) {
                    val char = if (i < otp.length) otp[i].toString() else ""
                    val isFocused = otp.length == i

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = when {
                                    isError -> RoseDanger
                                    isFocused -> PrimaryIndigo
                                    char.isNotEmpty() -> EmeraldSuccess
                                    else -> BorderDark
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun Step3SuccessView(
    onNavigateToLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(EmeraldSuccess.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Password Reset Complete",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Text(
            text = "Your password has been updated securely. You can now sign in with your new credentials.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        SpendoraButton(
            text = "Sign In Now",
            onClick = onNavigateToLogin,
            containerColor = EmeraldSuccess
        )
    }
}
