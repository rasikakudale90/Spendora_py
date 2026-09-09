package com.spendora.app.ui.screens.expenses

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.ExpenseDto
import com.spendora.app.data.model.PaymentMode
import com.spendora.app.ui.components.*
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseDto?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ExpenseDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadExpenses()
        viewModel.toastEvents.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                    expenseToEdit = null
                    showFormSheet = true
                },
                containerColor = PrimaryCyan,
                contentColor = OnPrimaryColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header Row: Spendora Logo & Screen Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
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
                            text = "EXPENSES LEDGER",
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
                        text = "${uiState.expenses.size} Items",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyanLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Search & Sort Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text(text = "Search expenses...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PrimaryCyanLight) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                IconButton(
                    onClick = { viewModel.toggleSortOrder() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = if (uiState.sortOrder == "desc") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = "Sort order",
                        tint = PrimaryCyanLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAllSelected = uiState.selectedCategoryId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isAllSelected) PrimaryCyan else SurfaceElevated)
                            .border(
                                1.dp,
                                if (isAllSelected) PrimaryCyan else BorderDark,
                                RoundedCornerShape(9999.dp)
                            )
                            .clickable { viewModel.onCategoryFilterSelect(null) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "All",
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAllSelected) OnPrimaryColor else TextSecondary
                        )
                    }
                }

                items(uiState.categories) { cat ->
                    val isSelected = uiState.selectedCategoryId == cat.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSelected) PrimaryCyan else SurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryCyan else BorderDark,
                                RoundedCornerShape(9999.dp)
                            )
                            .clickable { viewModel.onCategoryFilterSelect(if (isSelected) null else cat.id) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OnPrimaryColor else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expenses List
            if (uiState.isLoading && uiState.expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else if (uiState.expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🧾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No expenses found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap the '+' button to log a new expense",
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
                    items(uiState.expenses) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            onEdit = {
                                expenseToEdit = expense
                                showFormSheet = true
                            },
                            onDelete = {
                                showDeleteConfirmDialog = expense
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        ExpenseFormSheet(
            expenseToEdit = expenseToEdit,
            categories = uiState.categories,
            onDismiss = { showFormSheet = false },
            onSave = { id, title, amount, date, categoryId, paymentMode, notes ->
                viewModel.saveExpense(
                    id = id,
                    title = title,
                    amount = amount,
                    expenseDate = date,
                    categoryId = categoryId,
                    paymentMode = paymentMode,
                    notes = notes,
                    onSuccess = { showFormSheet = false }
                )
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        val expense = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(text = "Delete Expense", color = TextPrimary) },
            text = { Text(text = "Are you sure you want to delete '${expense.title}' of ${formatInr(expense.amount)}?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense.id)
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
