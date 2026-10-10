package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

@Composable
fun SettingSectionCaption(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    label: String,
): Unit = with(scope) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec)
            .padding(top = 4.dp, bottom = 2.dp),
    )
}

@Composable
fun SettingToggleRow(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): Unit = with(scope) {
    SwitchButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = { Text(label, textAlign = TextAlign.Center) },
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
    )
}

@Composable
fun SettingOptionRow(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    label: String,
    onClick: () -> Unit,
): Unit = with(scope) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(label, textAlign = TextAlign.Center)
    }
}
