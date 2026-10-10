package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.ngxyzaudax.radiacode.protocol.Spectrum
import kotlin.math.ln
import kotlin.math.max

private const val TARGET_POINTS: Int = 120

data class SpectrumPoint(
    val x: Float,
    val y: Float,
)

fun spectrumPoints(
    spectrum: Spectrum?,
    width: Float,
    height: Float,
    logarithmic: Boolean,
): List<SpectrumPoint> {
    val counts = spectrum?.counts ?: return emptyList()
    if (counts.isEmpty() || width <= 0f || height <= 0f) return emptyList()
    val bins = downsample(counts, TARGET_POINTS)
    val maxCount = bins.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val stepX = width / bins.size.coerceAtLeast(1)
    return bins.mapIndexed { index, count ->
        val normalized = normalize(count, maxCount, logarithmic)
        SpectrumPoint(
            x = index * stepX + stepX / 2f,
            y = height - normalized * height,
        )
    }
}

fun spectrumPath(points: List<SpectrumPoint>): Path {
    val path = Path()
    points.forEachIndexed { index, point ->
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    return path
}

fun spectrumEnergyRange(spectrum: Spectrum?): Pair<Float, Float> {
    val counts = spectrum?.counts ?: return 0f to 0f
    if (counts.isEmpty()) return 0f to 0f
    val minEnergy = spectrum.channelEnergy(0)
    val maxEnergy = spectrum.channelEnergy(counts.lastIndex)
    return minEnergy to maxEnergy
}

private fun downsample(counts: IntArray, target: Int): IntArray {
    if (counts.size <= target) return counts
    val binSize = counts.size.toFloat() / target
    return IntArray(target) { index ->
        val start = (index * binSize).toInt()
        val end = ((index + 1) * binSize).toInt().coerceAtMost(counts.size)
        counts.slice(start until end).maxOrNull() ?: 0
    }
}

private fun normalize(count: Int, maxCount: Float, logarithmic: Boolean): Float =
    if (logarithmic) {
        ln(max(count.toFloat(), 1f)) / ln(maxCount)
    } else {
        count / maxCount
    }

fun gridLineY(height: Float, fraction: Float): Offset =
    Offset(0f, height * (1f - fraction))
