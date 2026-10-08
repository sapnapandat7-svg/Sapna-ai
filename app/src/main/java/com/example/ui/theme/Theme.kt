package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = CyberBlack,
    primaryContainer = CyberNavyContainer,
    onPrimaryContainer = JarvisCyanBright,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = CyberNavySurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = NeonMint,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberNavyDark,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavySurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderCyan,
    outlineVariant = CyberNavySurfaceVariant,
    error = NeonCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always preserve JARVIS cyberpunk aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
