package com.spendora.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    object Dashboard : BottomNavItem("Overview", Screen.Dashboard.route, Icons.Default.Dashboard)
    object Analytics : BottomNavItem("Analytics", Screen.Analytics.route, Icons.Default.QueryStats)
    object Expenses : BottomNavItem("Ledger", Screen.Expenses.route, Icons.AutoMirrored.Filled.ReceiptLong)
    object Budgets : BottomNavItem("Budgets", Screen.Budgets.route, Icons.Default.AccountBalanceWallet)
    object Goals : BottomNavItem("Vault", Screen.Goals.route, Icons.Default.Lock)
}

@Composable
fun SpendoraBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Analytics,
        BottomNavItem.Expenses,
        BottomNavItem.Budgets,
        BottomNavItem.Goals
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderDark),
        containerColor = BackgroundDark,
        tonalElevation = 12.dp
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
                        tint = if (isSelected) PrimaryCyanLight else TextMuted
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryCyanLight else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryCyanLight,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = PrimaryCyanLight,
                    unselectedTextColor = TextMuted,
                    indicatorColor = PrimaryCyan.copy(alpha = 0.20f)
                )
            )
        }
    }
}
