package com.threeloops.mirrorarchive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppleWhite,
    onPrimary = AppleBlack,
    surface = AppleDarkSurface,
    onSurface = AppleWhite,
    background = AppleBlack,
    onBackground = AppleWhite,
    outline = AppleDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AppleBlack,
    onPrimary = AppleWhite,
    surface = AppleLightSurface,
    onSurface = AppleBlack,
    background = AppleWhite,
    onBackground = AppleBlack,
    outline = AppleLightBorder
)

@Composable
fun MirrorArchiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
