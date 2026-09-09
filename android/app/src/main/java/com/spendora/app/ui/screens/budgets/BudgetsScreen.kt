package com.spendora.app.ui.screens.budgets

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.BudgetDto
import com.spendora.app.ui.components.*
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetDto?>(null) }
    var deletingBudget by remember { mutableStateOf<BudgetDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadBudgets()
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.categories.isEmpty()) {
                        viewModel.loadCategories()
                    }
                    editingBudget = null
                    showFormSheet = true
                },
                containerColor = PrimaryIndigo,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Header: Spendora Logo & Budgets Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpendoraLogo(size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Spendora",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "BUDGETS & LIMITS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = PrimaryCyanLight
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = PrimaryCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${uiState.budgetData?.categoryBudgets?.size ?: 0} Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyanLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Period Switcher Tab Row (Daily, Weekly, Monthly, Yearly)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                PeriodTabRow(
                    selectedPeriod = uiState.selectedPeriod,
                    onSelectPeriod = { viewModel.selectPeriod(it) }
                )
            }

            if (uiState.isLoading && uiState.budgetData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Month Telemetry Banner
                    item {
                        val calendar = java.util.Calendar.getInstance()
                        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        val elapsedPct = ((dayOfMonth.toFloat() / maxDays.toFloat()) * 100).toInt()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
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
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = PrimaryCyanLight,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${uiState.selectedPeriod.replaceFirstChar { it.uppercase() }} Budget Cycle",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = PrimaryCyan.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Day $dayOfMonth / $maxDays",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryCyanLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$dayOfMonth of $maxDays days elapsed ($elapsedPct% of fiscal cycle)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { elapsedPct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(9999.dp)),
                                    color = PrimaryCyan,
                                    trackColor = BorderDark
                                )
                            }
                        }
                    }

                    // Aggregated Spend Cap Master Gauge Card
                    val overall = uiState.budgetData?.overallBudget
                    val totalCap = overall?.amount ?: (uiState.budgetData?.categoryBudgets?.sumOf { it.amount } ?: 0.0)
                    val totalSpent = overall?.spent ?: (uiState.budgetData?.categoryBudgets?.sumOf { it.spent } ?: 0.0)
                    val remainingBuffer = (totalCap - totalSpent).coerceAtLeast(0.0)
                    val pctAllocated = if (totalCap > 0) ((totalSpent / totalCap) * 100).coerceIn(0.0, 100.0) else 0.0

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardSurfaceGradient)
                                .border(1.dp, BorderDark, RoundedCornerShape(22.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = "AGGREGATED SPEND CAP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp
                                            ),
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = formatInr(totalSpent),
                                                style = TelemetryMetricTextStyle.copy(fontSize = 20.sp),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "/ ${formatInr(totalCap)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = if (pctAllocated > 90.0) RoseBg else EmeraldBg,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (pctAllocated > 90.0) RoseDanger.copy(alpha = 0.3f) else EmeraldSuccess.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = if (pctAllocated > 90.0) RoseDangerLight else EmeraldSuccessLight,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${"%.0f".format(pctAllocated)}% Allocated",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (pctAllocated > 90.0) RoseDangerLight else EmeraldSuccessLight
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Glowing Gradient Velocity Track
                                LinearProgressIndicator(
                                    progress = { (pctAllocated / 100f).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(9999.dp)),
                                    color = if (pctAllocated > 90.0) RoseDanger else PrimaryCyan,
                                    trackColor = BorderDark
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "₹0.00", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "Burn Velocity: ${"%.2f".format(if (pctAllocated > 0) pctAllocated / 75.0 else 1.0)}x (${if (pctAllocated <= 85.0) "Optimal" else "Elevated"})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = PrimaryCyanLight
                                    )
                                    Text(text = formatInr(totalCap), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Reserve Status Pod
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceContainerLowest.copy(alpha = 0.8f))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                .background(EmeraldBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Security,
                                                    contentDescription = null,
                                                    tint = EmeraldSuccessLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Reserve Status",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted
                                                )
                                                Text(
                                                    text = "${formatInr(remainingBuffer)} Safe Buffer Remaining",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = EmeraldSuccessLight
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2 Quick Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (uiState.categories.isEmpty()) {
                                        viewModel.loadCategories()
                                    }
                                    editingBudget = null
                                    showFormSheet = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceElevated,
                                    contentColor = PrimaryCyanLight
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            ) {
                                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "+ Create Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = {
                                    if (overall != null) {
                                        editingBudget = overall
                                        showFormSheet = true
                                    } else {
                                        editingBudget = null
                                        showFormSheet = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceElevated,
                                    contentColor = QuantumVioletLight
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            ) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Overall Limit", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    // Departmental & Personal Limits Section Header
                    val categoryBudgets = uiState.budgetData?.categoryBudgets ?: emptyList()
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Departmental & Personal Limits",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${categoryBudgets.size} Active Limits",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PrimaryCyanLight
                            )
                        }
                    }

                    if (categoryBudgets.isNotEmpty()) {
                        items(categoryBudgets, key = { it.id }) { catBudget ->
                            BudgetCard(
                                budget = catBudget,
                                onEdit = {
                                    editingBudget = catBudget
                                    showFormSheet = true
                                },
                                onDelete = { deletingBudget = catBudget }
                            )
                        }
                    } else {
                        item {
                            SpendoraCard {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No category limits configured for this period.\nTap + Create Category to allocate caps.",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Predictive Burn Forecast Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
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
                                            imageVector = Icons.Default.Insights,
                                            contentDescription = null,
                                            tint = PrimaryCyanLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Predictive Burn Forecast",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = EmeraldBg
                                    ) {
                                        Text(
                                            text = "On Track",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = EmeraldSuccessLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(text = "Projected Month-End Close", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            text = formatInr((totalSpent * 1.15).coerceAtLeast(totalSpent)),
                                            style = TelemetryMetricTextStyle.copy(fontSize = 18.sp),
                                            color = PrimaryCyanLight
                                        )
                                    }
                                    Text(
                                        text = "${formatInr(remainingBuffer)} under cap",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldSuccessLight
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Modal Form Sheet
    if (showFormSheet) {
        BudgetFormSheet(
            budget = editingBudget,
            categories = uiState.categories,
            initialPeriod = uiState.selectedPeriod,
            onDismiss = { showFormSheet = false },
            onSave = { scope, categoryId, amount, periodType ->
                if (editingBudget != null) {
                    viewModel.updateBudget(
                        id = editingBudget!!.id,
                        amount = amount,
                        onSuccess = { showFormSheet = false }
                    )
                } else {
                    viewModel.setBudget(
                        scope = scope,
                        categoryId = categoryId,
                        amount = amount,
                        periodType = periodType,
                        onSuccess = { showFormSheet = false }
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingBudget != null) {
        AlertDialog(
            onDismissRequest = { deletingBudget = null },
            title = { Text("Delete Budget?", color = TextPrimary) },
            text = {
                val name = if (deletingBudget!!.scope == "overall") "Overall Budget" else (deletingBudget!!.categoryName ?: "Category Budget")
                Text("Are you sure you want to delete the $name limit of ${formatInr(deletingBudget!!.amount)}?", color = TextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBudget(deletingBudget!!.id)
                        deletingBudget = null
                    }
                ) {
                    Text("Delete", color = RoseDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBudget = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
