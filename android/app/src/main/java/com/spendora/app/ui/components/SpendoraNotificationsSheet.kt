package com.spendora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.DashboardSummary
import com.spendora.app.data.model.LeakAnalysisResponse
import com.spendora.app.data.model.SafeToSpendResponse
import com.spendora.app.ui.theme.*

data class SpendoraNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val icon: ImageVector
)

enum class NotificationType {
    ALERT,
    WARNING,
    INFO,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendoraNotificationsSheet(
    summary: DashboardSummary,
    safeToSpend: SafeToSpendResponse?,
    leakAnalysis: LeakAnalysisResponse?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Build real live notification alerts from financial telemetry
    val notifications = buildList {
        // 1. Safe to spend pace notification
        if (safeToSpend != null) {
            when {
                safeToSpend.burnPacePercentage > 105.0 -> {
                    add(
                        SpendoraNotificationItem(
                            id = "burn_danger",
                            title = "High Burn Rate Warning",
                            message = "Your spending velocity is ${"%.0f".format(safeToSpend.burnPacePercentage)}% of target pace. Daily safe limit is ${formatInr(safeToSpend.dailySafeSpend)}.",
                            timestamp = "Active Alert",
                            type = NotificationType.ALERT,
                            icon = Icons.Default.Warning
                        )
                    )
                }
                safeToSpend.burnPacePercentage in 85.0..105.0 -> {
                    add(
                        SpendoraNotificationItem(
                            id = "burn_warning",
                            title = "Moderate Burn Pace",
                            message = "You are spending close to the safe threshold (${"%.0f".format(safeToSpend.burnPacePercentage)}%). Remaining safe buffer: ${formatInr(safeToSpend.remainingBuffer)}.",
                            timestamp = "Active Monitor",
                            type = NotificationType.WARNING,
                            icon = Icons.Default.TrendingUp
                        )
                    )
                }
                else -> {
                    add(
                        SpendoraNotificationItem(
                            id = "burn_optimal",
                            title = "Safe Burn Velocity",
                            message = "Optimal burn velocity (${"%.0f".format(safeToSpend.burnPacePercentage)}%). You are on track to save ${formatInr(safeToSpend.projectedMonthEndBalance)} this month.",
                            timestamp = "Optimal",
                            type = NotificationType.SUCCESS,
                            icon = Icons.Default.CheckCircle
                        )
                    )
                }
            }
        }

        // 2. Leak Hunter subscriptions notification
        if (leakAnalysis != null && leakAnalysis.detectedSubscriptions.isNotEmpty()) {
            add(
                SpendoraNotificationItem(
                    id = "leaks_detected",
                    title = "${leakAnalysis.detectedSubscriptions.size} Subscriptions Active",
                    message = "Monthly subscription drain: ${formatInr(leakAnalysis.totalMonthlyLeak)}. Tap Leak Hunter on dashboard to review.",
                    timestamp = "Subscription Audit",
                    type = NotificationType.INFO,
                    icon = Icons.Default.Search
                )
            )
        }

        // 3. Cash Flow Summary
        val netCashFlow = summary.totalIncome - summary.totalSpent
        if (summary.totalIncome > 0) {
            if (netCashFlow >= 0) {
                add(
                    SpendoraNotificationItem(
                        id = "positive_cashflow",
                        title = "Positive Cash Flow",
                        message = "Net surplus of +${formatInr(netCashFlow)} (${"%.1f".format(summary.savingsRate)}% savings rate) across ${summary.expenseCount} transactions.",
                        timestamp = "Monthly Status",
                        type = NotificationType.SUCCESS,
                        icon = Icons.Default.AccountBalance
                    )
                )
            } else {
                add(
                    SpendoraNotificationItem(
                        id = "negative_cashflow",
                        title = "Deficit Spending Alert",
                        message = "Monthly expenses exceed income by ${formatInr(-netCashFlow)}. Consider adjusting discretionary categories.",
                        timestamp = "Deficit Alert",
                        type = NotificationType.ALERT,
                        icon = Icons.Default.TrendingDown
                    )
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderDark)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PrimaryCyanLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Notifications & Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${notifications.size} live telemetry notices",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All Clear!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "No critical alerts or budget breaches at this time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        val (bgColor, borderColor, accentColor) = when (item.type) {
                            NotificationType.ALERT -> Triple(RoseBg, RoseDanger.copy(alpha = 0.4f), RoseDanger)
                            NotificationType.WARNING -> Triple(AmberBg, AmberWarning.copy(alpha = 0.4f), AmberWarningLight)
                            NotificationType.SUCCESS -> Triple(EmeraldBg, EmeraldSuccess.copy(alpha = 0.4f), EmeraldSuccessLight)
                            NotificationType.INFO -> Triple(SurfaceElevated, PrimaryCyan.copy(alpha = 0.35f), PrimaryCyanLight)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = item.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accentColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
