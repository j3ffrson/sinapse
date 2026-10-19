package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SynapseDarkColorScheme = darkColorScheme(
    primary = SynapseCyan,
    onPrimary = SynapseObsidian,
    primaryContainer = SynapseCyanContainer,
    onPrimaryContainer = SynapseCyan,
    secondary = SynapseViolet,
    onSecondary = Color.White,
    secondaryContainer = SynapseVioletContainer,
    onSecondaryContainer = SynapseVioletLight,
    tertiary = SynapseCoralSos,
    onTertiary = Color.White,
    tertiaryContainer = SynapseCoralContainer,
    onTertiaryContainer = SynapseCoralSos,
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SynapseDarkColorScheme,
        typography = Typography,
        content = content
    )
}
