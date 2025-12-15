package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.VisualTransformation
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

@Composable
fun DsfrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val colors = DSFRTheme.colors
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(6),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textDefault,
            unfocusedTextColor = colors.textDefault,
            disabledTextColor = colors.textDisabled,
            errorTextColor = colors.textDefault,
            focusedContainerColor = colors.inputBackground,
            unfocusedContainerColor = colors.inputBackground,
            disabledContainerColor = colors.actionDisabled,
            errorContainerColor = colors.inputBackground,
            focusedBorderColor = colors.borderActionHigh,
            unfocusedBorderColor = colors.inputBorder,
            disabledBorderColor = colors.borderDefault,
            errorBorderColor = colors.error,
            focusedLabelColor = colors.textLabel,
            unfocusedLabelColor = colors.textLabel,
            disabledLabelColor = colors.textDisabled,
            errorLabelColor = colors.error,
            focusedPlaceholderColor = colors.textMention,
            unfocusedPlaceholderColor = colors.textMention,
            disabledPlaceholderColor = colors.textDisabled,
            errorPlaceholderColor = colors.textMention
        )
    )
}