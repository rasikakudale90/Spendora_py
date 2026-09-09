package com.spendora.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Spendora Midnight Obsidian & Electric Neon Palette
val BackgroundDark = Color(0xFF070B12)      // Deep Obsidian base
val SurfaceDark = Color(0xFF0F172A)         // Deep Slate base
val SurfaceCard = Color(0xFF131D33)         // Elevated card container
val SurfaceCardEnd = Color(0xFF0B1324)      // Gradient card lower stop
val SurfaceElevated = Color(0xFF1E293B)     // Floating element / chip surface
val BorderDark = Color(0xFF1E2D4A)          // Subtle card perimeter stroke
val BorderGlow = Color(0xFF334A73)          // Active / Focused stroke

// Electric Indigo & Purple Accents
val PrimaryIndigo = Color(0xFF6366F1)       // Electric Indigo
val PrimaryIndigoLight = Color(0xFF818CF8)  // Indigo 400
val SecondaryViolet = Color(0xFF8B5CF6)     // Electric Violet
val SecondaryVioletLight = Color(0xFFA78BFA)// Violet 400

// Financial Health & Metric Indicators
val EmeraldSuccess = Color(0xFF10B981)      // Vivid Neon Emerald
val EmeraldSuccessLight = Color(0xFF34D399) // Mint highlight
val EmeraldBg = Color(0x2210B981)           // Translucent glowing badge background

val RoseDanger = Color(0xFFFF4D6D)          // Vivid Coral Crimson
val RoseDangerLight = Color(0xFFFF758F)     // Coral highlight
val RoseBg = Color(0x22FF4D6D)              // Translucent danger badge background

val AmberWarning = Color(0xFFF59E0B)        // Warm Amber Gold
val AmberWarningLight = Color(0xFFFBBF24)
val AmberBg = Color(0x22F59E0B)

val CyanInfo = Color(0xFF06B6D4)            // Sky / Cyan
val CyanInfoLight = Color(0xFF38BDF8)
val CyanBg = Color(0x2206B6D4)

// High-Contrast Crisp Typography
val TextPrimary = Color(0xFFFFFFFF)         // Pure crisp white for 100% legibility
val TextSecondary = Color(0xFF94A3B8)       // Silver slate for captions & labels
val TextMuted = Color(0xFF64748B)           // Muted slate for placeholders & secondary meta
val TextHighlight = Color(0xFF818CF8)       // Vivid Indigo accent text

// Pre-computed Gradient Brushes for Premium Depth
val PrimaryGradient = Brush.horizontalGradient(
    listOf(PrimaryIndigo, SecondaryViolet)
)

val EmeraldGradient = Brush.horizontalGradient(
    listOf(Color(0xFF059669), EmeraldSuccess)
)

val RoseGradient = Brush.horizontalGradient(
    listOf(Color(0xFFE11D48), RoseDanger)
)

val CardSurfaceGradient = Brush.verticalGradient(
    listOf(SurfaceCard, SurfaceCardEnd)
)

val GlassOverlayGradient = Brush.verticalGradient(
    listOf(Color(0x1AFFFFFF), Color(0x00FFFFFF))
)
