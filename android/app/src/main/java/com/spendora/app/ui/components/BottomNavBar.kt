package com.spendora.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.navigation.Screen
import com.spendora.app.ui.theme.*

sealed class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard)
    object Expenses : BottomNavItem("Expenses", Screen.Expenses.route, Icons.Default.ReceiptLong)
    object Income : BottomNavItem("Income", Screen.Income.route, Icons.Default.AccountBalanceWallet)
    object Budgets : BottomNavItem("Budgets", Screen.Budgets.route, Icons.Default.PieChart)
    object Goals : BottomNavItem("Goals", Screen.Goals.route, Icons.Default.Flag)
}

@Composable
fun SpendoraBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Expenses,
        BottomNavItem.Income,
        BottomNavItem.Budgets,
        BottomNavItem.Goals
    )

    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) PrimaryIndigoLight else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryIndigo.copy(alpha = 0.2f)
                )
            )
        }
    }
}
