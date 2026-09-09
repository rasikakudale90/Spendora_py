package com.spendora.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.CategoryDto
import com.spendora.app.data.model.ExpenseDto
import com.spendora.app.data.model.PaymentMode
import com.spendora.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormSheet(
    expenseToEdit: ExpenseDto? = null,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, amount: Double, expenseDate: String, categoryId: String, paymentMode: PaymentMode, notes: String?) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var expenseDate by remember { mutableStateOf(expenseToEdit?.expenseDate ?: todayDate) }
    var selectedCategoryId by remember(expenseToEdit, categories) {
        mutableStateOf(expenseToEdit?.categoryId ?: categories.firstOrNull()?.id ?: "")
    }
    var selectedPaymentMode by remember { mutableStateOf(expenseToEdit?.paymentMode ?: PaymentMode.UPI) }
    var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(categories) {
        if (selectedCategoryId.isBlank() && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
            categoryError = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDark) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = if (expenseToEdit == null) "Add Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            SpendoraTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = "Title / Merchant",
                placeholder = "e.g. Swiggy, Groceries, Starbucks",
                errorMessage = titleError
            )

            Spacer(modifier = Modifier.height(12.dp))

            SpendoraTextField(
                value = amountText,
                onValueChange = { amountText = it; amountError = null },
                label = "Amount (₹)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                errorMessage = amountError
            )

            Spacer(modifier = Modifier.height(12.dp))

            SpendoraTextField(
                value = expenseDate,
                onValueChange = { expenseDate = it },
                label = "Date (YYYY-MM-DD)",
                placeholder = todayDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )
                if (categories.isNotEmpty()) {
                    val activeCat = categories.find { it.id == selectedCategoryId }
                    if (activeCat != null) {
                        Text(
                            text = "Selected: ${activeCat.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigoLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (categories.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryIndigoLight,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Loading categories...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategoryId == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryId = category.id
                                categoryError = null
                            },
                            label = {
                                Text(
                                    text = category.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                containerColor = SurfaceElevated
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) PrimaryIndigoLight else BorderDark
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !categoryError.isNullOrBlank()) {
                Text(
                    text = categoryError ?: "",
                    color = RoseDanger,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Payment Mode",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PaymentMode.values()) { mode ->
                    val isSelected = selectedPaymentMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentMode = mode },
                        label = {
                            Text(
                                text = mode.value,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            containerColor = SurfaceElevated
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryIndigoLight else BorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SpendoraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (Optional)",
                placeholder = "Add transaction remarks...",
                singleLine = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (expenseToEdit == null) "Save Expense" else "Update Expense",
                gradientBrush = PrimaryGradient,
                onClick = {
                    var valid = true
                    if (title.isBlank()) {
                        titleError = "Title is required"
                        valid = false
                    }
                    val amountVal = amountText.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        amountError = "Enter a valid positive amount"
                        valid = false
                    }
                    if (selectedCategoryId.isBlank()) {
                        categoryError = "Please select a category"
                        valid = false
                    }

                    if (valid && amountVal != null && selectedCategoryId.isNotBlank()) {
                        onSave(
                            expenseToEdit?.id,
                            title,
                            amountVal,
                            expenseDate.trim(),
                            selectedCategoryId,
                            selectedPaymentMode,
                            notes.ifBlank { null }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
