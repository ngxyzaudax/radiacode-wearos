package com.ngxyzaudax.radiacode.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.watchface.complications.data.ColorRamp

private const val MIN_ZONE_WIDTH: Float = 0.02f
private const val RAMP_STEPS: Int = 32

data class MetricZone(
    val startFraction: Float,
    val endFraction: Float,
    val color: Color,
)

object MetricPalette {
    val safe: Color = Color(0xFF00E5A0)
    val warn: Color = Color(0xFFFFC44B)
    val danger: Color = Color(0xFFFF4D6A)
    val dose: Color = Color(0xFF55DDFF)
    val count: Color = Color(0xFFC08BFF)
    val battery: Color = Color(0xFF8CE99A)
    val temperature: Color = Color(0xFFFFAB70)
    val track: Color = Color(0xFF23262C)
    val muted: Color = Color(0xFF9BA3AF)
    val bright: Color = Color(0xFFF2F4F7)
    val background: Color = Color(0xFF000000)
}

fun ascendingZones(scale: MetricScale): List<MetricZone> {
    val warn = scale.warnFraction
    val danger = scale.dangerFraction
    if (warn <= 0f && danger <= 0f) return listOf(MetricZone(0f, 1f, MetricPalette.safe))
    val warnStart = if (warn > 0f) warn else danger
    val dangerStart = if (danger > 0f) danger else 1f
    return listOfNotNull(
        zoneOrNull(0f, warnStart, MetricPalette.safe),
        zoneOrNull(warnStart, dangerStart, MetricPalette.warn),
        zoneOrNull(dangerStart, 1f, MetricPalette.danger),
    )
}

fun descendingZones(scale: MetricScale): List<MetricZone> = listOfNotNull(
    zoneOrNull(0f, scale.dangerFraction, MetricPalette.danger),
    zoneOrNull(scale.dangerFraction, scale.warnFraction, MetricPalette.warn),
    zoneOrNull(scale.warnFraction, 1f, MetricPalette.safe),
)

private fun zoneOrNull(start: Float, end: Float, color: Color): MetricZone? =
    if (end - start >= MIN_ZONE_WIDTH) {
        MetricZone(start.coerceIn(0f, 1f), end.coerceIn(0f, 1f), color)
    } else {
        null
    }

fun zoneColorAt(fraction: Float, zones: List<MetricZone>): Color =
    zones.lastOrNull { fraction >= it.startFraction }?.color
        ?: zones.firstOrNull()?.color
        ?: MetricPalette.safe

fun zoneColorRamp(zones: List<MetricZone>): ColorRamp =
    ColorRamp(
        colors = (0 until RAMP_STEPS)
            .map { step -> zoneColorAt(step.toFloat() / (RAMP_STEPS - 1), zones).value.toInt() }
            .toIntArray(),
        interpolated = false,
    )
