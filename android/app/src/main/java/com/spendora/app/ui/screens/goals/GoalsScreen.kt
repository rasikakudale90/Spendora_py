package com.spendora.app.ui.screens.goals

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.GoalDto
import com.spendora.app.ui.components.*
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var showContributeSheet by remember { mutableStateOf(false) }
    var selectedGoalForContribute by remember { mutableStateOf<GoalDto?>(null) }
    var editingGoal by remember { mutableStateOf<GoalDto?>(null) }
    var deletingGoal by remember { mutableStateOf<GoalDto?>(null) }

    val statusFilters = listOf(
        null to "All Goals",
        "active" to "Active",
        "completed" to "Completed",
        "paused" to "Paused"
    )

    LaunchedEffect(Unit) {
        viewModel.loadGoals()
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGoal = null
                    showFormSheet = true
                },
                containerColor = PrimaryIndigo,
                contentColor = TextPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Goal")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Savings Goals & Runway",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI-powered runway forecasting and target milestones",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            if (uiState.isLoading && uiState.goalsResponse == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // KPI Strip: Total Saved, Target, Overall Progress %
                    val response = uiState.goalsResponse
                    if (response != null && response.totalCount > 0) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceDark
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Total Saved", fontSize = 11.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatInr(response.totalSavedAmount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = EmeraldSuccess
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Target Total", fontSize = 11.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatInr(response.totalTargetAmount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Progress", fontSize = 11.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${response.overallProgressPercentage.toInt()}%",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryIndigoLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Status Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(statusFilters) { (filterKey, filterLabel) ->
                                val isSelected = uiState.selectedStatusFilter == filterKey
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) PrimaryIndigo else SurfaceElevated,
                                    modifier = Modifier.clickable { viewModel.selectStatusFilter(filterKey) }
                                ) {
                                    Text(
                                        text = filterLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextPrimary else TextMuted,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Goals List
                    val goals = uiState.goalsResponse?.items ?: emptyList()
                    if (goals.isNotEmpty()) {
                        items(goals, key = { it.id }) { goal ->
                            GoalCard(
                                goal = goal,
                                onContribute = {
                                    selectedGoalForContribute = goal
                                    showContributeSheet = true
                                },
                                onEdit = {
                                    editingGoal = goal
                                    showFormSheet = true
                                },
                                onDelete = { deletingGoal = goal }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No financial goals found.\nTap + to create your first savings milestone!",
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
        GoalFormSheet(
            goal = editingGoal,
            onDismiss = { showFormSheet = false },
            onSave = { name, targetAmount, currentAmount, targetDate, category, color, notes ->
                if (editingGoal != null) {
                    viewModel.updateGoal(
                        id = editingGoal!!.id,
                        name = name,
                        targetAmount = targetAmount,
                        currentAmount = currentAmount,
                        targetDate = targetDate,
                        category = category,
                        color = color,
                        notes = notes,
                        onSuccess = { showFormSheet = false }
                    )
                } else {
                    viewModel.createGoal(
                        name = name,
                        targetAmount = targetAmount,
                        currentAmount = currentAmount,
                        targetDate = targetDate,
                        category = category,
                        color = color,
                        notes = notes,
                        onSuccess = { showFormSheet = false }
                    )
                }
            }
        )
    }

    // Contribution Sheet (Deposit / Withdraw)
    if (showContributeSheet && selectedGoalForContribute != null) {
        GoalContributeSheet(
            goal = selectedGoalForContribute!!,
            onDismiss = {
                showContributeSheet = false
                selectedGoalForContribute = null
            },
            onContribute = { amount, action, notes ->
                viewModel.contribute(
                    id = selectedGoalForContribute!!.id,
                    amount = amount,
                    action = action,
                    notes = notes,
                    onSuccess = {
                        showContributeSheet = false
                        selectedGoalForContribute = null
                    }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingGoal != null) {
        AlertDialog(
            onDismissRequest = { deletingGoal = null },
            title = { Text("Delete Goal?", color = TextPrimary) },
            text = { Text("Are you sure you want to delete '${deletingGoal!!.name}'?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGoal(deletingGoal!!.id)
                        deletingGoal = null
                    }
                ) {
                    Text("Delete", color = RoseDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGoal = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
