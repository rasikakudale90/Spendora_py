package com.spendora.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.components.SpendoraLogo
import com.spendora.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation Phase: 0 = Wordmark in, 1 = Wordmark collapsing into S, 2 = S Emblem glowing
    var animationStage by remember { mutableIntStateOf(0) }

    // Glow Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Stage 0 -> 1: Wordmark to S Transition Trigger
    LaunchedEffect(Unit) {
        delay(400)
        animationStage = 1 // Start morphing 'Spendora' into 'S'
        delay(700)
        animationStage = 2 // S Emblem full bloom
        delay(600)
        onSplashFinished() // Navigate to main app
    }

    // Animated S Emblem Scale & Alpha
    val logoScale by animateFloatAsState(
        targetValue = when (animationStage) {
            0 -> 0.2f
            1 -> 0.7f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 1) 1f else 0f,
        animationSpec = tween(400),
        label = "logoAlpha"
    )

    // Animated 'pendora' letters collapse
    val textAlpha by animateFloatAsState(
        targetValue = if (animationStage == 0) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )

    val textWidthFactor by animateFloatAsState(
        targetValue = if (animationStage == 0) 1f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "textWidthFactor"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (animationStage == 0) 1f else 0f,
        animationSpec = tween(300),
        label = "subtitleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Background Aura Glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryIndigo.copy(alpha = 0.25f),
                            EmeraldSuccess.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Morphing Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(110.dp)
            ) {
                // S Emblem (Fades and springs up as wordmark collapses)
                if (animationStage >= 1) {
                    Box(
                        modifier = Modifier
                            .scale(logoScale)
                            .alpha(logoAlpha)
                    ) {
                        SpendoraLogo(size = 96.dp)
                    }
                }

                // Initial Wordmark "Spendora" (S + pendora)
                if (animationStage < 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.alpha(if (animationStage == 0) 1f else textAlpha)
                    ) {
                        // Glowing Gradient 'S'
                        Text(
                            text = "S",
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(CyanInfoLight, PrimaryIndigoLight, EmeraldSuccessLight)
                                ),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black
                            )
                        )

                        // Collapsing 'pendora'
                        if (textWidthFactor > 0.05f) {
                            Text(
                                text = "pendora",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimary,
                                modifier = Modifier
                                    .alpha(textAlpha)
                                    .scale(scaleX = textWidthFactor, scaleY = 1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Brand Tagline
            Text(
                text = "SMART AI FINANCIAL INTELLIGENCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TextSecondary,
                modifier = Modifier.alpha(subtitleAlpha)
            )
        }
    }
}
