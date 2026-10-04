package io.github.lucf15.blurkeyboard.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun AppThemeProvider(isDark: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val colors =
        remember(isDark, dynamicColor, context, configuration) {
            when {
                dynamicColor && dynamicColorAvailable() -> dynamicAppColors(context, isDark)
                isDark -> DarkColors
                else -> LightColors
            }
        }

    CompositionLocalProvider(LocalAppColors provides colors, LocalIsDarkTheme provides isDark) {
        content()
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable get() = AppTypographyDefault

    val spacing: AppSpacing
        get() = Spacing
}

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("No AppColors provided") }
val LocalIsDarkTheme = staticCompositionLocalOf { false }
