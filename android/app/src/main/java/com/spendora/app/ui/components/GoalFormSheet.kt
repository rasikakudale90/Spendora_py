package com.spendora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
        "purple" to Color(0xFFA855F7),
        "amber" to AmberWarning,
        "rose" to RoseDanger
    )

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
                text = if (isEditing) "Edit Savings Goal" else "Create New Goal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Goal Title
            Text(text = "Goal Title", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                placeholder = { Text("e.g. Dream Car, Emergency Fund", color = TextMuted) },
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
            Spacer(modifier = Modifier.height(14.dp))

            // Target Amount
            Text(text = "Target Amount (₹)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = targetAmountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        targetAmountText = it
                        errorMessage = null
                    }
                },
                placeholder = { Text("e.g. 500000", color = TextMuted) },
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
            Spacer(modifier = Modifier.height(14.dp))

            // Current Saved Amount
            Text(text = "Current Saved (₹)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = currentAmountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        currentAmountText = it
                        errorMessage = null
                    }
                },
                placeholder = { Text("0.00", color = TextMuted) },
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
            Spacer(modifier = Modifier.height(14.dp))

            // Target Deadline Date (YYYY-MM-DD)
            Text(text = "Target Deadline (YYYY-MM-DD, Optional)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = targetDate,
                onValueChange = { targetDate = it },
                placeholder = { Text("2026-12-31", color = TextMuted) },
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
            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips
            Text(text = "Category", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(4).forEach { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryIndigo else SurfaceElevated,
                        modifier = Modifier
                            .clickable { category = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Color Theme Chips
            Text(text = "Accent Color", fontSize = 12.sp, color = TextSecondary)
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
                            .size(32.dp)
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
            Spacer(modifier = Modifier.height(14.dp))

            // Notes
            Text(text = "Notes (Optional)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("e.g. Monthly SIP contribution", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = RoseDanger, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (isEditing) "Update Goal" else "Create Goal",
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
