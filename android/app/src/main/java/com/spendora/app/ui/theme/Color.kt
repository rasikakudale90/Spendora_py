package com.spendora.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Semantic Design Token Palette for Spendora.
 * Supports dynamic Dark Mode (Midnight Obsidian) & Light Mode (Pure Pearl Slate).
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

// ── Midnight Obsidian Dark Palette ──────────────────────────────────
val SpendoraDarkColors = SpendoraColors(
    background = Color(0xFF070B12),
    surface = Color(0xFF0F172A),
    surfaceCard = Color(0xFF131D33),
    surfaceCardEnd = Color(0xFF0B1324),
    surfaceElevated = Color(0xFF1E293B),
    border = Color(0xFF1E2D4A),
    borderGlow = Color(0xFF334A73),
    primary = Color(0xFF6366F1),
    primaryLight = Color(0xFF818CF8),
    secondary = Color(0xFF8B5CF6),
    secondaryLight = Color(0xFFA78BFA),
    emerald = Color(0xFF10B981),
    emeraldLight = Color(0xFF34D399),
    emeraldBg = Color(0x2210B981),
    rose = Color(0xFFFF4D6D),
    roseLight = Color(0xFFFF758F),
    roseBg = Color(0x22FF4D6D),
    amber = Color(0xFFF59E0B),
    amberLight = Color(0xFFFBBF24),
    amberBg = Color(0x22F59E0B),
    cyan = Color(0xFF06B6D4),
    cyanLight = Color(0xFF38BDF8),
    cyanBg = Color(0x2206B6D4),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    textHighlight = Color(0xFF818CF8),
    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
    emeraldGradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
    roseGradient = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFFF4D6D))),
    cardGradient = Brush.verticalGradient(listOf(Color(0xFF131D33), Color(0xFF0B1324))),
    glassOverlayGradient = Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x00FFFFFF))),
    isDark = true
)

// ── Pure Pearl Slate Light Palette ──────────────────────────────────
val SpendoraLightColors = SpendoraColors(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceCardEnd = Color(0xFFF8FAFC),
    surfaceElevated = Color(0xFFF1F5F9),
    border = Color(0xFFE2E8F0),
    borderGlow = Color(0xFFCBD5E1),
    primary = Color(0xFF4F46E5),
    primaryLight = Color(0xFF6366F1),
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
    cyan = Color(0xFF0284C7),
    cyanLight = Color(0xFF0EA5E9),
    cyanBg = Color(0x1A0284C7),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    textHighlight = Color(0xFF4F46E5),
    primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
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

val GlassOverlayGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = SpendoraTheme.colors.glassOverlayGradient
