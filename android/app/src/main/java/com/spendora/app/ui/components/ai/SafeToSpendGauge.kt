package com.spendora.app.ui.components.ai

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.SafeToSpendResponse
import com.spendora.app.ui.components.formatInr
import com.spendora.app.ui.theme.*

@Composable
fun SafeToSpendGauge(
    safeToSpend: SafeToSpendResponse?,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (safeToSpend == null) return

    val pace = (safeToSpend.burnPacePercentage / 100.0).coerceIn(0.0, 1.5).toFloat()
    val animatedPace by animateFloatAsState(
        targetValue = (pace / 1.5f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "speedometerArc"
    )

    val statusColor = when (safeToSpend.burnRateStatus) {
        "danger" -> RoseDanger
        "warning" -> AmberWarning
        else -> EmeraldSuccess
    }

    val statusLightColor = when (safeToSpend.burnRateStatus) {
        "danger" -> RoseDangerLight
        "warning" -> AmberWarningLight
        else -> EmeraldSuccessLight
    }

    val statusBg = when (safeToSpend.burnRateStatus) {
        "danger" -> RoseBg
        "warning" -> AmberBg
        else -> EmeraldBg
    }

    val statusTitle = when (safeToSpend.burnRateStatus) {
        "danger" -> "High Burn Danger"
        "warning" -> "Moderate Pace"
        else -> "Optimal Safe Pace"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .background(CardSurfaceGradient)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBg)
                            .border(1.dp, statusColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = statusLightColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Safe-to-Spend",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${safeToSpend.daysRemainingInMonth} days left in month",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusLightColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Speedometer Gauge Arc (Responsive Aspect Ratio Canvas)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val gaugeWidth = maxWidth.coerceAtMost(260.dp)
                val trackColor = SpendoraTheme.colors.surfaceElevated
                val emeraldColor = SpendoraTheme.colors.emerald

                Box(
                    modifier = Modifier.size(width = gaugeWidth, height = 130.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Track Arc (180 degrees from 180 to 360)
                        drawArc(
                            color = trackColor,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active Progress Arc
                        val sweepAngle = 180f * animatedPace
                        if (sweepAngle > 0f) {
                            drawArc(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(emeraldColor, statusColor)
                                ),
                                startAngle = 180f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = formatInr(safeToSpend.dailySafeSpend),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Daily Safe Burn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics row: Burn Rate vs Projected Balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Current Burn Pace", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatInr(safeToSpend.currentBurnRatePerDay)} / day",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (safeToSpend.burnRateStatus == "danger") RoseDangerLight else TextPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "Projected Balance", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInr(safeToSpend.projectedMonthEndBalance),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (safeToSpend.projectedMonthEndBalance >= 0) EmeraldSuccessLight else RoseDangerLight
                    )
                }
            }

            if (safeToSpend.aiRecommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = safeToSpend.aiRecommendation,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
