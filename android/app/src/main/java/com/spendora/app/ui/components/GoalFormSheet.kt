package com.spendora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.spendora.app.data.model.GoalDto
import com.spendora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormSheet(
    goal: GoalDto? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, targetAmount: Double, currentAmount: Double, targetDate: String?, category: String, color: String, notes: String?) -> Unit
) {
    val isEditing = goal != null
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmountText by remember { mutableStateOf(goal?.targetAmount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var currentAmountText by remember { mutableStateOf(goal?.currentAmount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var targetDate by remember { mutableStateOf(goal?.targetDate ?: "") }
    var category by remember { mutableStateOf(goal?.category ?: "Savings") }
    var color by remember { mutableStateOf(goal?.color ?: "emerald") }
    var notes by remember { mutableStateOf(goal?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Savings", "Emergency Fund", "Vehicle", "Vacation", "Real Estate", "Gadgets", "Investment", "Education")
    val colors = listOf(
        "emerald" to EmeraldSuccess,
        "indigo" to PrimaryIndigoLight,
        "blue" to Color(0xFF38BDF8),
        "purple" to SecondaryVioletLight,
        "amber" to AmberWarning,
        "rose" to RoseDanger
    )

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
                text = if (isEditing) "Edit Savings Goal" else "Create New Goal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Goal Title
            SpendoraTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = "Goal Title",
                placeholder = "e.g. Dream Car, Emergency Fund",
                errorMessage = null
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Target Amount
            SpendoraTextField(
                value = targetAmountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        targetAmountText = it
                        errorMessage = null
                    }
                },
                label = "Target Amount (₹)",
                placeholder = "e.g. 500000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Current Saved Amount
            SpendoraTextField(
                value = currentAmountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        currentAmountText = it
                        errorMessage = null
                    }
                },
                label = "Current Saved (₹)",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Target Deadline Date (YYYY-MM-DD)
            SpendoraTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                label = "Target Deadline (YYYY-MM-DD, Optional)",
                placeholder = "2026-12-31"
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text(text = "Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = {
                            Text(
                                text = cat,
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

            // Color Theme Chips
            Text(text = "Accent Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { (colorName, colorVal) ->
                    val isSelected = color == colorName
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colorVal)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) TextPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { color = colorName }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            SpendoraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (Optional)",
                placeholder = "e.g. Monthly SIP contribution",
                singleLine = false
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = RoseDanger, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (isEditing) "Update Goal" else "Create Goal",
                gradientBrush = PrimaryGradient,
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter a goal title"
                        return@SpendoraButton
                    }
                    val tgt = targetAmountText.toDoubleOrNull()
                    if (tgt == null || tgt <= 0) {
                        errorMessage = "Please enter a valid target amount greater than 0"
                        return@SpendoraButton
                    }
                    val cur = currentAmountText.toDoubleOrNull() ?: 0.0
                    onSave(
                        name.trim(),
                        tgt,
                        cur,
                        targetDate.trim().ifBlank { null },
                        category,
                        color,
                        notes.trim().ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
