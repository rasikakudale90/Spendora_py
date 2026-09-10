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
import com.spendora.app.ui.components.ai.FinancialHealthDetailSheet
import com.spendora.app.ui.components.ai.FinancialHealthRadar
import com.spendora.app.ui.components.ai.LeakHunterSheet
import com.spendora.app.ui.components.ai.PurchaseSimulatorSheet
import com.spendora.app.ui.components.ai.SafeToSpendDetailSheet
import com.spendora.app.ui.components.ai.SafeToSpendGauge
import com.spendora.app.ui.components.ai.SmartScannerSheet
import com.spendora.app.ui.screens.reports.StatementExportSheet
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel
import com.spendora.app.ui.viewmodel.AuthViewModel
import com.spendora.app.ui.viewmodel.DashboardViewModel
import com.spendora.app.ui.viewmodel.ExpenseViewModel
import com.spendora.app.ui.viewmodel.IncomeViewModel
import com.spendora.app.ui.viewmodel.ReportViewModel


@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    expenseViewModel: ExpenseViewModel,
    incomeViewModel: IncomeViewModel,
    authViewModel: AuthViewModel,
    aiViewModel: AiViewModel = viewModel(),
    reportViewModel: ReportViewModel = viewModel(),
    onNavigateToExpenses: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onOpenAiAssistant: () -> Unit = {}
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val expenseState by expenseViewModel.uiState.collectAsState()
    val incomeState by incomeViewModel.uiState.collectAsState()
    val aiState by aiViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddIncomeSheet by remember { mutableStateOf(false) }
    var showSimulatorSheet by remember { mutableStateOf(false) }
    var showLeakSheet by remember { mutableStateOf(false) }
    var showScannerSheet by remember { mutableStateOf(false) }
    var showSafeToSpendSheet by remember { mutableStateOf(false) }
    var showHealthDetailSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showStatementSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    var selectedAccountIndex by remember { mutableIntStateOf(0) }
    var selectedActivityFilter by remember { mutableStateOf("All") }

    val accountOptions = listOf(
        "All Ledgers" to null,
        "UPI & Online" to "UPI",
        "Cards & Bank" to "Card",
        "Cash Reserve" to "Cash"
    )
    val accountDotColors = listOf(PrimaryCyan, QuantumViolet, PrimaryCyanLight, EmeraldSuccess)

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
        expenseViewModel.loadCategories()
        expenseViewModel.loadExpenses()
        incomeViewModel.loadIncomes()
        incomeViewModel.loadMonthlySummary()
        aiViewModel.loadSafeToSpend()
        aiViewModel.loadFinancialHealth()
    }

    val selectedPaymentMode = accountOptions[selectedAccountIndex].second
    val activeAccountExpenses = remember(expenseState.expenses, selectedPaymentMode) {
        if (selectedPaymentMode == null) {
            expenseState.expenses
        } else {
            expenseState.expenses.filter { it.paymentMode.name.equals(selectedPaymentMode, ignoreCase = true) }
        }
    }
    val activeAccountSpent = remember(activeAccountExpenses, selectedPaymentMode, dashboardState.summary.totalSpent) {
        if (selectedPaymentMode == null) {
            dashboardState.summary.totalSpent
        } else {
            activeAccountExpenses.sumOf { it.amount }
        }
    }
    val activeAccountTxCount = remember(activeAccountExpenses, selectedPaymentMode, dashboardState.summary.expenseCount) {
        if (selectedPaymentMode == null) {
            dashboardState.summary.expenseCount
        } else {
            activeAccountExpenses.size
        }
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Spendora Logo, Overview Title, AI Assistant, Notification Bell & 3-Dots Menu
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpendoraLogo(size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Spendora",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "OVERVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 10.sp
                                ),
                                color = PrimaryCyanLight,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentTheme by authViewModel.themeMode.collectAsState()
                        val isDarkMode = currentTheme == "dark"

                        // Theme Toggle (Sun / Moon)
                        IconButton(
                            onClick = { authViewModel.toggleTheme() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                                .border(1.dp, BorderDark, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkMode) AmberWarningLight else PrimaryCyanLight,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // AI Assistant Sparkle Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryCyan.copy(alpha = 0.15f))
                                .border(1.dp, PrimaryCyan.copy(alpha = 0.45f), CircleShape)
                                .clickable { onOpenAiAssistant() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Open AI Financial Assistant",
                                tint = PrimaryCyanLight,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Notification Bell with Active Alerts Badge
                        val hasAlerts = (aiState.safeToSpend?.burnPacePercentage ?: 0.0) > 105.0 ||
                                (aiState.leakAnalysis?.detectedSubscriptions?.isNotEmpty() == true)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                                .border(1.dp, BorderDark, CircleShape)
                                .clickable { showNotificationsSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications & Alerts",
                                tint = if (hasAlerts) AmberWarningLight else TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                            if (hasAlerts) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarningLight)
                                )
                            }
                        }

                        // 3-Dots Overflow Menu for Secondary Tools
                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = !showOverflowMenu },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderDark, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false },
                                modifier = Modifier
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Export Statement (CSV/PDF)",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showStatementSheet = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = PrimaryCyanLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Spending Leak Audit",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showLeakSheet = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = QuantumViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Purchase Simulator",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showSimulatorSheet = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Calculate,
                                            contentDescription = null,
                                            tint = EmeraldSuccessLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Financial Health Radar",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        showHealthDetailSheet = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = PrimaryCyanLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = BorderDark
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Sign Out",
                                            color = RoseDangerLight,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        authViewModel.logout()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = RoseDangerLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Account Selector Rail (Functional Filter for All, UPI, Cards, Cash with dynamic balances)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accountOptions.indices.toList()) { index ->
                        val (accName, mode) = accountOptions[index]
                        val dotColor = accountDotColors[index % accountDotColors.size]
                        val isSelected = selectedAccountIndex == index
                        val accAmount = remember(expenseState.expenses, mode, dashboardState.summary.totalSpent) {
                            if (mode == null) {
                                (dashboardState.summary.totalIncome - dashboardState.summary.totalSpent).coerceAtLeast(0.0)
                            } else {
                                expenseState.expenses.filter { it.paymentMode.name.equals(mode, ignoreCase = true) }.sumOf { it.amount }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryCyan.copy(alpha = 0.6f) else BorderDark,
                                    RoundedCornerShape(9999.dp)
                                )
                                .clickable { selectedAccountIndex = index }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = "$accName (${formatInr(accAmount)})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = if (isSelected) JetBrainsMonoFontFamily else HankenGroteskFontFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryCyanLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hero Net Worth & Monthly Burn Telemetry Card with Canvas Sparkline
            item {
                var isBalanceHidden by remember { mutableStateOf(false) }
                val netWorth = dashboardState.summary.totalIncome - activeAccountSpent
                val isPositive = netWorth >= 0
                val totalSpent = activeAccountSpent
                val totalBudget = if (dashboardState.summary.totalIncome > 0) dashboardState.summary.totalIncome else 50000.0
                val burnPct = ((totalSpent / totalBudget) * 100).coerceIn(0.0, 100.0)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurfaceGradient)
                        .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        // Top Row: Micro Tag & Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (selectedPaymentMode != null) "[SYS.02] ${selectedPaymentMode.uppercase()} POOL" else "[SYS.01] CONSOLIDATED LIQUIDITY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.1.sp
                                    ),
                                    color = PrimaryCyanLight
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Balance Visibility",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = EmeraldBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = EmeraldSuccessLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "+${"%.1f".format(dashboardState.summary.savingsRate)}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = EmeraldSuccessLight
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Big Bold Net Worth Display
                        Text(
                            text = if (isBalanceHidden) "••••••••" else formatInr(netWorth),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = if (isPositive) TextPrimary else RoseDanger
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live telemetry sync • $activeAccountTxCount transactions",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = HankenGroteskFontFamily),
                                color = TextMuted
                            )
                            Text(
                                text = "₹ INR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PrimaryCyanLight
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Inflow vs Outflow comparison mini bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLowest)
                                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In: +${formatInr(dashboardState.summary.totalIncome)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = EmeraldSuccessLight
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(RoseDanger)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Out: -${formatInr(totalSpent)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = RoseDangerLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Monthly Burn Velocity Track Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceContainerLowest.copy(alpha = 0.8f))
                                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = PrimaryCyanLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Monthly Velocity",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "${formatInr(totalSpent)} / ${formatInr(totalBudget)}",
                                        style = TelemetryMetricTextStyle.copy(fontSize = 12.sp),
                                        color = PrimaryCyanLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Gradient Neumorphic Track
                                LinearProgressIndicator(
                                    progress = { (burnPct / 100f).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(9999.dp)),
                                    color = PrimaryCyan,
                                    trackColor = BorderDark
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${"%.0f".format(burnPct)}% of soft budget",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${formatInr((totalBudget - totalSpent).coerceAtLeast(0.0))} remaining buffer",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = EmeraldSuccessLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Safe-to-Spend Daily Gauge + Next Paycheck Pod
            item {
                val safeDaily = aiState.safeToSpend?.dailySafeSpend ?: (if (dashboardState.summary.totalIncome > dashboardState.summary.totalSpent) (dashboardState.summary.totalIncome - dashboardState.summary.totalSpent) / 20.0 else 250.0)
                val daysRemaining = aiState.safeToSpend?.daysRemainingInMonth ?: 7

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                        .clickable { showSafeToSpendSheet = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryCyan.copy(alpha = 0.15f))
                                    .border(1.dp, PrimaryCyan.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = PrimaryCyanLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = formatInr(safeDaily),
                                        style = TelemetryMetricTextStyle.copy(fontSize = 17.sp),
                                        color = PrimaryCyanLight
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "/ DAY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = "Safe-to-spend target active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(QuantumViolet)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$daysRemaining Days",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = QuantumViolet
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "till Paycheck",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // 4 Cybernetic Tactical Action Pods (+ Expense, + Income, Scan Bill, Simulate)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // + Expense (Crimson Rose)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (expenseState.categories.isEmpty()) {
                                    expenseViewModel.loadCategories()
                                }
                                showAddExpenseSheet = true
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(RoseDanger.copy(alpha = 0.12f))
                                .border(1.dp, RoseDanger.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Add Expense",
                                tint = RoseDangerLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ Expense",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }

                    // + Income (Emerald Green)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showAddIncomeSheet = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(EmeraldBg)
                                .border(1.dp, EmeraldSuccess.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Add Income",
                                tint = EmeraldSuccessLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ Income",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }

                    // Scan Bill (Electric Cyan)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showScannerSheet = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PrimaryCyan.copy(alpha = 0.15f))
                                .border(1.dp, PrimaryCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "Scan Bill",
                                tint = PrimaryCyanLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan Bill",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }

                    // Simulate (Quantum Violet)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSimulatorSheet = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(QuantumViolet.copy(alpha = 0.15f))
                                .border(1.dp, QuantumViolet.copy(alpha = 0.45f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Simulate",
                                tint = QuantumViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Simulate",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }

            // AI Scout Telemetry Campaign Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, PrimaryCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = PrimaryCyanLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Auto-Yield Scout",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = PrimaryCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "AI ON",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = PrimaryCyanLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Optimal 4.85% APY vault allocation found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { showLeakSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCyan,
                                contentColor = OnPrimaryColor
                            ),
                            shape = RoundedCornerShape(9999.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Review",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Spending Clusters (Proportional Segment Bar & Badges Grid)
            item {
                val categories = dashboardState.categoryBreakdown
                val totalClusterSpend = categories.sumOf { it.amount }.coerceAtLeast(1.0)
                val colors = listOf(PrimaryCyan, QuantumViolet, EmeraldSuccess, TextMuted)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                        .clickable { onNavigateToExpenses() }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = PrimaryCyanLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Spending Clusters",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "View Breakdown →",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryCyanLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Proportional segmented bar
                        if (categories.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(SurfaceContainerHighest)
                                    .padding(1.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                categories.take(4).forEachIndexed { idx, cat ->
                                    val segmentWeight = (cat.amount / totalClusterSpend).toFloat().coerceIn(0.05f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .weight(segmentWeight)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(colors[idx % colors.size])
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2-Column Badges Grid
                            val displayCats = categories.take(4)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                displayCats.chunked(2).forEach { rowCats ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowCats.forEachIndexed { rowIdx, cat ->
                                            val colorIdx = categories.indexOf(cat)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(SurfaceContainerLow)
                                                    .clickable { onNavigateToExpenses() }
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(colors[colorIdx % colors.size])
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = cat.categoryName,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextPrimary,
                                                            maxLines = 1
                                                        )
                                                    }
                                                    Text(
                                                        text = formatInr(cat.amount),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                        if (rowCats.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No spending clusters yet this month",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Text(
                        text = "View Ledger",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryCyanLight,
                        modifier = Modifier.clickable { onNavigateToExpenses() }
                    )
                }
            }

            // Swipe Filter Bar
            item {
                val filters = listOf("All", "Expenses", "Income", "Recurring")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedActivityFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) PrimaryCyan else SurfaceElevated)
                                .clickable { selectedActivityFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) OnPrimaryColor else TextSecondary
                            )
                        }
                    }
                }
            }

            // Transactions Feed
            when (selectedActivityFilter) {
                "Income" -> {
                    if (incomeState.incomes.isEmpty()) {
                        item {
                            SpendoraCard {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No income transactions logged this cycle.",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(incomeState.incomes.take(10), key = { "inc_${it.id}" }) { income ->
                            IncomeItemRow(
                                income = income,
                                onEdit = { onNavigateToIncome() },
                                onDelete = {
                                    incomeViewModel.deleteIncome(income.id)
                                    dashboardViewModel.loadDashboard()
                                }
                            )
                        }
                    }
                }
                else -> {
                    if (activeAccountExpenses.isEmpty()) {
                        item {
                            SpendoraCard {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selectedPaymentMode != null) "No $selectedPaymentMode transactions recorded." else "No recent activity found.",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(activeAccountExpenses.take(10), key = { "exp_${it.id}" }) { expense ->
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

    if (showSafeToSpendSheet) {
        SafeToSpendDetailSheet(
            aiViewModel = aiViewModel,
            onDismiss = { showSafeToSpendSheet = false }
        )
    }

    if (showHealthDetailSheet) {
        FinancialHealthDetailSheet(
            aiViewModel = aiViewModel,
            onDismiss = { showHealthDetailSheet = false }
        )
    }

    if (showNotificationsSheet) {
        SpendoraNotificationsSheet(
            summary = dashboardState.summary,
            safeToSpend = aiState.safeToSpend,
            leakAnalysis = aiState.leakAnalysis,
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showStatementSheet) {
        StatementExportSheet(
            viewModel = reportViewModel,
            onDismiss = { showStatementSheet = false }
        )
    }
}

