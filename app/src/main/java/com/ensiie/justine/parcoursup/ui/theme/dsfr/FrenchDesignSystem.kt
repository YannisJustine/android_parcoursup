package com.ensiie.justine.parcoursup.ui.theme.dsfr

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Couleurs officielles du DSFR
 * Source: https://github.com/GouvernementFR/dsfr/tree/main/src/dsfr/component
 */
@Immutable
data class DSFRColors(
    val blueFrance: Color,
    val blueFrance925: Color,
    val blueFrance850: Color,
    val blueFrance113: Color,
    val blueFrance975: Color,

    val rougeMarianne: Color,
    val rougeMarianne425: Color,
    val rougeMarianne625: Color,

    val grey950: Color,
    val grey900: Color,
    val grey850: Color,
    val grey800: Color,
    val grey200: Color,
    val grey1000: Color,
    val grey975: Color,
    val grey950alt: Color,
    val grey1000alt: Color,

    val textTitle: Color,
    val textLabel: Color,
    val textDefault: Color,
    val textMention: Color,
    val textDisabled: Color,
    val textInverted: Color,

    val backgroundDefault: Color,
    val backgroundAlt: Color,
    val backgroundContrast: Color,
    val backgroundActionHigh: Color,
    val backgroundActionLow: Color,
    val backgroundOpen: Color,
    val backgroundRaised: Color,
    val backgroundOverlay: Color,

    val borderDefault: Color,
    val borderPlain: Color,
    val borderActionHigh: Color,

    val actionHigh: Color,
    val actionLow: Color,
    val actionDisabled: Color,

    val success: Color,
    val error: Color,
    val warning: Color,
    val info: Color,

    val buttonPrimary: Color,
    val buttonPrimaryHover: Color,
    val buttonSecondary: Color,
    val buttonSecondaryHover: Color,
    val buttonTertiary: Color,

    val inputBorder: Color,
    val inputBackground: Color,

    val chipBackground: Color,
    val chipBorder: Color
)

@Immutable
data class DSFRTypography(
    val displayXl: TextStyle,
    val displayLg: TextStyle,
    val displayMd: TextStyle,
    val displaySm: TextStyle,
    val displayXs: TextStyle,
    val headingXl: TextStyle,
    val headingLg: TextStyle,
    val headingMd: TextStyle,
    val headingSm: TextStyle,
    val bodyLg: TextStyle,
    val bodyMd: TextStyle,
    val bodySm: TextStyle,
    val bodyXs: TextStyle
)

@Immutable
data class DSFRSpacing(
    val xs: Dp,      // 4dp
    val sm: Dp,      // 8dp
    val md: Dp,      // 16dp
    val lg: Dp,      // 24dp
    val xl: Dp,      // 32dp
    val xxl: Dp      // 48dp
)

@Immutable
data class DSFRElevation(
    val none: Dp,
    val raised: Dp,
    val lifted: Dp,
    val overlay: Dp
)

val LocalDSFRColors = staticCompositionLocalOf {
    LightDSFRColors
}

val LocalDSFRTypography = staticCompositionLocalOf {
    DefaultDSFRTypography
}

val LocalDSFRSpacing = staticCompositionLocalOf {
    DefaultDSFRSpacing
}

val LocalDSFRElevation = staticCompositionLocalOf {
    DefaultDSFRElevation
}

/**
 * Thème DSFR Light
 */
private val LightDSFRColors = DSFRColors(
    blueFrance = Color(0xFF000091),
    blueFrance925 = Color(0xFF1212FF),
    blueFrance850 = Color(0xFF2323FF),
    blueFrance113 = Color(0xFFECEDFE),
    blueFrance975 = Color(0xFFE3E3FD),

    rougeMarianne = Color(0xFFE1000F),
    rougeMarianne425 = Color(0xFFF95C5E),
    rougeMarianne625 = Color(0xFFCE0500),

    grey950 = Color(0xFF161616),
    grey900 = Color(0xFF1E1E1E),
    grey850 = Color(0xFF2A2A2A),
    grey800 = Color(0xFF383838),
    grey200 = Color(0xFFEEEEEE),
    grey1000 = Color(0xFF000000),
    grey975 = Color(0xFFF6F6F6),
    grey950alt = Color(0xFFFFFFFF),
    grey1000alt = Color(0xFF000000),

    textTitle = Color(0xFF161616),
    textLabel = Color(0xFF666666),
    textDefault = Color(0xFF3A3A3A),
    textMention = Color(0xFF666666),
    textDisabled = Color(0xFF929292),
    textInverted = Color(0xFFFFFFFF),

    backgroundDefault = Color(0xFFFFFFFF),
    backgroundAlt = Color(0xFFF6F6F6),
    backgroundContrast = Color(0xFF161616),
    backgroundActionHigh = Color(0xFF000091),
    backgroundActionLow = Color(0xFFE3E3FD),
    backgroundOpen = Color(0xFFECEDFE),
    backgroundRaised = Color(0xFFFFFFFF),
    backgroundOverlay = Color(0xCC000000),

    borderDefault = Color(0xFFDDDDDD),
    borderPlain = Color(0xFF929292),
    borderActionHigh = Color(0xFF000091),

    actionHigh = Color(0xFF000091),
    actionLow = Color(0xFFE3E3FD),
    actionDisabled = Color(0xFFE5E5E5),

    success = Color(0xFF18753C),
    error = Color(0xFFCE0500),
    warning = Color(0xFFB34000),
    info = Color(0xFF0063CB),

    buttonPrimary = Color(0xFF000091),
    buttonPrimaryHover = Color(0xFF1212FF),
    buttonSecondary = Color(0xFFFFFFFF),
    buttonSecondaryHover = Color(0xFFF6F6F6),
    buttonTertiary = Color(0x00000000),

    inputBorder = Color(0xFF3A3A3A),
    inputBackground = Color(0xFFEEEEEE),

    chipBackground = Color(0xFFE3E3FD),
    chipBorder = Color(0xFF000091)
)

