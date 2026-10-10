package com.ngxyzaudax.radiacode.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun RadiaCodeTheme(content: @Composable () -> Unit): Unit {
    MaterialTheme(
        colorScheme = radiacodeColorScheme(),
        content = content,
    )
}
