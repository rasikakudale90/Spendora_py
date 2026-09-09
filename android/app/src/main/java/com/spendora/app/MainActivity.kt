package com.spendora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val incomeViewModel: IncomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by authViewModel.themeMode.collectAsState()
            SpendoraTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
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
