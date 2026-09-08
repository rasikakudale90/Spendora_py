package com.spendora.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.PaymentMode
import com.spendora.app.ui.components.*
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AuthViewModel
import com.spendora.app.ui.viewmodel.DashboardViewModel
import com.spendora.app.ui.viewmodel.ExpenseViewModel
import com.spendora.app.ui.viewmodel.IncomeViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    expenseViewModel: ExpenseViewModel,
    incomeViewModel: IncomeViewModel,
    authViewModel: AuthViewModel,
    onNavigateToExpenses: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onOpenAiAssistant: () -> Unit = {}
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddIncomeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${currentUser?.fullName?.split(" ")?.firstOrNull() ?: "there"} 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Here is your live financial snapshot",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddExpenseSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Expense", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showAddIncomeSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Income", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = onOpenAiAssistant,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryIndigo)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // KPI Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Total Income",
                        amount = dashboardState.summary.totalIncome,
                        icon = Icons.Default.ArrowUpward,
                        iconTint = EmeraldSuccess,
                        iconBg = EmeraldSuccess.copy(alpha = 0.15f),
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Total Spent",
                        amount = dashboardState.summary.totalSpent,
                        icon = Icons.Default.ArrowDownward,
                        iconTint = RoseDanger,
                        iconBg = RoseDanger.copy(alpha = 0.15f),
                        isPositive = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isSurplus = dashboardState.summary.netSavings >= 0
                    KpiCard(
                        title = "Net Cash Flow",
                        amount = dashboardState.summary.netSavings,
                        subtitle = if (isSurplus) "Surplus" else "Deficit",
                        icon = Icons.Default.AccountBalance,
                        iconTint = if (isSurplus) EmeraldSuccess else RoseDanger,
                        iconBg = (if (isSurplus) EmeraldSuccess else RoseDanger).copy(alpha = 0.15f),
                        isPositive = isSurplus,
                        modifier = Modifier.weight(1f)
                    )

                    SpendoraCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Savings Rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(dashboardState.summary.savingsRate)}%",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (dashboardState.summary.savingsRate >= 20.0) EmeraldSuccess else AmberWarning
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${dashboardState.summary.expenseCount} transactions",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Top Categories Breakdown
            if (dashboardState.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Spending Categories",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                item {
                    SpendoraCard {
                        dashboardState.categoryBreakdown.take(4).forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.categoryName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${"%.1f".format(cat.percentage)}%",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatInr(cat.amount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Expenses",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryIndigoLight,
                        modifier = Modifier.clickable { onNavigateToExpenses() }
                    )
                }
            }

            if (dashboardState.recentExpenses.isEmpty()) {
                item {
                    SpendoraCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses recorded this month yet.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(dashboardState.recentExpenses) { expense ->
                    ExpenseItemRow(
                        expense = expense,
                        onEdit = { onNavigateToExpenses() },
                        onDelete = {
                            expenseViewModel.deleteExpense(expense.id)
                            dashboardViewModel.loadDashboard()
                        }
                    )
                }
            }
        }
    }

    if (showAddExpenseSheet) {
        ExpenseFormSheet(
            categories = expenseState.categories,
            onDismiss = { showAddExpenseSheet = false },
            onSave = { _, title, amount, date, categoryId, paymentMode, notes ->
                expenseViewModel.saveExpense(
                    id = null,
                    title = title,
                    amount = amount,
                    expenseDate = date,
                    categoryId = categoryId,
                    paymentMode = paymentMode,
                    notes = notes,
                    onSuccess = {
                        showAddExpenseSheet = false
                        dashboardViewModel.loadDashboard()
                    }
                )
            }
        )
    }

    if (showAddIncomeSheet) {
        IncomeFormSheet(
            onDismiss = { showAddIncomeSheet = false },
            onSave = { _, title, amount, date, source, notes ->
                incomeViewModel.saveIncome(
                    id = null,
                    title = title,
                    amount = amount,
                    incomeDate = date,
                    source = source,
                    notes = notes,
                    onSuccess = {
                        showAddIncomeSheet = false
                        dashboardViewModel.loadDashboard()
                    }
                )
            }
        )
    }
}
