package com.spendora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.GoalDto
import com.spendora.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalContributeSheet(
    goal: GoalDto,
    onDismiss: () -> Unit,
    onContribute: (amount: Double, action: String, notes: String?) -> Unit
) {
    var action by remember { mutableStateOf("deposit") } // "deposit" or "withdraw"
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val newSavedAmount = if (action == "deposit") {
        goal.currentAmount + amount
    } else {
        (goal.currentAmount - amount).coerceAtLeast(0.0)
    }

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
                text = "Update Goal Contribution",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = goal.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryIndigoLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Action Toggle: Deposit vs Withdraw
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (action == "deposit") EmeraldSuccess else SurfaceElevated)
                        .border(1.dp, if (action == "deposit") EmeraldSuccessLight else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { action = "deposit"; errorMessage = null }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Deposit Funds",
                        fontWeight = if (action == "deposit") FontWeight.Bold else FontWeight.Medium,
                        color = if (action == "deposit") TextPrimary else TextMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (action == "withdraw") RoseDanger else SurfaceElevated)
                        .border(1.dp, if (action == "withdraw") RoseDangerLight else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { action = "withdraw"; errorMessage = null }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "- Withdraw Funds",
                        fontWeight = if (action == "withdraw") FontWeight.Bold else FontWeight.Medium,
                        color = if (action == "withdraw") TextPrimary else TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            SpendoraTextField(
                value = amountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = it
                        errorMessage = null
                    }
                },
                label = if (action == "deposit") "Deposit Amount (₹)" else "Withdrawal Amount (₹)",
                placeholder = "e.g. 5000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Saved", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = formatInr(goal.currentAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "➔",
                        fontSize = 18.sp,
                        color = TextMuted
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "New Saved Total", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = formatInr(newSavedAmount),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (action == "deposit") EmeraldSuccessLight else RoseDangerLight
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Notes / Reason
            SpendoraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Note / Reason (Optional)",
                placeholder = "e.g. Monthly salary savings"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = RoseDanger, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SpendoraButton(
                text = if (action == "deposit") "Confirm Deposit" else "Confirm Withdrawal",
                gradientBrush = if (action == "deposit") EmeraldGradient else RoseGradient,
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@SpendoraButton
                    }
                    if (action == "withdraw" && amt > goal.currentAmount) {
                        errorMessage = "Cannot withdraw more than current saved amount (${formatInr(goal.currentAmount)})"
                        return@SpendoraButton
                    }
                    onContribute(amt, action, notes.trim().ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
