package com.spendora.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    onSave: (id: Int?, title: String, amount: Double, expenseDate: String, categoryId: Int, paymentMode: PaymentMode, notes: String?) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var expenseDate by remember { mutableStateOf(expenseToEdit?.expenseDate ?: todayDate) }
    var selectedCategoryId by remember { mutableStateOf(expenseToEdit?.categoryId ?: categories.firstOrNull()?.id ?: 1) }
    var selectedPaymentMode by remember { mutableStateOf(expenseToEdit?.paymentMode ?: PaymentMode.UPI) }
    var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (expenseToEdit == null) "Add Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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

            Text(
                text = "Category",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(text = category.name, color = if (isSelected) TextPrimary else TextSecondary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            containerColor = SurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Payment Mode",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
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
                        label = { Text(text = mode.value, color = if (isSelected) TextPrimary else TextSecondary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            containerColor = SurfaceElevated
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

                    if (valid && amountVal != null) {
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
