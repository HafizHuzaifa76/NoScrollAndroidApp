package com.noscroll.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mist,
    onPrimaryContainer = EmeraldDeep,
    secondary = EmeraldDeep,
    onSecondary = Color.White,
    tertiary = Amber,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = CanvasDeep,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFCBD5D1),
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldBright,
    onPrimary = Ink,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = Mist,
    secondary = Mist,
    onSecondary = Ink,
    tertiary = Amber,
    background = Color(0xFF0B1211),
    onBackground = Color(0xFFF3F5F4),
    surface = Color(0xFF15201E),
    onSurface = Color(0xFFF3F5F4),
    surfaceVariant = Color(0xFF1E2B28),
    onSurfaceVariant = Color(0xFFC5D0CC),
    outline = Color(0xFF3A4A46),
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
