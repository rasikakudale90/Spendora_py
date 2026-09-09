package com.spendora.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Semantic Design Token Palette for Spendora Dark Telemetry.
 * Aligned with Stitch MCP design system (design.md).
 * Supports dynamic Dark Mode (OLED Obsidian & Electric Cyan) & Light Mode (Pure Slate).
 */
@Immutable
data class SpendoraColors(
    val background: Color,
    val surface: Color,
    val surfaceCard: Color,
    val surfaceCardEnd: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderGlow: Color,
    val primary: Color,
    val primaryLight: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val emerald: Color,
    val emeraldLight: Color,
    val emeraldBg: Color,
    val rose: Color,
    val roseLight: Color,
    val roseBg: Color,
    val amber: Color,
    val amberLight: Color,
    val amberBg: Color,
    val cyan: Color,
    val cyanLight: Color,
    val cyanBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textHighlight: Color,
    val primaryGradient: Brush,
    val emeraldGradient: Brush,
    val roseGradient: Brush,
    val cardGradient: Brush,
    val glassOverlayGradient: Brush,
    val isDark: Boolean
)

// ── Spendora Dark Telemetry Palette (from design.md) ────────────────
val SpendoraDarkColors = SpendoraColors(
    background = Color(0xFF080A0F),
    surface = Color(0xFF0F131B),
    surfaceCard = Color(0xFF181C24),
    surfaceCardEnd = Color(0xFF0E121A),
    surfaceElevated = Color(0xFF262A33),
    border = Color(0xFF262E3B),
    borderGlow = Color(0xFF06B6D4),
    primary = Color(0xFF06B6D4),
    primaryLight = Color(0xFF4CD7F6),
    onPrimary = Color(0xFF080A0F),
    secondary = Color(0xFF8B5CF6),
    secondaryLight = Color(0xFFD0BCFF),
    emerald = Color(0xFF10B981),
    emeraldLight = Color(0xFF4EDEA3),
    emeraldBg = Color(0x2210B981),
    rose = Color(0xFFF43F5E),
    roseLight = Color(0xFFFFB4AB),
    roseBg = Color(0x22F43F5E),
    amber = Color(0xFFF59E0B),
    amberLight = Color(0xFFFBBF24),
    amberBg = Color(0x22F59E0B),
    cyan = Color(0xFF06B6D4),
    cyanLight = Color(0xFF4CD7F6),
    cyanBg = Color(0x2206B6D4),
    textPrimary = Color(0xFFDFE2EE),
    textSecondary = Color(0xFFBCC9CD),
    textMuted = Color(0xFF869397),
    textHighlight = Color(0xFF4CD7F6),
    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2))),
    emeraldGradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF4EDEA3))),
    roseGradient = Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFFF758F))),
    cardGradient = Brush.verticalGradient(listOf(Color(0xFF181C24), Color(0xFF0E121A))),
    glassOverlayGradient = Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x00FFFFFF))),
    isDark = true
)

// ── Pure Pearl Slate Light Palette ──────────────────────────────────
val SpendoraLightColors = SpendoraColors(
    background = Color(0xFFF4F6F9),
    surface = Color(0xFFFFFFFF),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceCardEnd = Color(0xFFF1F5F9),
    surfaceElevated = Color(0xFFE2E8F0),
    border = Color(0xFFCBD5E1),
    borderGlow = Color(0xFF06B6D4),
    primary = Color(0xFF0891B2),
    primaryLight = Color(0xFF06B6D4),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF7C3AED),
    secondaryLight = Color(0xFF8B5CF6),
    emerald = Color(0xFF059669),
    emeraldLight = Color(0xFF10B981),
    emeraldBg = Color(0x1A059669),
    rose = Color(0xFFE11D48),
    roseLight = Color(0xFFF43F5E),
    roseBg = Color(0x1AE11D48),
    amber = Color(0xFFD97706),
    amberLight = Color(0xFFF59E0B),
    amberBg = Color(0x1AD97706),
    cyan = Color(0xFF0891B2),
    cyanLight = Color(0xFF06B6D4),
    cyanBg = Color(0x1A0891B2),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    textHighlight = Color(0xFF0891B2),
    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF0891B2), Color(0xFF06B6D4))),
    emeraldGradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
    roseGradient = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E))),
    cardGradient = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))),
    glassOverlayGradient = Brush.verticalGradient(listOf(Color(0x0A000000), Color(0x00000000))),
    isDark = false
)

// ── Dynamic Dynamic Accessors (Zero Hardcoding) ──────────────────────
val BackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.background

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.surface

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.surfaceCard

val SurfaceCardEnd: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.surfaceCardEnd

val SurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.surfaceElevated

val BorderDark: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.border

val BorderGlow: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.borderGlow

val PrimaryIndigo: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.primary

val PrimaryIndigoLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.primaryLight

val PrimaryCyan: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.primary

val PrimaryCyanLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.primaryLight

val OnPrimaryColor: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.onPrimary

val SecondaryViolet: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.secondary

val SecondaryVioletLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.secondaryLight

val EmeraldSuccess: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.emerald

val EmeraldSuccessLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.emeraldLight

val EmeraldBg: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.emeraldBg

val RoseDanger: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.rose

val RoseDangerLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.roseLight

val RoseBg: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.roseBg

val AmberWarning: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.amber

val AmberWarningLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.amberLight

val AmberBg: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.amberBg

val CyanInfo: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.cyan

val CyanInfoLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.cyanLight

val CyanBg: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.cyanBg

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.textMuted

val TextHighlight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.textHighlight

val PrimaryGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.primaryGradient

val EmeraldGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.emeraldGradient

val RoseGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.roseGradient

val CardSurfaceGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.cardGradient

val QuantumViolet: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.secondary

val QuantumVioletLight: Color
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.secondaryLight

val SurfaceContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = if (SpendoraTheme.colors.isDark) Color(0xFF181C24) else Color(0xFFF1F5F9)

val SurfaceContainerLowest: Color
    @Composable
    @ReadOnlyComposable
    get() = if (SpendoraTheme.colors.isDark) Color(0xFF0A0E16) else Color(0xFFE2E8F0)

val SurfaceContainerHighest: Color
    @Composable
    @ReadOnlyComposable
    get() = if (SpendoraTheme.colors.isDark) Color(0xFF31353E) else Color(0xFFCBD5E1)
