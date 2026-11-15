package com.beast.rideshare.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OrichuPrimary,
    secondary = OrichuPrimary2,
    tertiary = OrichuPrimaryLight,
    background = OrichuDarkBg,
    surface = OrichuDarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OrichuDarkInk,
    onSurface = OrichuDarkInk,
    surfaceVariant = OrichuDarkSoft,
    onSurfaceVariant = OrichuDarkMuted,
    outline = OrichuDarkLine,
    outlineVariant = OrichuDarkLine,
    surfaceContainer = OrichuDarkCard,
    surfaceContainerHigh = OrichuDarkSoft,
    surfaceContainerHighest = OrichuDarkSoft,
    error = OrichuBad,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrichuPrimary,
    secondary = OrichuPrimary2,
    tertiary = OrichuPrimaryDark,
    background = OrichuLightBg,
    surface = OrichuLightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OrichuLightInk,
    onSurface = OrichuLightInk,
    surfaceVariant = OrichuLightSoft,
    onSurfaceVariant = OrichuLightMuted,
    outline = OrichuLightLine,
    outlineVariant = OrichuLightLine,
    surfaceContainer = OrichuLightCard,
    surfaceContainerHigh = OrichuLightSoft,
    surfaceContainerHighest = OrichuLightSoft,
    error = OrichuBad,
    onError = Color.White
)

@Composable
fun RideShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}
