package com.spendora.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, if (event.isError) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
                }
                is AuthUiEvent.NavigateToLogin -> onNavigateToLogin()
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

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    fontSize = 32.sp,
                    color = EmeraldSuccessLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Text(
                text = "Start tracking smart with AI-driven insights",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            SpendoraCard {
                SpendoraTextField(
                    value = registerState.fullName,
                    onValueChange = viewModel::onRegisterNameChange,
                    label = "Full Name",
                    placeholder = "Alex Morgan",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = registerState.nameError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SpendoraTextField(
                    value = registerState.email,
                    onValueChange = viewModel::onRegisterEmailChange,
                    label = "Email Address",
                    placeholder = "you@example.com",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = registerState.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SpendoraTextField(
                    value = registerState.password,
                    onValueChange = viewModel::onRegisterPasswordChange,
                    label = "Password",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    errorMessage = registerState.passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.register()
                        }
                    )
                )

                if (registerState.password.isNotEmpty()) {
                    PasswordStrengthIndicator(password = registerState.password)
                }

                if (registerState.generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = registerState.generalError ?: "",
                        color = RoseDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SpendoraButton(
                    text = "Create Account",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.register()
                    },
                    isLoading = registerState.isLoading,
                    containerColor = EmeraldSuccess
                )

                Spacer(modifier = Modifier.height(18.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = BorderDark)
                    Text(
                        text = "OR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = BorderDark)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Google Sign In
                com.spendora.app.ui.components.GoogleSignInButton(
                    onTokenReceived = { idToken ->
                        viewModel.googleSignIn(idToken)
                    },
                    buttonText = "Sign up with Google",
                    isLoading = registerState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldSuccessLight,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
