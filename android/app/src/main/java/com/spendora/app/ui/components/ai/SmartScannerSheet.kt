package com.spendora.app.ui.components.ai

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.PaymentMode
import com.spendora.app.data.model.TransactionExtractionResponse
import com.spendora.app.ui.components.SpendoraButton
import com.spendora.app.ui.components.formatInr
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel
import com.spendora.app.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartScannerSheet(
    aiViewModel: AiViewModel,
    expenseViewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState by aiViewModel.uiState.collectAsState()
    var rawText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sampleSms = listOf(
        "HDFC UPI Debit" to "Sent Rs.450.00 from HDFC Bank A/C **1234 to SWIGGY on 08-09-2026 via UPI Ref 62391039.",
        "SBI Card Alert" to "Your SBI Card ending 4012 was charged INR 1,890.00 at ZOMATO on 07/09/2026.",
        "Amazon Order" to "Paid Rs.2,499.00 on AMAZON PAY for wireless earbuds on 06-09-2026."
    )

    ModalBottomSheet(
        onDismissRequest = {
            aiViewModel.clearExtractionResult()
            onDismiss()
        },
        containerColor = SurfaceDark,
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = PrimaryIndigoLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Transaction Scanner",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Text(
                text = "Instant SMS / Bank alert parser with automated PII masking",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Samples
            Text(text = "Quick Test Samples", fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleSms) { (label, smsText) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceElevated,
                        modifier = Modifier.clickable {
                            rawText = smsText
                            aiViewModel.extractTransaction(text = smsText)
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input TextField
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Paste SMS / Alert Notification", fontSize = 12.sp, color = TextSecondary)
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            rawText = clipData.getItemAt(0).text.toString()
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryIndigoLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paste Clipboard", fontSize = 11.sp, color = PrimaryIndigoLight)
                }
            }

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it; errorMessage = null },
                placeholder = { Text("Paste your Indian bank SMS or UPI debit alert here...", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
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

            Spacer(modifier = Modifier.height(14.dp))

            SpendoraButton(
                text = if (uiState.isScanning) "Parsing Alert..." else "Parse Transaction",
                onClick = {
                    if (rawText.isBlank()) {
                        errorMessage = "Please enter or paste transaction text"
                        return@SpendoraButton
                    }
                    aiViewModel.extractTransaction(text = rawText)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Extracted Result Card
            val result = uiState.extractionResult
            if (result != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "EXTRACTED TRANSACTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigoLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = formatInr(result.amount),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (result.type == "expense") RoseDanger else EmeraldSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = PrimaryIndigo.copy(alpha = 0.2f)) {
                                Text(
                                    text = result.categoryName ?: "General",
                                    fontSize = 11.sp,
                                    color = PrimaryIndigoLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• ${result.paymentMode}", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "• ${result.transactionDate}", fontSize = 11.sp, color = TextMuted)
                        }

                        if (result.isPotentialDuplicate) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = result.duplicateWarning ?: "Possible duplicate transaction detected",
                                    fontSize = 11.sp,
                                    color = AmberWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val categories = expenseViewModel.uiState.value.categories
                                val categoryId = categories.find { it.name.equals(result.categoryName, ignoreCase = true) }?.id
                                    ?: categories.firstOrNull()?.id ?: ""

                                val paymentMode = when (result.paymentMode) {
                                    "Card" -> PaymentMode.CARD
                                    "Cash" -> PaymentMode.CASH
                                    "Net Banking" -> PaymentMode.NET_BANKING
                                    "Other" -> PaymentMode.OTHER
                                    else -> PaymentMode.UPI
                                }

                                expenseViewModel.saveExpense(
                                    title = result.title,
                                    amount = result.amount,
                                    expenseDate = result.transactionDate,
                                    categoryId = categoryId,
                                    paymentMode = paymentMode,
                                    notes = "Auto-extracted via SMS Parser",
                                    onSuccess = {
                                        aiViewModel.clearExtractionResult()
                                        onDismiss()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save to Spendora", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
