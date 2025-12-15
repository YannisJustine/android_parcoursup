package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

enum class DsfrTextStyle {
    DisplayXl,
    DisplayLg,
    DisplayMd,
    DisplaySm,
    DisplayXs,
    HeadingXl,
    HeadingLg,
    HeadingMd,
    HeadingSm,
    BodyLg,
    BodyMd,
    BodySm,
    BodyXs
}

enum class DsfrTextColor {
    Title,
    Label,
    Default,
    Mention,
    Disabled,
    Inverted,
    Primary,
    Error,
    Success,
    Warning,
    Info
}

@Composable
fun DsfrText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: DsfrTextStyle = DsfrTextStyle.BodyMd,
    color: DsfrTextColor = DsfrTextColor.Default,
    customColor: Color? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val colors = DSFRTheme.colors
    val typography = DSFRTheme.typography
    
    val style = when (textStyle) {
        DsfrTextStyle.DisplayXl -> typography.displayXl
        DsfrTextStyle.DisplayLg -> typography.displayLg
        DsfrTextStyle.DisplayMd -> typography.displayMd
        DsfrTextStyle.DisplaySm -> typography.displaySm
        DsfrTextStyle.DisplayXs -> typography.displayXs
        DsfrTextStyle.HeadingXl -> typography.headingXl
        DsfrTextStyle.HeadingLg -> typography.headingLg
        DsfrTextStyle.HeadingMd -> typography.headingMd
        DsfrTextStyle.HeadingSm -> typography.headingSm
        DsfrTextStyle.BodyLg -> typography.bodyLg
        DsfrTextStyle.BodyMd -> typography.bodyMd
        DsfrTextStyle.BodySm -> typography.bodySm
        DsfrTextStyle.BodyXs -> typography.bodyXs
    }
    
    val textColor = customColor ?: when (color) {
        DsfrTextColor.Title -> colors.textTitle
        DsfrTextColor.Label -> colors.textLabel
        DsfrTextColor.Default -> colors.textDefault
        DsfrTextColor.Mention -> colors.textMention
        DsfrTextColor.Disabled -> colors.textDisabled
        DsfrTextColor.Inverted -> colors.textInverted
        DsfrTextColor.Primary -> colors.blueFrance
        DsfrTextColor.Error -> colors.error
        DsfrTextColor.Success -> colors.success
        DsfrTextColor.Warning -> colors.warning
        DsfrTextColor.Info -> colors.info
    }
    
    Text(
        text = text,
        modifier = modifier,
        color = textColor,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}
