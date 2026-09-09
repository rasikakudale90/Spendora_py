package com.spendora.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalSpendoraColors = staticCompositionLocalOf { SpendoraDarkColors }

object SpendoraTheme {
    val colors: SpendoraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSpendoraColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = SpendoraDarkColors.primary,
    onPrimary = SpendoraDarkColors.onPrimary,
    primaryContainer = SpendoraDarkColors.primaryLight,
    onPrimaryContainer = Color(0xFF00424F),
    secondary = SpendoraDarkColors.secondary,
    onSecondary = Color(0xFF3C0091),
    secondaryContainer = Color(0xFF571BC1),
    onSecondaryContainer = Color(0xFFC4ABFF),
    tertiary = SpendoraDarkColors.emerald,
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF1BBD85),
    onTertiaryContainer = Color(0xFF00452E),
    background = SpendoraDarkColors.background,
    onBackground = SpendoraDarkColors.textPrimary,
    surface = SpendoraDarkColors.surface,
    onSurface = SpendoraDarkColors.textPrimary,
    surfaceVariant = SpendoraDarkColors.surfaceElevated,
    onSurfaceVariant = SpendoraDarkColors.textSecondary,
    outline = SpendoraDarkColors.border,
    outlineVariant = Color(0xFF3D494C),
    error = SpendoraDarkColors.rose,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = SpendoraLightColors.primary,
    onPrimary = SpendoraLightColors.onPrimary,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF00424F),
    secondary = SpendoraLightColors.secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDE7F6),
    onSecondaryContainer = Color(0xFF3C0091),
    tertiary = SpendoraLightColors.emerald,
    onTertiary = Color(0xFFFFFFFF),
    background = SpendoraLightColors.background,
    onBackground = SpendoraLightColors.textPrimary,
    surface = SpendoraLightColors.surface,
    onSurface = SpendoraLightColors.textPrimary,
    surfaceVariant = SpendoraLightColors.surfaceElevated,
    onSurfaceVariant = SpendoraLightColors.textSecondary,
    outline = SpendoraLightColors.border,
    outlineVariant = Color(0xFFCBD5E1),
    error = SpendoraLightColors.rose,
    onError = SpendoraLightColors.surface
)

@Composable
fun SpendoraTheme(
    themeMode: String = "dark", // "dark", "light", or "system"
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }

    val spendoraColors = if (isDark) SpendoraDarkColors else SpendoraLightColors
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = spendoraColors.background.toArgb()
            window.navigationBarColor = spendoraColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalSpendoraColors provides spendoraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
