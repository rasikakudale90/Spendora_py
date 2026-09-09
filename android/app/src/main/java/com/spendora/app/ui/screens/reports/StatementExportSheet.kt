package com.spendora.app.ui.screens.reports

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.components.formatInr
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementExportSheet(
    viewModel: ReportViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val statement by viewModel.statement.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val errorMessage by viewModel.message.collectAsState()

    var selectedMonthIndex by remember { mutableIntStateOf(0) }
    val monthOptions = remember {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displaySdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        (0..5).map { offset ->
            val tempCal = cal.clone() as Calendar
            tempCal.add(Calendar.MONTH, -offset)
            Pair(sdf.format(tempCal.time), displaySdf.format(tempCal.time))
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SpendoraTheme.colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SpendoraTheme.colors.border)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINANCIAL STATEMENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        fontSize = 11.sp,
                        color = CyanInfoLight
                    )
                    Text(
                        text = "Monthly Audit & Export",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 20.sp,
                        color = SpendoraTheme.colors.textPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpendoraTheme.colors.surfaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SpendoraTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                monthOptions.take(3).forEachIndexed { index, (value, display) ->
                    val isSelected = selectedMonthIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyanInfoLight.copy(alpha = 0.15f) else SpendoraTheme.colors.surfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyanInfoLight else SpendoraTheme.colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedMonthIndex = index
                                viewModel.loadStatement(value)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = HankenGroteskFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            fontSize = 12.sp,
                            color = if (isSelected) CyanInfoLight else SpendoraTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanInfoLight, modifier = Modifier.size(32.dp))
                }
            } else if (statement != null) {
                val st = statement!!
                // Statement Summary Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SpendoraTheme.colors.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpendoraTheme.colors.border))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET CASH FLOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 11.sp,
                                color = SpendoraTheme.colors.textMuted
                            )
                            val rateColor = if (st.savingsRatePct >= 20.0) EmeraldSuccessLight else if (st.savingsRatePct >= 0) AmberWarningLight else RoseDangerLight
                            Text(
                                text = "${if (st.savingsRatePct >= 0) "+" else ""}${st.savingsRatePct}% SAVINGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                fontSize = 11.sp,
                                color = rateColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val netColor = if (st.netCashFlow >= 0) EmeraldSuccessLight else RoseDangerLight
                        Text(
                            text = "${if (st.netCashFlow >= 0) "+" else ""}${formatInr(st.netCashFlow)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            fontSize = 24.sp,
                            color = netColor
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = SpendoraTheme.colors.border, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOTAL INCOME (${st.incomeCount})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    fontSize = 10.sp,
                                    color = SpendoraTheme.colors.textMuted
                                )
                                Text(
                                    text = formatInr(st.totalIncome),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontSize = 14.sp,
                                    color = EmeraldSuccessLight
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOTAL SPENT (${st.expenseCount})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    fontSize = 10.sp,
                                    color = SpendoraTheme.colors.textMuted
                                )
                                Text(
                                    text = formatInr(st.totalSpent),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontSize = 14.sp,
                                    color = RoseDangerLight
                                )
                            }
                        }
                    }
                }

                if (st.categoryAllocations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "TOP SPENDING ALLOCATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        fontSize = 11.sp,
                        color = SpendoraTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    st.categoryAllocations.take(3).forEach { alloc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = alloc.category,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = HankenGroteskFamily),
                                fontSize = 13.sp,
                                color = SpendoraTheme.colors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${alloc.percentage}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    fontSize = 11.sp,
                                    color = SpendoraTheme.colors.textMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatInr(alloc.amount),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    fontSize = 13.sp,
                                    color = SpendoraTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.exportExpenses(context) { file ->
                            viewModel.shareFile(context, file)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanInfoLight,
                        contentColor = BackgroundDark
                    ),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BackgroundDark)
                    } else {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Expense CSV",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.exportIncomes(context) { file ->
                            viewModel.shareFile(context, file)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpendoraTheme.colors.surfaceCard,
                        contentColor = SpendoraTheme.colors.textPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SpendoraTheme.colors.border)),
                    enabled = !isExporting
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp), tint = EmeraldSuccessLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Income CSV",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
