package com.spendora.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            // Header: Close / Dismiss, Subheader & Currency Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (expenseToEdit == null) "New Expense" else "Edit Expense",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Text(
                        text = "INR (₹)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyanLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Bold Amount Display Pod
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TRANSACTION VELOCITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = PrimaryCyanLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "-2.4% vs Daily Avg",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldSuccessLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            style = TelemetryMetricTextStyle.copy(fontSize = 28.sp),
                            color = PrimaryCyanLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = amountText,
                            onValueChange = { amountText = it; amountError = null },
                            textStyle = TelemetryMetricTextStyle.copy(
                                fontSize = 36.sp,
                                color = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (amountText.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = TelemetryMetricTextStyle.copy(
                                            fontSize = 36.sp,
                                            color = TextMuted
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = QuantumVioletLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Instant Ledger Sync Activated",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !amountError.isNullOrBlank()) {
                Text(
                    text = amountError ?: "",
                    color = RoseDanger,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note & Merchant Input Card with Storefront Icon
            SpendoraTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = "Title / Merchant",
                placeholder = "What did you spend on?",
                leadingIcon = Icons.Default.Storefront,
                errorMessage = titleError
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Autocomplete Chips
            val quickMerchants = listOf("🚕 Uber", "☕ Starbucks", "☁️ AWS Cloud", "🥑 Groceries", "🍕 Swiggy")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "QUICK:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
                items(quickMerchants) { merchant ->
                    val cleanName = merchant.substringAfter(" ")
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.clickable {
                            title = cleanName
                            titleError = null
                        }
                    ) {
                        Text(
                            text = merchant,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Vector Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY VECTOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextMuted
                )
                if (categories.isNotEmpty()) {
                    val activeCat = categories.find { it.id == selectedCategoryId }
                    Text(
                        text = activeCat?.name ?: "Select",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryCyanLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (categories.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryCyanLight,
                    strokeWidth = 2.dp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategoryId == category.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryCyan else BorderDark,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedCategoryId = category.id
                                    categoryError = null
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) PrimaryCyanLight else TextPrimary
                            )
                        }
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

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Source Selector Card
            Text(
                text = "PAYMENT SOURCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PaymentMode.values()) { mode ->
                    val isSelected = selectedPaymentMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryCyan else BorderDark,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMode = mode }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mode.value,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) PrimaryCyanLight else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Notes Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SpendoraTextField(
                        value = expenseDate,
                        onValueChange = { expenseDate = it },
                        label = "Date (YYYY-MM-DD)",
                        placeholder = todayDate
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SpendoraTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (Optional)",
                placeholder = "Add transaction remarks...",
                singleLine = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // High-Impact Neon Action CTA
            Button(
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryCyan,
                    contentColor = OnPrimaryColor
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (expenseToEdit == null) "Save Transaction" else "Update Transaction",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
