package com.spendora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spendora.app.ui.navigation.AppNavigation
import com.spendora.app.ui.theme.BackgroundDark
import com.spendora.app.ui.theme.SpendoraTheme
import com.spendora.app.ui.theme.TextPrimary
import com.spendora.app.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        dashboardContent = {
                            DashboardPlaceholder(authViewModel = authViewModel)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardPlaceholder(authViewModel: AuthViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to Spendora Dashboard\n(Phase 31 will implement complete dashboard)",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
