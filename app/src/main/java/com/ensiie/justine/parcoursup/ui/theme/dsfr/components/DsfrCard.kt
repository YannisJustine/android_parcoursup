package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

@Composable
fun DsfrCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4),
        colors = CardDefaults.cardColors(
            containerColor = DSFRTheme.colors.backgroundAlt
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (elevated) DSFRTheme.elevation.raised else 0.dp
        ),
        border = if (!elevated) BorderStroke(1.dp, DSFRTheme.colors.borderDefault) else null,
        content = content
    )
}