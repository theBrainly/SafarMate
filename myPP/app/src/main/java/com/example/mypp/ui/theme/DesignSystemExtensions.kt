package com.example.mypp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Extension functions to easily access design system values from within composables
 */
object SafarMateTheme {
    /**
     * Access spacing values from the design system
     */
    val spacing: AppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current

    /**
     * Access shape values from the design system
     */
    val shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    /**
     * Access elevation values from the design system
     */
    val elevation: AppElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAppElevation.current

    /**
     * Access component size values from the design system
     */
    val sizes: AppSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSizes.current

    /**
     * Access typography from Material theme
     */
    val typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /**
     * Access color scheme from Material theme
     */
    val colors
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}

// Usage example:
// Box(
//    modifier = Modifier.padding(SafarMateTheme.spacing.medium),
//    shape = SafarMateTheme.shapes.medium
// )