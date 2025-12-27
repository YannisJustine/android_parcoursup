package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

enum class DsfrButtonType {
    Primary,
    Secondary,
    Tertiary
}

enum class DsfrButtonSize {
    Small,
    Medium,
    Large
}

@Composable
fun DsfrButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: DsfrButtonType = DsfrButtonType.Primary,
    size: DsfrButtonSize = DsfrButtonSize.Medium,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val colors = DSFRTheme.colors
    val contentPadding = when (size) {
        DsfrButtonSize.Small -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        DsfrButtonSize.Medium -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        DsfrButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (type) {
                DsfrButtonType.Primary -> colors.buttonPrimary
                DsfrButtonType.Secondary -> colors.buttonSecondary
                DsfrButtonType.Tertiary -> colors.buttonTertiary
            },
            contentColor = when (type) {
                DsfrButtonType.Primary -> colors.textInverted
                DsfrButtonType.Secondary -> colors.textTitle
                DsfrButtonType.Tertiary -> colors.actionHigh
            },
            disabledContainerColor = colors.actionDisabled,
            disabledContentColor = colors.textDisabled
        ),
        border = when (type) {
            DsfrButtonType.Secondary -> BorderStroke(1.dp, colors.borderActionHigh)
            else -> null
        },
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (type == DsfrButtonType.Primary) DSFRTheme.elevation.raised else 0.dp
        )
    ) {
        ProvideTextStyle(
            value = when (size) {
                DsfrButtonSize.Small -> DSFRTheme.typography.bodySm
                DsfrButtonSize.Medium -> DSFRTheme.typography.bodyMd
                DsfrButtonSize.Large -> DSFRTheme.typography.bodyLg
            }
        ) {
            content()
        }
    }
}

@Composable
fun DsfrTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    colors: ButtonColors? = null,
    content: @Composable RowScope.() -> Unit
) {
    val dsfrColors = DSFRTheme.colors

    val defaultColors = ButtonDefaults.textButtonColors(
        containerColor = if (selected) dsfrColors.backgroundActionLow else dsfrColors.backgroundAlt,
        contentColor = dsfrColors.actionHigh,
        disabledContentColor = dsfrColors.textDisabled
    )

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors ?: defaultColors
    ) {
        ProvideTextStyle(value = DSFRTheme.typography.bodyMd) {
            content()
        }
    }
}

@Composable
fun DsfrIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = DSFRTheme.colors.textTitle,
            disabledContentColor = DSFRTheme.colors.textDisabled
        )
    ) {
        content()
    }
}