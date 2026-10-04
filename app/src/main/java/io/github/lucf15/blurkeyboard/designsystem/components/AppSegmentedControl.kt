package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun AppSegmentedControl(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = AppTheme.colors
    val selectedSurface = colors.controlSelected
    val selectedInk = colors.textPrimary
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val position = animateFloatAsState(selectedIndex.toFloat(), tween(180), label = "Appearance selection")
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(colors.controlTrack)) {
        Canvas(Modifier.matchParentSize()) {
            val padding = 3.dp.toPx()
            val width = (size.width - padding * 2) / labels.size
            val index = if (rtl) labels.lastIndex - position.value else position.value
            val offset = Offset(padding + index * width, padding)
            val selection = Size(width, size.height - padding * 2)
            drawRoundRect(colors.shadow.copy(alpha = .065f), offset.copy(y = offset.y + 1.dp.toPx()), selection, CornerRadius(9.dp.toPx()))
            drawRoundRect(selectedSurface, offset, selection, CornerRadius(9.dp.toPx()))
        }
        Row(Modifier.matchParentSize().padding(3.dp).selectableGroup()) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(9.dp))
                    .selectable(selected, role = Role.RadioButton) { onSelect(index) }, contentAlignment = Alignment.Center) {
                    BasicText(label, style = AppTheme.typography.caption.copy(
                        color = if (selected) selectedInk else colors.textSecondary,
                        fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}
