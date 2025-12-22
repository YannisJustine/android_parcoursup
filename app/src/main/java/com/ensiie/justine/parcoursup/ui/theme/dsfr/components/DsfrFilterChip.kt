package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

@Composable
fun DsfrFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val colors = DSFRTheme.colors
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            ProvideTextStyle(value = DSFRTheme.typography.bodySm) {
                label()
            }
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.backgroundAlt,
            labelColor = colors.textDefault,
            selectedContainerColor = colors.chipBackground,
            selectedLabelColor = colors.actionHigh,
            disabledContainerColor = colors.actionDisabled,
            disabledLabelColor = colors.textDisabled
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) colors.chipBorder else colors.borderDefault,
            selectedBorderColor = colors.chipBorder,
            disabledBorderColor = colors.borderDefault,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp,
            enabled = enabled,
            selected = selected
        )
    )
}