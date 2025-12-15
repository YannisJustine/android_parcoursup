package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

@Composable
fun DsfrCircularProgressIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = DSFRTheme.colors.actionHigh,
        trackColor = DSFRTheme.colors.backgroundContrast,
        strokeWidth = 4.dp
    )
}

@Composable
fun DsfrLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = DSFRTheme.colors.actionHigh,
        trackColor = DSFRTheme.colors.backgroundContrast
    )
}