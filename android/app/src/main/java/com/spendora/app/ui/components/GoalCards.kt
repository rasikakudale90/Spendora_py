package com.spendora.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.spendora.app.data.model.GoalDto
import com.spendora.app.ui.theme.*

@Composable
fun GoalCard(
    goal: GoalDto,
    onContribute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = goal.progressPercentage.toFloat().coerceIn(0f, 100f)
    val animatedProgress by animateFloatAsState(targetValue = pct / 100f, label = "goalProgress")

    val accentColor = when (goal.color.lowercase()) {
        "indigo" -> PrimaryIndigoLight
        "blue" -> Color(0xFF38BDF8)
        "purple" -> Color(0xFFA855F7)
        "amber" -> AmberWarning
        "rose" -> RoseDanger
        else -> EmeraldSuccess
    }

    val pacing = goal.runway?.pacingStatus
    val pacingColor = when (pacing) {
        "ahead", "on_track" -> EmeraldSuccess
        "at_risk" -> AmberWarning
        "behind" -> RoseDanger
        "completed" -> EmeraldSuccess
        else -> PrimaryIndigoLight
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp)),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title, Category & Action buttons
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceElevated
                            ) {
                                Text(
                                    text = goal.category,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (goal.targetDate != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Due ${goal.targetDate}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saved ${formatInr(goal.currentAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Target ${formatInr(goal.targetAmount)} (${goal.progressPercentage.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = BorderDark
                )
            }

            // AI Runway & Pacing Alert (if available)
            if (goal.runway != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
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
                                        .background(pacingColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = goal.runway.pacingStatus.replace("_", " ").uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pacingColor
                                )
                            }
                            if (goal.runway.requiredMonthlySavings > 0) {
                                Text(
                                    text = "Req: ${formatInr(goal.runway.requiredMonthlySavings)}/mo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.runway.pacingMessage,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        if (!goal.runway.speedupSuggestion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(14.dp).padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = goal.runway.speedupSuggestion,
                                    fontSize = 11.sp,
                                    color = AmberWarningLight,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Quick Deposit/Withdraw Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.remainingAmount > 0) "${formatInr(goal.remainingAmount)} to go" else "Goal Completed! 🎉",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (goal.remainingAmount > 0) TextMuted else EmeraldSuccess
                )

                Button(
                    onClick = onContribute,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Deposit / Withdraw", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
