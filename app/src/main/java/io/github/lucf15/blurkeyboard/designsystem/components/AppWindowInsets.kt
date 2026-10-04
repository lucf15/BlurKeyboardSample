package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalContentWindowInsetsOverride = compositionLocalOf<WindowInsets?> { null }

val ContentWindowInsets: WindowInsets
    @Composable get() = LocalContentWindowInsetsOverride.current ?: WindowInsets.systemBars.union(WindowInsets.displayCutout)
