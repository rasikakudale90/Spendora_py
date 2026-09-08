package com.spendora.app.ui.components.ai

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.spendora.app.data.model.CategoryDto
import com.spendora.app.data.model.PurchaseSimulationResponse
import com.spendora.app.ui.components.SpendoraButton
import com.spendora.app.ui.components.formatInr
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseSimulatorSheet(
    aiViewModel: AiViewModel,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onAddAsExpense: (title: String, amount: Double) -> Unit
) {
    val uiState by aiViewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sampleChips = listOf(
        "iPhone 16" to 79900.0,
        "Goa Weekend Trip" to 15000.0,
        "Sony Headphones" to 24990.0,
        "Annual Gym Sub" to 18000.0,
        "Smart Watch" to 8500.0
    )

    ModalBottomSheet(
        onDismissRequest = {
            aiViewModel.clearSimulationResult()
            onDismiss()
        },
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryIndigoLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Can I Afford This?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Text(
                text = "Simulate how a potential purchase impacts your runway and burn pace",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Samples
            Text(text = "Quick Sample Tests", fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleChips) { (sampleTitle, sampleAmt) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceElevated,
                        modifier = Modifier.clickable {
                            title = sampleTitle
                            amountText = sampleAmt.toInt().toString()
                            aiViewModel.simulatePurchase(sampleTitle, sampleAmt)
                        }
                    ) {
                        Text(
                            text = "$sampleTitle (${formatInr(sampleAmt)})",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            Text(text = "Item Name", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; errorMessage = null },
                placeholder = { Text("e.g. Mechanical Keyboard", color = TextMuted) },
                singleLine = true,
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Estimated Price (₹)", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = it
                        errorMessage = null
                    }
                },
                placeholder = { Text("e.g. 12000", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
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
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = errorMessage!!, color = RoseDanger, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SpendoraButton(
                text = if (uiState.isSimulating) "Analyzing Trajectory..." else "Run Affordability Simulation",
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (title.isBlank() || amt == null || amt <= 0) {
                        errorMessage = "Please enter item name and a valid amount"
                        return@SpendoraButton
                    }
                    aiViewModel.simulatePurchase(title, amt)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Result Display
            val result = uiState.simulationResult
            if (result != null) {
                Spacer(modifier = Modifier.height(20.dp))

                val verdictColor = when (result.verdict) {
                    "safe" -> EmeraldSuccess
                    "caution" -> AmberWarning
                    else -> RoseDanger
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = verdictColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, verdictColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result.verdict == "safe") Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = verdictColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = result.verdictTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = verdictColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.verdictSummary,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metric Impact Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Daily Safe Burn", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = "${formatInr(result.dailySafeSpendBefore)} ➔ ${formatInr(result.dailySafeSpendAfter)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Projected Cash Flow", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = formatInr(result.projectedCashFlow),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.projectedCashFlow >= 0) EmeraldSuccess else RoseDanger
                                )
                            }
                        }

                        if (result.aiAnalysis.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = result.aiAnalysis,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onAddAsExpense(result.itemTitle, result.itemAmount)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add this as Expense now", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
