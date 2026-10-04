package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun AppHeader(title: String, insetPadding: PaddingValues) {
    Box(modifier = Modifier.fillMaxWidth().padding(insetPadding)) {
        BasicText(
            title,
            style = AppTheme.typography.headerLargeTitle.copy(color = AppTheme.colors.textPrimary),
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        )
    }
}
