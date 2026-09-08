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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.FinancialHealthResponse
import com.spendora.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FinancialHealthRadar(
    health: FinancialHealthResponse?,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (health == null) return

    val animatedScore by animateFloatAsState(
        targetValue = health.compositeScore.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "scoreAnim"
    )

    val tierColor = when (health.tier.lowercase()) {
        "elite" -> PrimaryIndigoLight
        "healthy" -> EmeraldSuccess
        "vulnerable" -> AmberWarning
        else -> RoseDanger
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .clickable { onCardClick() },
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Score & Tier Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(tierColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Financial Health Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = health.tierTitle,
                            fontSize = 11.sp,
                            color = tierColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tierColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${health.compositeScore} / 100",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5-Pillar Spider Radar Chart Canvas
            val pillars = health.pillars
            if (pillars.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f
                        val numSides = pillars.size
                        val angleStep = (2.0 * Math.PI / numSides).toFloat()

                        // 1. Draw 4 Concentric Radar Polygons (25%, 50%, 75%, 100%)
                        for (level in 1..4) {
                            val levelRadius = radius * (level / 4f)
                            val gridPath = Path()
                            for (i in 0 until numSides) {
                                val angle = (i * angleStep) - (Math.PI / 2.0).toFloat()
                                val x = center.x + levelRadius * cos(angle)
                                val y = center.y + levelRadius * sin(angle)
                                if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                            }
                            gridPath.close()
                            drawPath(gridPath, color = BorderDark, style = Stroke(width = 1.dp.toPx()))
                        }

                        // 2. Draw Axis Spokes
                        for (i in 0 until numSides) {
                            val angle = (i * angleStep) - (Math.PI / 2.0).toFloat()
                            val x = center.x + radius * cos(angle)
                            val y = center.y + radius * sin(angle)
                            drawLine(
                                color = BorderDark,
                                start = center,
                                end = Offset(x, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // 3. Draw User's Data Polygon
                        val dataPath = Path()
                        for (i in 0 until numSides) {
                            val pScore = (pillars[i].score / 100f).coerceIn(0.1, 1.0).toFloat()
                            val pRadius = radius * pScore
                            val angle = (i * angleStep) - (Math.PI / 2.0).toFloat()
                            val x = center.x + pRadius * cos(angle)
                            val y = center.y + pRadius * sin(angle)
                            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                        }
                        dataPath.close()

                        // Fill translucent polygon
                        drawPath(dataPath, color = PrimaryIndigo.copy(alpha = 0.35f))
                        // Stroke border
                        drawPath(dataPath, color = PrimaryIndigoLight, style = Stroke(width = 2.dp.toPx()))

                        // 4. Draw Vertex Dots
                        for (i in 0 until numSides) {
                            val pScore = (pillars[i].score / 100f).coerceIn(0.1, 1.0).toFloat()
                            val pRadius = radius * pScore
                            val angle = (i * angleStep) - (Math.PI / 2.0).toFloat()
                            val x = center.x + pRadius * cos(angle)
                            val y = center.y + pRadius * sin(angle)
                            drawCircle(color = TextPrimary, radius = 3.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pillar Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                pillars.take(3).forEach { p ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${p.score.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (p.status) {
                                "optimal" -> EmeraldSuccess
                                "good" -> PrimaryIndigoLight
                                "fair" -> AmberWarning
                                else -> RoseDanger
                            }
                        )
                        Text(
                            text = p.name.split(" ").firstOrNull() ?: p.name,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Top Score Booster Tip
            val topBooster = health.scoreBoosters.firstOrNull()
            if (topBooster != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Score Booster (+${topBooster.impactPoints} pts)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarningLight
                            )
                            Text(
                                text = topBooster.actionText,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
