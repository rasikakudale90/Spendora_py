package com.spendora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.BudgetDto
import com.spendora.app.data.model.CategoryDto
import com.spendora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormSheet(
    budget: BudgetDto? = null,
    categories: List<CategoryDto>,
    initialPeriod: String = "monthly",
    onDismiss: () -> Unit,
    onSave: (scope: String, categoryId: String?, amount: Double, periodType: String) -> Unit
) {
    val isEditing = budget != null
    var scope by remember { mutableStateOf(budget?.scope ?: "overall") } // "overall" or "category"
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }
    var amountText by remember { mutableStateOf(budget?.amount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var periodType by remember { mutableStateOf(budget?.periodType ?: initialPeriod) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark,
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDark) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = if (isEditing) "Edit Budget Limit" else "Set New Budget",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditing) {
                // Scope selector: Overall vs Category
                Text(text = "Budget Scope", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scope == "overall") PrimaryIndigo else SurfaceElevated)
                            .border(1.dp, if (scope == "overall") PrimaryIndigoLight else BorderDark, RoundedCornerShape(10.dp))
                            .clickable { scope = "overall" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Overall",
                            fontWeight = if (scope == "overall") FontWeight.Bold else FontWeight.Medium,
                            color = if (scope == "overall") TextPrimary else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (scope == "category") PrimaryIndigo else SurfaceElevated)
                            .border(1.dp, if (scope == "category") PrimaryIndigoLight else BorderDark, RoundedCornerShape(10.dp))
                            .clickable { scope = "category" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Category",
                            fontWeight = if (scope == "category") FontWeight.Bold else FontWeight.Medium,
                            color = if (scope == "category") TextPrimary else TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // If Category Scope, Show Category Dropdown
                if (scope == "category") {
                    Text(text = "Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Select a Category",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = PrimaryIndigoLight,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceDark).border(1.dp, BorderDark)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(text = cat.name, color = TextPrimary, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Period Type selector
                Text(text = "Period", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                PeriodTabRow(
                    selectedPeriod = periodType,
                    onSelectPeriod = { periodType = it }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Budget Amount Input
            SpendoraTextField(
                value = amountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = it
                        errorMessage = null
                    }
                },
                label = "Allocated Amount (₹)",
                placeholder = "e.g. 15000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                errorMessage = errorMessage
            )

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (isEditing) "Save Changes" else "Set Budget",
                gradientBrush = PrimaryGradient,
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@SpendoraButton
                    }
                    if (scope == "category" && selectedCategoryId == null && !isEditing) {
                        errorMessage = "Please select a category"
                        return@SpendoraButton
                    }
                    onSave(scope, selectedCategoryId, amt, periodType)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
