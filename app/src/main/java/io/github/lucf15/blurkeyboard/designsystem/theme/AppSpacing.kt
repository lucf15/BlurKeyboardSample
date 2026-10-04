package io.github.lucf15.blurkeyboard.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(val xs: Dp, val sm: Dp, val m: Dp, val md: Dp, val lg: Dp, val xl: Dp)

val Spacing = AppSpacing(xs = 4.dp, sm = 8.dp, m = 12.dp, md = 16.dp, lg = 24.dp, xl = 32.dp)
