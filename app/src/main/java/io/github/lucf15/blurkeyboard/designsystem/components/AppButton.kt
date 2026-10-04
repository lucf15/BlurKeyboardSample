package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    filled: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val background = if (filled) AppTheme.colors.primary else AppTheme.colors.surface
    val contentColor = if (filled) AppTheme.colors.onPrimary else AppTheme.colors.textPrimary
    val border = if (filled) AppTheme.colors.primary else AppTheme.colors.border

    Box(
        modifier =
            modifier
                .height(52.dp)
                .appShapeWithShadow(shape = CircleShape, background = background, border = border)
                .clickable(role = Role.Button, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 28.dp).alpha(if (enabled) 1f else 0.5f),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Image(
                    imageVector = it,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.size(18.dp),
                )
            }
            BasicText(text = text, style = AppTheme.typography.button.copy(color = contentColor))
        }
    }
}
