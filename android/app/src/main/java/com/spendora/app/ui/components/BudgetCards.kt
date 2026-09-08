package com.spendora.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.BudgetDto
import com.spendora.app.ui.theme.*

@Composable
fun PeriodTabRow(
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        "daily" to "Daily",
        "weekly" to "Weekly",
        "monthly" to "Monthly",
        "yearly" to "Yearly"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        periods.forEach { (key, label) ->
            val isSelected = selectedPeriod == key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryIndigo else Color.Transparent)
                    .clickable { onSelectPeriod(key) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextMuted
                )
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: BudgetDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverall = budget.scope == "overall"
    val title = if (isOverall) "Overall Budget" else (budget.categoryName ?: "Category Budget")
    val pct = budget.percentageUsed.toFloat().coerceIn(0f, 100f)
    val animatedProgress by animateFloatAsState(targetValue = pct / 100f, label = "budgetProgress")

    val statusColor = when (budget.status) {
        "over_budget" -> RoseDanger
        "near_limit" -> AmberWarning
        else -> EmeraldSuccess
    }

    val statusLabel = when (budget.status) {
        "over_budget" -> "Over Budget"
        "near_limit" -> "Near Limit"
        else -> "On Track"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (budget.status == "over_budget") 1.5.dp else 1.dp,
                color = if (budget.status == "over_budget") RoseDanger.copy(alpha = 0.6f) else BorderDark,
                shape = RoundedCornerShape(16.dp)
            ),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOverall) PrimaryIndigo.copy(alpha = 0.15f) else statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = if (isOverall) PrimaryIndigoLight else statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${budget.periodType.replaceFirstChar { it.uppercase() }} Limit",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = RoseDanger.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = BorderDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row: Spent, Remaining, Allocated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Spent", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInr(budget.spent),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (budget.status == "over_budget") RoseDanger else TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Remaining", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInr(budget.remaining),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (budget.remaining > 0) EmeraldSuccess else RoseDanger
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Limit (${budget.percentageUsed.toInt()}%)", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInr(budget.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
