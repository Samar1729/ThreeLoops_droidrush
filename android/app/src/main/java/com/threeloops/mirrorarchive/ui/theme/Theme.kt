package com.threeloops.mirrorarchive.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GreenWhiteColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenBadgeBg,
    onPrimaryContainer = GreenBadgeText,
    surface = GreenSurface,
    onSurface = GreenTextHeading,
    surfaceVariant = GreenSurfaceVariant,
    onSurfaceVariant = GreenTextSecondary,
    background = GreenWhiteBackground,
    onBackground = GreenTextHeading,
    outline = GreenBorder
)

@Composable
fun MirrorArchiveTheme(
    darkTheme: Boolean = false, // Green to White theme
    content: @Composable () -> Unit
) {
    val colorScheme = GreenWhiteColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GreenWhiteBackground.toArgb()
            window.navigationBarColor = GreenWhiteBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
