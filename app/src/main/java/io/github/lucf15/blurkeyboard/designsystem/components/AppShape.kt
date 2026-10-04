package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun Modifier.appShapeWithShadow(
    shape: Shape,
    background: Color = AppTheme.colors.surface,
    border: Color = AppTheme.colors.border,
    shadow: Color = AppTheme.colors.shadow,
    elevation: Dp = 8.dp,
): Modifier =
    this.shadow(elevation = elevation, shape = shape, clip = false, ambientColor = shadow, spotColor = shadow)
        .clip(shape)
        .background(background, shape)
        .border(1.dp, border, shape)
