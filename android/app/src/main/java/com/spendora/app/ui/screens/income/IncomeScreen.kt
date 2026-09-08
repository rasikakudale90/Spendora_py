package com.spendora.app.ui.screens.income

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.IncomeDto
import com.spendora.app.ui.components.*
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.IncomeViewModel

@Composable
fun IncomeScreen(
    viewModel: IncomeViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var incomeToEdit by remember { mutableStateOf<IncomeDto?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<IncomeDto?>(null) }

    val sources = listOf("All", "Salary", "Freelance", "Investment", "Bonus", "Gift", "Rental", "Other")

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    incomeToEdit = null
                    showFormSheet = true
                },
                containerColor = EmeraldSuccess,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Income")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Income & Cash Flow",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            // Monthly Summary Card
            if (uiState.monthlySummary != null) {
                SpendoraCard(
                    backgroundColor = EmeraldBg.copy(alpha = 0.3f),
                    borderColor = EmeraldSuccess.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Income This Month",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldSuccessLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatInr(uiState.monthlySummary?.totalIncome ?: 0.0),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${uiState.monthlySummary?.incomeCount ?: 0} entries",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccessLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text(text = "Search income entries...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = EmeraldSuccess,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Source Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sources) { src ->
                    val isSelected = (src == "All" && uiState.selectedSource == null) || (uiState.selectedSource == src)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSourceFilterSelect(if (src == "All") null else src) },
                        label = { Text(text = src, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            containerColor = SurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Incomes List
            if (uiState.isLoading && uiState.incomes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldSuccess)
                }
            } else if (uiState.incomes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "💰", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No income entries found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap the '+' button to log a salary or income stream",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.incomes) { income ->
                        IncomeItemRow(
                            income = income,
                            onEdit = {
                                incomeToEdit = income
                                showFormSheet = true
                            },
                            onDelete = {
                                showDeleteConfirmDialog = income
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        IncomeFormSheet(
            incomeToEdit = incomeToEdit,
            onDismiss = { showFormSheet = false },
            onSave = { id, title, amount, date, source, notes ->
                viewModel.saveIncome(
                    id = id,
                    title = title,
                    amount = amount,
                    incomeDate = date,
                    source = source,
                    notes = notes,
                    onSuccess = { showFormSheet = false }
                )
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        val income = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(text = "Delete Income Entry", color = TextPrimary) },
            text = { Text(text = "Are you sure you want to delete '${income.title}' of +${formatInr(income.amount)}?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteIncome(income.id)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text(text = "Delete", color = RoseDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
