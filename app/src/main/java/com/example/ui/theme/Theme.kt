package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanMinimalColorScheme = lightColorScheme(
    primary = CleanMinimalPrimary,
    onPrimary = CleanMinimalOnPrimary,
    primaryContainer = CleanMinimalContainer,
    onPrimaryContainer = CleanMinimalPrimaryDark,
    secondary = CleanMinimalTextSecondary,
    onSecondary = Color.White,
    tertiary = CleanMinimalGold,
    background = CleanMinimalBackground,
    onBackground = CleanMinimalTextPrimary,
    surface = CleanMinimalSurface,
    onSurface = CleanMinimalTextPrimary,
    surfaceVariant = CleanMinimalSurfaceVariant,
    onSurfaceVariant = CleanMinimalTextSecondary,
    outline = CleanMinimalBorder,
    outlineVariant = CleanMinimalBorderLight
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CleanMinimalColorScheme,
        typography = Typography,
        content = content
    )
}


