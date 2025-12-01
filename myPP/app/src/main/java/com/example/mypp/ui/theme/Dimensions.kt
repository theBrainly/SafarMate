package com.example.mypp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Custom shape definitions for SafarMate app
 * Helps maintain consistent corner radii across the app
 */
val AppShapes = Shapes(
    // Used for small components like chips
    extraSmall = RoundedCornerShape(4.dp),
    
    // Used for buttons, text fields
    small = RoundedCornerShape(8.dp),
    
    // Used for cards, dialogs
    medium = RoundedCornerShape(12.dp),
    
    // Used for bottom sheets, large cards
    large = RoundedCornerShape(16.dp),
    
    // Used for floating panels, modal bottom sheets
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Standard spacing values to maintain consistent padding and margins
 */
object AppSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    
    // Specific use cases
    val screenPadding = 16.dp
    val cardPadding = 16.dp
    val buttonPadding = 12.dp
    val textFieldPadding = 16.dp
    val iconPadding = 8.dp
}

/**
 * Standard elevation values for consistent shadows
 */
object AppElevation {
    val none = 0.dp
    val extraSmall = 1.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val extraLarge = 16.dp
}

/**
 * Standard sizes for components
 */
object AppSizes {
    val buttonHeight = 48.dp
    val largeButtonHeight = 56.dp
    val iconSize = 24.dp
    val smallIconSize = 16.dp
    val largeIconSize = 32.dp
    val avatarSize = 40.dp
    val smallAvatarSize = 24.dp
    val largeAvatarSize = 64.dp
    val cardCornerRadius = 12.dp
}