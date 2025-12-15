package com.ensiie.justine.parcoursup.ui.theme.dsfr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme

enum class DsfrTagType {
    Default,
    Info, 
    Success,  
    Warning,
    Error,
    Primary 
}

enum class DsfrTagSize {
    Small,
    Medium
}

@Composable
fun DsfrTag(
    text: String,
    modifier: Modifier = Modifier,
    type: DsfrTagType = DsfrTagType.Default,
    size: DsfrTagSize = DsfrTagSize.Medium,
    dismissible: Boolean = false,
    onDismiss: (() -> Unit)? = null
) {
    val colors = DSFRTheme.colors
    
    val (backgroundColor, textColor ) = when (type) {
        DsfrTagType.Default -> Pair(
            colors.backgroundAlt,
            colors.textDefault
        )
        DsfrTagType.Info -> Pair(
            colors.backgroundOpen,
            colors.info
        )
        DsfrTagType.Success -> Pair(
            Color(0xFFB8FEC9),
            colors.success
        )
        DsfrTagType.Warning -> Pair(
            Color(0xFFFFE9E6),
            colors.warning
        )
        DsfrTagType.Error -> Pair(
            Color(0xFFFFE9E9),
            colors.error
        )
        DsfrTagType.Primary -> Pair(
            colors.backgroundActionLow,
            colors.actionHigh
        )
    }
    
    val padding = when (size) {
        DsfrTagSize.Small -> Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        DsfrTagSize.Medium -> Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    }
    
    val textStyle = when (size) {
        DsfrTagSize.Small -> DsfrTextStyle.BodyXs
        DsfrTagSize.Medium -> DsfrTextStyle.BodySm
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
    ) {
        DsfrText(
            text = text,
            modifier = padding,
            textStyle = textStyle,
            customColor = textColor
        )
    }
}
