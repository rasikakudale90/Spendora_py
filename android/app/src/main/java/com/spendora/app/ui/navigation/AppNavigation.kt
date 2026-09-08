package com.spendora.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendora.app.ui.components.SpendoraBottomNavBar
import com.spendora.app.ui.screens.auth.LoginScreen
import com.spendora.app.ui.screens.auth.OtpForgotPasswordScreen
import com.spendora.app.ui.screens.auth.RegisterScreen
import com.spendora.app.ui.screens.dashboard.DashboardScreen
import com.spendora.app.ui.screens.expenses.ExpensesScreen
import com.spendora.app.ui.screens.income.IncomeScreen
import com.spendora.app.ui.screens.budgets.BudgetsScreen
import com.spendora.app.ui.screens.goals.GoalsScreen
import com.spendora.app.ui.theme.BackgroundDark
import com.spendora.app.ui.theme.TextPrimary
import com.spendora.app.ui.viewmodel.AuthViewModel
import com.spendora.app.ui.viewmodel.BudgetViewModel
import com.spendora.app.ui.viewmodel.DashboardViewModel
import com.spendora.app.ui.viewmodel.ExpenseViewModel
import com.spendora.app.ui.viewmodel.GoalViewModel
import com.spendora.app.ui.viewmodel.IncomeViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    incomeViewModel: IncomeViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    goalViewModel: GoalViewModel = viewModel(),
    onOpenAiAssistant: () -> Unit = {}
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authenticatedRoutes = listOf(
        Screen.Dashboard.route,
        Screen.Expenses.route,
        Screen.Income.route,
        Screen.Budgets.route,
        Screen.Goals.route
    )
    val showBottomBar = currentRoute in authenticatedRoutes

    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                SpendoraBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Routes
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgot = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.ForgotPassword.route) {
                OtpForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }
                )
            }

            // Core Financial Screens
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    dashboardViewModel = dashboardViewModel,
                    expenseViewModel = expenseViewModel,
                    incomeViewModel = incomeViewModel,
                    authViewModel = authViewModel,
                    onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                    onNavigateToIncome = { navController.navigate(Screen.Income.route) },
                    onOpenAiAssistant = onOpenAiAssistant
                )
            }

            composable(Screen.Expenses.route) {
                ExpensesScreen(viewModel = expenseViewModel)
            }

            composable(Screen.Income.route) {
                IncomeScreen(viewModel = incomeViewModel)
            }

            composable(Screen.Budgets.route) {
                BudgetsScreen(viewModel = budgetViewModel)
            }

            composable(Screen.Goals.route) {
                GoalsScreen(viewModel = goalViewModel)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = TextPrimary)
    }
}
