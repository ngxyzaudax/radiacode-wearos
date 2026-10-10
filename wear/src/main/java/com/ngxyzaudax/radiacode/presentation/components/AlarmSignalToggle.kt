package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

private val TOGGLE_HEIGHT = 36.dp
private val GLYPH_SIZE = 14.dp

@Composable
fun AlarmSignalToggle(
    label: String,
    checked: Boolean,
    width: Dp,
    onCheckedChange: (Boolean) -> Unit,
): Unit {
    val container = if (checked) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val content = if (checked) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .width(width)
            .height(TOGGLE_HEIGHT)
            .clip(RoundedCornerShape(percent = 50))
            .background(container)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckGlyph(checked = checked, fill = MaterialTheme.colorScheme.primary, outline = content)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
private fun CheckGlyph(checked: Boolean, fill: Color, outline: Color): Unit {
    Canvas(modifier = Modifier.size(GLYPH_SIZE)) {
        val corner = CornerRadius(size.minDimension * 0.28f)
        if (!checked) {
            drawRoundRect(
                color = outline,
                cornerRadius = corner,
                style = Stroke(width = size.minDimension * 0.12f),
            )
            return@Canvas
        }
        drawRoundRect(color = fill, cornerRadius = corner)
        drawTick(tickColorOn(fill))
    }
}

private fun DrawScope.drawTick(color: Color): Unit {
    val stroke = size.minDimension * 0.14f
    drawLine(
        color = color,
        start = Offset(size.width * 0.26f, size.height * 0.52f),
        end = Offset(size.width * 0.44f, size.height * 0.72f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.44f, size.height * 0.72f),
        end = Offset(size.width * 0.75f, size.height * 0.30f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

private fun tickColorOn(background: Color): Color {
    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance > 0.5f) Color.Black else Color.White
}
