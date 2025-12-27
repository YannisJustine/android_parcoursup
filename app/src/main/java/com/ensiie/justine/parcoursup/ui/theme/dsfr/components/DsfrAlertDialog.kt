package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

@Composable
fun DsfrAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = RoundedCornerShape(7),
        containerColor = DSFRTheme.colors.backgroundDefault,
        iconContentColor = DSFRTheme.colors.actionHigh,
        titleContentColor = DSFRTheme.colors.textTitle,
        textContentColor = DSFRTheme.colors.textDefault,
        tonalElevation = DSFRTheme.elevation.overlay,
        properties = properties
    )
}