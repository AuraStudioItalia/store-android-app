package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PixelPrimary80,
    onPrimary = PixelOnPrimary20,
    primaryContainer = PixelPrimaryContainer30,
    onPrimaryContainer = PixelOnPrimaryContainer90,
    secondary = PixelSecondary80,
    onSecondary = PixelOnSecondary20,
    secondaryContainer = PixelSecondaryContainer30,
    onSecondaryContainer = PixelOnSecondaryContainer90,
    tertiary = PixelTertiary80,
    onTertiary = PixelOnTertiary20,
    tertiaryContainer = PixelTertiaryContainer30,
    onTertiaryContainer = PixelOnTertiaryContainer90,
    background = PixelDarkBackground,
    onBackground = AuraTextPrimary,
    surface = PixelDarkSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = PixelDarkSurfaceContainerHigh,
    onSurfaceVariant = AuraTextSecondary,
    surfaceContainer = PixelDarkSurfaceContainer,
    surfaceContainerHigh = PixelDarkSurfaceContainerHigh,
    surfaceContainerHighest = PixelDarkSurfaceContainerHighest,
    outline = PixelDarkSurfaceContainerHighest,
    error = PixelError80,
    onError = PixelOnError20
)

private val LightColorScheme = lightColorScheme(
    primary = PixelPrimaryLight,
    onPrimary = PixelOnPrimaryLight,
    primaryContainer = PixelPrimaryContainerLight,
    onPrimaryContainer = PixelOnPrimaryContainerLight,
    secondary = PixelSecondaryLight,
    onSecondary = PixelOnSecondaryLight,
    secondaryContainer = PixelSecondaryContainerLight,
    onSecondaryContainer = PixelOnSecondaryContainerLight,
    tertiary = PixelTertiaryLight,
    onTertiary = PixelOnTertiaryLight,
    tertiaryContainer = PixelTertiaryContainerLight,
    onTertiaryContainer = PixelOnTertiaryContainerLight,
    background = PixelLightBackground,
    onBackground = PixelOnBackgroundLight,
    surface = PixelLightSurface,
    onSurface = PixelOnSurfaceLight,
    surfaceVariant = PixelLightSurfaceVariant,
    onSurfaceVariant = PixelOnSurfaceVariantLight,
    surfaceContainer = PixelLightSurfaceContainer,
    surfaceContainerHigh = PixelLightSurfaceContainerHigh,
    surfaceContainerHighest = PixelLightSurfaceContainerHighest,
    outline = PixelLightOutline,
    error = PixelErrorLight,
    onError = PixelOnErrorLight
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
