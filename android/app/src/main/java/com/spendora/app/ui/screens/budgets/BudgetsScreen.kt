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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
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
import com.spendora.app.ui.components.BudgetCard
import com.spendora.app.ui.components.BudgetFormSheet
import com.spendora.app.ui.components.PeriodTabRow
import com.spendora.app.ui.components.formatInr
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
            // Header & Period Picker
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Budgets & Limits",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Control multi-period limits and prevent overspending",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Period Switcher (Daily, Weekly, Monthly, Yearly)
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
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Overall Budget Section
                    val overall = uiState.budgetData?.overallBudget
                    if (overall != null) {
                        item {
                            Text(
                                text = "OVERALL BUDGET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BudgetCard(
                                budget = overall,
                                onEdit = {
                                    editingBudget = overall
                                    showFormSheet = true
                                },
                                onDelete = { deletingBudget = overall }
                            )
                        }
                    } else {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                                    .clickable {
                                        editingBudget = null
                                        showFormSheet = true
                                    },
                                color = SurfaceDark
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PieChart,
                                                contentDescription = null,
                                                tint = PrimaryIndigoLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Set ${uiState.selectedPeriod.replaceFirstChar { it.uppercase() }} Budget",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "No overall limit set for this period",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = PrimaryIndigoLight
                                    )
                                }
                            }
                        }
                    }

                    // Category Budgets Section
                    val categoryBudgets = uiState.budgetData?.categoryBudgets ?: emptyList()
                    if (categoryBudgets.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "CATEGORY BUDGETS (${categoryBudgets.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                        }

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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No category budgets set for this period.\nTap + to allocate spending limits.",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
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
