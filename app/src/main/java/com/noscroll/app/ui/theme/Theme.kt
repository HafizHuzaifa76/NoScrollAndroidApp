package com.noscroll.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = ForestDark,
    onSecondary = Color.White,
    tertiary = Mist,
    background = Sand,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

private val DarkColorScheme = darkColorScheme(
    primary = Mist,
    onPrimary = Ink,
    secondary = Sand,
    onSecondary = Ink,
    tertiary = Forest,
    background = Color(0xFF121412),
    onBackground = Sand,
    surface = Color(0xFF1C1F1C),
    onSurface = Sand,
)

@Composable
fun NoScrollAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
