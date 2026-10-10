package com.ngxyzaudax.radiacode.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import com.ngxyzaudax.radiacode.R
import com.ngxyzaudax.radiacode.presentation.theme.MetricPalette

private val BUTTON_SIZE = 56.dp
private val ICON_SIZE = 28.dp

@Composable
fun TrackActionButtons(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
): Unit {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionIconButton(
            iconRes = R.drawable.ic_delete,
            description = "Delete track",
            container = MetricPalette.danger,
            onClick = onDelete,
        )
    }
}

@Composable
private fun ActionIconButton(
    @DrawableRes iconRes: Int,
    description: String,
    container: Color,
    onClick: () -> Unit,
): Unit {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(BUTTON_SIZE),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = MetricPalette.background,
        ),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            modifier = Modifier.size(ICON_SIZE),
        )
    }
}
