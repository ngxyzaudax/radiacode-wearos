package com.ngxyzaudax.radiacode.tile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.argb
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private const val TRANSPARENT_ARGB: Int = 0x00000000
private const val BLACK_ARGB: Int = 0xFF000000.toInt()
private const val ELEVATED_ARGB: Int = 0xFF0D0F13.toInt()
private const val OUTLINE_ARGB: Int = 0xFF272B32.toInt()
private const val OUTLINE_VARIANT_ARGB: Int = 0xFF14171B.toInt()

internal val TILE_BLACK: LayoutColor = BLACK_ARGB.argb
internal val TILE_TRANSPARENT: LayoutColor = TRANSPARENT_ARGB.argb

internal fun Color.layoutColor(): LayoutColor = toArgb().argb

internal fun Color.dim(alpha: Float): LayoutColor = copy(alpha = alpha).toArgb().argb

/**
 * True-black scheme for OLED panels: every surface token collapses to black so tiles blend into the
 * bezel and only data-bearing pixels are lit. Must be paired with `allowDynamicTheme = false`,
 * otherwise the platform replaces the whole scheme with system dynamic colors.
 */
internal fun oledColorScheme(): ColorScheme = ColorScheme(
    background = TILE_BLACK,
    onBackground = MetricPalette.bright.layoutColor(),
    surfaceContainer = TILE_BLACK,
    surfaceContainerLow = TILE_BLACK,
    surfaceContainerHigh = ELEVATED_ARGB.argb,
    onSurface = MetricPalette.bright.layoutColor(),
    onSurfaceVariant = MetricPalette.muted.layoutColor(),
    outline = OUTLINE_ARGB.argb,
    outlineVariant = OUTLINE_VARIANT_ARGB.argb,
    primary = MetricPalette.dose.layoutColor(),
    primaryDim = MetricPalette.dose.dim(0.6f),
    primaryContainer = ELEVATED_ARGB.argb,
    onPrimary = TILE_BLACK,
    onPrimaryContainer = MetricPalette.dose.layoutColor(),
    secondary = MetricPalette.count.layoutColor(),
    secondaryDim = MetricPalette.count.dim(0.6f),
    secondaryContainer = ELEVATED_ARGB.argb,
    onSecondary = TILE_BLACK,
    onSecondaryContainer = MetricPalette.count.layoutColor(),
    tertiary = MetricPalette.safe.layoutColor(),
    tertiaryDim = MetricPalette.safe.dim(0.6f),
    tertiaryContainer = ELEVATED_ARGB.argb,
    onTertiary = TILE_BLACK,
    onTertiaryContainer = MetricPalette.safe.layoutColor(),
    error = MetricPalette.danger.layoutColor(),
    errorDim = MetricPalette.danger.dim(0.6f),
    errorContainer = ELEVATED_ARGB.argb,
    onError = TILE_BLACK,
    onErrorContainer = MetricPalette.danger.layoutColor(),
)
