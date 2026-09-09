package com.spendora.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.spendora.app.ui.navigation.AppNavigation
import com.spendora.app.ui.theme.BackgroundDark
import com.spendora.app.ui.theme.SpendoraTheme
import com.spendora.app.ui.viewmodel.AuthViewModel
import com.spendora.app.ui.viewmodel.DashboardViewModel
import com.spendora.app.ui.viewmodel.ExpenseViewModel
import com.spendora.app.ui.viewmodel.IncomeViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.components.BiometricPromptHelper
import com.spendora.app.ui.components.SpendoraLogo
import com.spendora.app.ui.theme.*

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val incomeViewModel: IncomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by authViewModel.themeMode.collectAsState()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isBiometricEnabled by authViewModel.isBiometricEnabledFlow.collectAsState()
            var isUnlocked by remember { mutableStateOf(!isBiometricEnabled) }

            fun triggerBiometric() {
                if (BiometricPromptHelper.isBiometricAvailable(this)) {
                    BiometricPromptHelper.showBiometricPrompt(
                        activity = this,
                        title = "Spendora Vault Security",
                        subtitle = "Scan biometric or enter PIN to access financial records",
                        onSuccess = { isUnlocked = true },
                        onError = { /* Allow user to retry */ }
                    )
                } else {
                    isUnlocked = true
                }
            }

            LaunchedEffect(isLoggedIn, isBiometricEnabled) {
                if (isLoggedIn && isBiometricEnabled && !isUnlocked) {
                    triggerBiometric()
                }
            }

            SpendoraTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpendoraTheme.colors.background
                ) {
                    if (isLoggedIn && isBiometricEnabled && !isUnlocked) {
                        // Vault Locked Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SpendoraTheme.colors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                SpendoraLogo(size = 80.dp)
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "SPENDORA VAULT LOCKED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    ),
                                    fontSize = 12.sp,
                                    color = CyanInfoLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Biometric Verification Required",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontSize = 20.sp,
                                    color = SpendoraTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Your financial telemetry and transaction data are protected with biometric hardware encryption.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = HankenGroteskFamily
                                    ),
                                    fontSize = 13.sp,
                                    color = SpendoraTheme.colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = { triggerBiometric() },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanInfoLight,
                                        contentColor = BackgroundDark
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Unlock",
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unlock Vault",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        fontSize = 15.sp
                                    )
                                }

                            }
                        }
                    } else {
                        AppNavigation(
                            authViewModel = authViewModel,
                            dashboardViewModel = dashboardViewModel,
                            expenseViewModel = expenseViewModel,
                            incomeViewModel = incomeViewModel
                        )
                    }
                }
            }
        }
    }
}

