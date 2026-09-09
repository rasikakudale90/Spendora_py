package com.spendora.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendora.app.data.model.PaymentMode
import com.spendora.app.ui.components.*
import com.spendora.app.ui.components.ai.FinancialHealthRadar
import com.spendora.app.ui.components.ai.LeakHunterSheet
import com.spendora.app.ui.components.ai.PurchaseSimulatorSheet
import com.spendora.app.ui.components.ai.SafeToSpendGauge
import com.spendora.app.ui.components.ai.SmartScannerSheet
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel
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
    aiViewModel: AiViewModel = viewModel(),
    onNavigateToExpenses: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onOpenAiAssistant: () -> Unit = {}
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val aiState by aiViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddIncomeSheet by remember { mutableStateOf(false) }
    var showSimulatorSheet by remember { mutableStateOf(false) }
    var showLeakSheet by remember { mutableStateOf(false) }
    var showScannerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
        aiViewModel.loadSafeToSpend()
        aiViewModel.loadFinancialHealth()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Hello, ${currentUser?.fullName?.split(" ")?.firstOrNull() ?: "there"} 👋",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, BorderDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Actions Bar (Expense, Income, AI Advisor)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RoseGradient)
                            .clickable { showAddExpenseSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Expense", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldGradient)
                            .clickable { showAddIncomeSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Income", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryGradient)
                            .clickable { onOpenAiAssistant() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // AI Intelligence Quick Access Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.clickable { showSimulatorSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = PrimaryIndigoLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Can I Afford This?", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.clickable { showLeakSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = RoseDangerLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Leak Hunter", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.clickable { showScannerSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = EmeraldSuccessLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan SMS Alert", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // KPI Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Total Income",
                        amount = dashboardState.summary.totalIncome,
                        icon = Icons.Default.ArrowUpward,
                        iconTint = EmeraldSuccessLight,
                        iconBg = EmeraldBg,
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Total Spent",
                        amount = dashboardState.summary.totalSpent,
                        icon = Icons.Default.ArrowDownward,
                        iconTint = RoseDangerLight,
                        iconBg = RoseBg,
                        isPositive = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isSurplus = dashboardState.summary.netSavings >= 0
                    KpiCard(
                        title = "Net Cash Flow",
                        amount = dashboardState.summary.netSavings,
                        subtitle = if (isSurplus) "Surplus" else "Deficit",
                        icon = Icons.Default.AccountBalance,
                        iconTint = if (isSurplus) EmeraldSuccessLight else RoseDangerLight,
                        iconBg = if (isSurplus) EmeraldBg else RoseBg,
                        isPositive = isSurplus,
                        modifier = Modifier.weight(1f)
                    )

                    SpendoraCard(
                        modifier = Modifier.weight(1f),
                        borderColor = if (dashboardState.summary.savingsRate >= 20.0) EmeraldSuccess.copy(alpha = 0.3f) else BorderDark
                    ) {
                        Text(
                            text = "Savings Rate",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${"%.1f".format(dashboardState.summary.savingsRate)}%",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (dashboardState.summary.savingsRate >= 20.0) EmeraldSuccessLight else AmberWarningLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${dashboardState.summary.expenseCount} entries this month",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            // AI Feature 3: Safe-to-Spend Real-Time Speedometer Gauge
            if (aiState.safeToSpend != null) {
                item {
                    SafeToSpendGauge(
                        safeToSpend = aiState.safeToSpend,
                        onCardClick = onOpenAiAssistant
                    )
                }
            }

            // AI Feature 6: Financial Health 5-Pillar Spider Radar Chart
            if (aiState.financialHealth != null) {
                item {
                    FinancialHealthRadar(
                        health = aiState.financialHealth,
                        onCardClick = onOpenAiAssistant
                    )
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
                        aiViewModel.loadSafeToSpend()
                        aiViewModel.loadFinancialHealth()
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
                        aiViewModel.loadSafeToSpend()
                        aiViewModel.loadFinancialHealth()
                    }
                )
            }
        )
    }

    if (showSimulatorSheet) {
        PurchaseSimulatorSheet(
            aiViewModel = aiViewModel,
            categories = expenseState.categories,
            onDismiss = { showSimulatorSheet = false },
            onAddAsExpense = { itemTitle, itemAmt ->
                val categories = expenseState.categories
                val firstCatId = categories.firstOrNull()?.id ?: ""
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

                expenseViewModel.saveExpense(
                    id = null,
                    title = itemTitle,
                    amount = itemAmt,
                    expenseDate = today,
                    categoryId = firstCatId,
                    paymentMode = PaymentMode.UPI,
                    notes = "Added via AI Purchase Simulator",
                    onSuccess = {
                        dashboardViewModel.loadDashboard()
                        aiViewModel.loadSafeToSpend()
                        aiViewModel.loadFinancialHealth()
                    }
                )
            }
        )
    }

    if (showLeakSheet) {
        LeakHunterSheet(
            aiViewModel = aiViewModel,
            onDismiss = { showLeakSheet = false }
        )
    }

    if (showScannerSheet) {
        SmartScannerSheet(
            aiViewModel = aiViewModel,
            expenseViewModel = expenseViewModel,
            onDismiss = {
                showScannerSheet = false
                dashboardViewModel.loadDashboard()
                aiViewModel.loadSafeToSpend()
                aiViewModel.loadFinancialHealth()
            }
        )
    }
}
