package com.vozbarrial.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VozNavy = Color(0xFF09243E)
val VozGreen = Color(0xFF087A55)
val VozMuted = Color(0xFF77869C)
val VozBackground = Color(0xFFF7F9FD)
val VozBlueSurface = Color(0xFFF0F4FC)

private val LightColorScheme = lightColorScheme(
    primary = VozNavy,
    onPrimary = Color.White,
    secondary = VozGreen,
    onSecondary = Color.White,
    background = VozBackground,
    surface = Color.White,
    onSurface = VozNavy,
    surfaceVariant = VozBlueSurface,
    onSurfaceVariant = VozMuted
)

@Composable
fun VozBarrialTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