/**
 * Thème DSFR Dark
 */
private val DarkDSFRColors = DSFRColors(
    blueFrance = Color(0xFF8585F6),
    blueFrance925 = Color(0xFF6A6AF4),
    blueFrance850 = Color(0xFF5555F3),
    blueFrance113 = Color(0xFF2A2A6C),
    blueFrance975 = Color(0xFF313178),

    rougeMarianne = Color(0xFFFF5C5C),
    rougeMarianne425 = Color(0xFFFF9191),
    rougeMarianne625 = Color(0xFFFE4B4B),

    grey950 = Color(0xFFF6F6F6),
    grey900 = Color(0xFFEEEEEE),
    grey850 = Color(0xFFDDDDDD),
    grey800 = Color(0xFFCCCCCC),
    grey200 = Color(0xFF2A2A2A),
    grey1000 = Color(0xFFFFFFFF),
    grey975 = Color(0xFF1E1E1E),
    grey950alt = Color(0xFF161616),
    grey1000alt = Color(0xFFFFFFFF),

    textTitle = Color(0xFFF6F6F6),
    textLabel = Color(0xFFCCCCCC),
    textDefault = Color(0xFFEEEEEE),
    textMention = Color(0xFFCCCCCC),
    textDisabled = Color(0xFF929292),
    textInverted = Color(0xFF161616),

    backgroundDefault = Color(0xFF1E1E1E),
    backgroundAlt = Color(0xFF2A2A2A),
    backgroundContrast = Color(0xFFFFFFFF),
    backgroundActionHigh = Color(0xFF8585F6),
    backgroundActionLow = Color(0xFF272747),
    backgroundOpen = Color(0xFF313178),
    backgroundRaised = Color(0xFF2A2A2A),
    backgroundOverlay = Color(0xCC000000),

    borderDefault = Color(0xFF3A3A3A),
    borderPlain = Color(0xFF6A6A6A),
    borderActionHigh = Color(0xFF8585F6),

    actionHigh = Color(0xFF8585F6),
    actionLow = Color(0xFF2A2A6C),
    actionDisabled = Color(0xFF3A3A3A),

    success = Color(0xFF27A658),
    error = Color(0xFFFF5C5C),
    warning = Color(0xFFFF9940),
    info = Color(0xFF518FFF),

    buttonPrimary = Color(0xFF8585F6),
    buttonPrimaryHover = Color(0xFF6A6AF4),
    buttonSecondary = Color(0xFF2A2A2A),
    buttonSecondaryHover = Color(0xFF383838),
    buttonTertiary = Color(0x00000000),

    inputBorder = Color(0xFFCCCCCC),
    inputBackground = Color(0xFF2A2A2A),

    chipBackground = Color(0xFF2A2A6C),
    chipBorder = Color(0xFF8585F6)
)

private val DefaultDSFRTypography = DSFRTypography(
    displayXl = TextStyle(
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold
    ),
    displayLg = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold
    ),
    displayMd = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySm = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    displayXs = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    headingXl = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    headingLg = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    headingMd = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    headingSm = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLg = TextStyle(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMd = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySm = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyXs = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    )
)

private val DefaultDSFRSpacing = DSFRSpacing(
    xs = 4.dp,
    sm = 8.dp,
    md = 16.dp,
    lg = 24.dp,
    xl = 32.dp,
    xxl = 48.dp
)

private val DefaultDSFRElevation = DSFRElevation(
    none = 0.dp,
    raised = 2.dp,
    lifted = 4.dp,
    overlay = 8.dp
)

@Composable
fun DSFRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkDSFRColors else LightDSFRColors

    CompositionLocalProvider(
        LocalDSFRColors provides colors,
        LocalDSFRTypography provides DefaultDSFRTypography,
        LocalDSFRSpacing provides DefaultDSFRSpacing,
        LocalDSFRElevation provides DefaultDSFRElevation,
        LocalOverscrollFactory provides null,
        content = content
    )
}

object DSFRTheme {
    val colors: DSFRColors
        @Composable
        get() = LocalDSFRColors.current

    val typography: DSFRTypography
        @Composable
        get() = LocalDSFRTypography.current

    val spacing: DSFRSpacing
        @Composable
        get() = LocalDSFRSpacing.current

    val elevation: DSFRElevation
        @Composable
        get() = LocalDSFRElevation.current
}