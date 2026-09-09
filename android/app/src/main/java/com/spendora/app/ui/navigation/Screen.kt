package com.spendora.app.ui.navigation

sealed class Screen(val route: String) {
    // Intro / Splash
    object Splash : Screen("splash")

    // Auth Routes
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Main App Routes
    object Dashboard : Screen("dashboard")
    object Expenses : Screen("expenses")
    object Income : Screen("income")
    object Budgets : Screen("budgets")
    object Goals : Screen("goals")
    object AiAssistant : Screen("ai_assistant")
}
