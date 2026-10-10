package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

private const val QR_PIXELS: Int = 256

@Composable
fun QrCodeImage(content: String, modifier: Modifier = Modifier): Unit {
    val matrix = remember(content) { encodeQrMatrix(content) }
    Canvas(modifier = modifier) { drawQrMatrix(matrix) }
}

private fun encodeQrMatrix(content: String): BitMatrix =
    QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        QR_PIXELS,
        QR_PIXELS,
        mapOf(EncodeHintType.MARGIN to 1),
    )

private fun DrawScope.drawQrMatrix(matrix: BitMatrix): Unit {
    drawRect(Color.White)
    val cell = size.minDimension / matrix.width.toFloat()
    for (x in 0 until matrix.width) {
        drawQrColumn(matrix, x, cell)
    }
}

private fun DrawScope.drawQrColumn(matrix: BitMatrix, x: Int, cell: Float): Unit {
    for (y in 0 until matrix.height) {
        if (!matrix.get(x, y)) continue
        drawRect(
            color = Color.Black,
            topLeft = Offset(x * cell, y * cell),
            size = Size(cell, cell),
        )
    }
}
