package com.spendora.app.ui.components

import androidx.compose.foundation.background
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) "Edit Budget Limit" else "Set New Budget",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditing) {
                // Scope selector: Overall vs Category
                Text(text = "Budget Scope", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (scope == "overall") PrimaryIndigo else SurfaceElevated)
                            .clickable { scope = "overall" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Overall",
                            fontWeight = if (scope == "overall") FontWeight.Bold else FontWeight.Normal,
                            color = if (scope == "overall") TextPrimary else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (scope == "category") PrimaryIndigo else SurfaceElevated)
                            .clickable { scope = "category" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Category",
                            fontWeight = if (scope == "category") FontWeight.Bold else FontWeight.Normal,
                            color = if (scope == "category") TextPrimary else TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // If Category Scope, Show Category Dropdown
                if (scope == "category") {
                    Text(text = "Category", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val selectedCategory = categories.find { it.id.toString() == selectedCategoryId }
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
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(text = cat.name, color = TextPrimary) },
                                    onClick = {
                                        selectedCategoryId = cat.id.toString()
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Period Type selector
                Text(text = "Period", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                PeriodTabRow(
                    selectedPeriod = periodType,
                    onSelectPeriod = { periodType = it }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Budget Amount Input
            Text(text = "Allocated Amount (₹)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = it
                        errorMessage = null
                    }
                },
                placeholder = { Text("e.g. 15000", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated,
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = errorMessage!!, color = RoseDanger, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (isEditing) "Save Changes" else "Set Budget",
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
