package com.ngxyzaudax.radiacode.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme

private val ELEVATED: Color = Color(0xFF0D0F13)
private val ELEVATED_HIGH: Color = Color(0xFF171A20)
private val OUTLINE: Color = Color(0xFF272B32)
private val OUTLINE_VARIANT: Color = Color(0xFF14171B)

/**
 * The in-app scheme is derived from the same [MetricPalette] the tiles and complications use, so a
 * reading keeps its colour meaning wherever it appears. Surfaces stay near-black for OLED panels:
 * only data-bearing pixels are lit.
 */
fun radiacodeColorScheme(): ColorScheme = ColorScheme(
    primary = MetricPalette.dose,
    onPrimary = MetricPalette.background,
    primaryContainer = ELEVATED,
    onPrimaryContainer = MetricPalette.dose,
    secondary = MetricPalette.count,
    onSecondary = MetricPalette.background,
    secondaryContainer = ELEVATED,
    onSecondaryContainer = MetricPalette.count,
    tertiary = MetricPalette.safe,
    onTertiary = MetricPalette.background,
    tertiaryContainer = ELEVATED,
    onTertiaryContainer = MetricPalette.safe,
    background = MetricPalette.background,
    onBackground = MetricPalette.bright,
    surfaceContainerLow = MetricPalette.background,
    surfaceContainer = ELEVATED,
    surfaceContainerHigh = ELEVATED_HIGH,
    onSurface = MetricPalette.bright,
    onSurfaceVariant = MetricPalette.muted,
    outline = OUTLINE,
    outlineVariant = OUTLINE_VARIANT,
    error = MetricPalette.danger,
    onError = MetricPalette.background,
    errorContainer = ELEVATED,
    onErrorContainer = MetricPalette.danger,
)
