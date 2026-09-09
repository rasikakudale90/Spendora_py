package com.spendora.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spendora.app.data.model.IncomeDto
import com.spendora.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeFormSheet(
    incomeToEdit: IncomeDto? = null,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, amount: Double, incomeDate: String, source: String, notes: String?) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    val standardSources = listOf("Salary", "Freelance", "Investment", "Bonus", "Gift", "Rental", "Refund", "Other")

    var title by remember { mutableStateOf(incomeToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(incomeToEdit?.amount?.toString() ?: "") }
    var incomeDate by remember { mutableStateOf(incomeToEdit?.incomeDate ?: todayDate) }
    var selectedSource by remember { mutableStateOf(incomeToEdit?.source ?: "Salary") }
    var notes by remember { mutableStateOf(incomeToEdit?.notes ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

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
                text = if (incomeToEdit == null) "Add Income" else "Edit Income",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            SpendoraTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = "Title / Description",
                placeholder = "e.g. Monthly Salary, Freelance project",
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
                value = incomeDate,
                onValueChange = { incomeDate = it },
                label = "Date (YYYY-MM-DD)",
                placeholder = todayDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Income Source",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(standardSources) { source ->
                    val isSelected = selectedSource == source
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSource = source },
                        label = {
                            Text(
                                text = source,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            containerColor = SurfaceElevated
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EmeraldSuccessLight else BorderDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SpendoraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (Optional)",
                placeholder = "Add income remarks...",
                singleLine = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (incomeToEdit == null) "Save Income" else "Update Income",
                gradientBrush = EmeraldGradient,
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
                            incomeToEdit?.id,
                            title,
                            amountVal,
                            incomeDate.trim(),
                            selectedSource,
                            notes.ifBlank { null }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
