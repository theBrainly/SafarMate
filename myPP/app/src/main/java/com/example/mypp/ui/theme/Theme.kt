package com.example.mypp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define composition locals for our design system
val LocalAppShapes = staticCompositionLocalOf { AppShapes }
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing }
val LocalAppElevation = staticCompositionLocalOf { AppElevation }
val LocalAppSizes = staticCompositionLocalOf { AppSizes }

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color.White,
    primaryContainer = Blue80.copy(alpha = 0.15f),
    onPrimaryContainer = Blue80,
    secondary = Orange80,
    onSecondary = Color.White,
    secondaryContainer = Orange80.copy(alpha = 0.15f),
    onSecondaryContainer = Orange80,
    tertiary = Teal80,
    background = DarkBackgroundColor,
    surface = DarkSurfaceColor,
    onBackground = LightTextColor,
    onSurface = LightTextColor,
    surfaceVariant = DarkSurfaceVariantColor,
    onSurfaceVariant = LightTextColorSecondary,
    error = ErrorColor,
    errorContainer = ErrorColor.copy(alpha = 0.2f),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue40.copy(alpha = 0.15f),
    onPrimaryContainer = Blue40,
    secondary = Orange40,
    onSecondary = Color.White,
    secondaryContainer = Orange40.copy(alpha = 0.15f),
    onSecondaryContainer = Orange40,
    tertiary = Teal40,
    background = LightBackgroundColor,
    surface = LightSurfaceColor,
    onBackground = DarkTextColor,
    onSurface = DarkTextColor,
    surfaceVariant = LightSurfaceVariantColor,
    onSurfaceVariant = DarkTextColorSecondary,
    error = ErrorColor,
    errorContainer = ErrorColor.copy(alpha = 0.1f),
    onError = Color.White
)

/**
 * SafarMate app theme with consistent design system
 */
@Composable
fun MyPPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Not used yet, but prepared for dynamic color
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb() // Using surface color for status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide our design system values to the composition
    CompositionLocalProvider(
        LocalAppShapes provides AppShapes,
        LocalAppSpacing provides AppSpacing,
        LocalAppElevation provides AppElevation,
        LocalAppSizes provides AppSizes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}