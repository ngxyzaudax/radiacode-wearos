package com.ngxyzaudax.radiacode.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

@Composable
fun ScreenHeader(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    title: String,
): Unit = with(scope) {
    ListHeader(
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
    ) {
        Text(title, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun NavMenuCard(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
): Unit = with(scope) {
    TitleCard(
        onClick = onClick,
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        subtitle = {
            Text(
                subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleColor = MaterialTheme.colorScheme.onSurface,
            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
fun PrimaryMetric(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    text: String,
): Unit = with(scope) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
fun BodyMetric(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    text: String,
): Unit = with(scope) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun ScreenActionButton(
    scope: TransformingLazyColumnItemScope,
    transformationSpec: TransformationSpec,
    label: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
): Unit = with(scope) {
    val colors = if (emphasized) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        colors = colors,
    ) {
        Text(label, textAlign = TextAlign.Center)
    }
}
