package com.spendora.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
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
    onPrimary = SpendoraDarkColors.textPrimary,
    primaryContainer = SpendoraDarkColors.surfaceElevated,
    onPrimaryContainer = SpendoraDarkColors.textPrimary,
    secondary = SpendoraDarkColors.secondary,
    onSecondary = SpendoraDarkColors.textPrimary,
    background = SpendoraDarkColors.background,
    onBackground = SpendoraDarkColors.textPrimary,
    surface = SpendoraDarkColors.surfaceCard,
    onSurface = SpendoraDarkColors.textPrimary,
    surfaceVariant = SpendoraDarkColors.surfaceElevated,
    onSurfaceVariant = SpendoraDarkColors.textSecondary,
    error = SpendoraDarkColors.rose,
    onError = SpendoraDarkColors.textPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SpendoraLightColors.primary,
    onPrimary = SpendoraLightColors.surface,
    primaryContainer = SpendoraLightColors.surfaceElevated,
    onPrimaryContainer = SpendoraLightColors.textPrimary,
    secondary = SpendoraLightColors.secondary,
    onSecondary = SpendoraLightColors.surface,
    background = SpendoraLightColors.background,
    onBackground = SpendoraLightColors.textPrimary,
    surface = SpendoraLightColors.surface,
    onSurface = SpendoraLightColors.textPrimary,
    surfaceVariant = SpendoraLightColors.surfaceElevated,
    onSurfaceVariant = SpendoraLightColors.textSecondary,
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
