package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun buildDarkColorScheme() = darkColorScheme(
    primary = SynapseCyan,
    onPrimary = Color.White,
    primaryContainer = SynapseCyanContainer,
    onPrimaryContainer = Color.White,
    secondary = SynapseViolet,
    onSecondary = Color.White,
    secondaryContainer = SynapseVioletContainer,
    onSecondaryContainer = SynapseVioletLight,
    tertiary = SynapseCoralSos,
    onTertiary = Color.White,
    tertiaryContainer = SynapseCoralContainer,
    onTertiaryContainer = Color.White,
    background = SynapseObsidian,
    onBackground = SynapseTextPrimary,
    surface = SynapseSurface,
    onSurface = SynapseTextPrimary,
    surfaceVariant = SynapseCardSurface,
    onSurfaceVariant = SynapseTextSecondary,
    outline = SynapseBorder,
    error = SynapseCoralSos,
    onError = Color.White
)

private fun buildLightColorScheme() = lightColorScheme(
    primary = SynapseCyan,
    onPrimary = Color.White,
    primaryContainer = SynapseCyanContainer,
    onPrimaryContainer = Color(0xFF630B00),
    secondary = SynapseViolet,
    onSecondary = Color.White,
    secondaryContainer = SynapseVioletContainer,
    onSecondaryContainer = Color(0xFF630B00),
    tertiary = SynapseCoralSos,
    onTertiary = Color.White,
    tertiaryContainer = SynapseCoralContainer,
    onTertiaryContainer = Color(0xFF630B00),
    background = SynapseObsidian,
    onBackground = SynapseTextPrimary,
    surface = SynapseSurface,
    onSurface = SynapseTextPrimary,
    surfaceVariant = SynapseCardSurface,
    onSurfaceVariant = SynapseTextSecondary,
    outline = SynapseBorder,
    error = SynapseCoralSos,
    onError = Color.White
)

@Composable
fun FESCSynapseTheme(
    darkTheme: Boolean = isSynapseDarkMode,
    content: @Composable () -> Unit
) {
    isSynapseDarkMode = darkTheme
    val colorScheme = if (darkTheme) buildDarkColorScheme() else buildLightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
