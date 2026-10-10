package com.ngxyzaudax.radiacode.complication

fun clampRangedValue(value: Float, min: Float, max: Float): Float {
    val safeMin = if (min.isFinite()) min else 0f
    val safeMax = when {
        !max.isFinite() -> safeMin + 0.01f
        max <= safeMin -> safeMin + 0.01f
        else -> max
    }
    val safeValue = if (value.isFinite()) value else safeMin
    return safeValue.coerceIn(safeMin, safeMax)
}
