package io.github.lucf15.blurkeyboard.designsystem.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val primary: Color,
    val accent: Color,
    val onPrimary: Color,
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textError: Color,
    val border: Color,
    val surface: Color,
    val success: Color,
    val warning: Color,
    val shadow: Color,
    val scrim: Color,
    val controlTrack: Color = surface,
    val controlSelected: Color = background,
    val controlSelectedBorder: Color = border,

) {
    fun primaryTint(alpha: Float) = primary.copy(alpha = alpha)
}

val LightColors =
    AppColors(
        primary = AppColorTokens.Primary,
        accent = AppColorTokens.Accent,
        onPrimary = Color.White,
        background = AppColorTokens.BackgroundLight,
        textPrimary = AppColorTokens.TextPrimaryLight,
        textSecondary = AppColorTokens.TextSecondaryLight,
        textError = AppColorTokens.TextError,
        border = AppColorTokens.BorderLight,
        surface = AppColorTokens.SurfaceLight,
        success = AppColorTokens.Success,
        warning = AppColorTokens.Warning,
        shadow = AppColorTokens.ShadowLight,
        scrim = AppColorTokens.SurfaceLight.copy(alpha = 0.55f),
        controlTrack = Color(0xFFE4E7EC),
        controlSelected = Color.White,
        controlSelectedBorder = Color(0xFFD7DCE3),
    )

val DarkColors =
    AppColors(
        primary = AppColorTokens.Primary,
        accent = AppColorTokens.Accent,
        onPrimary = Color.White,
        background = AppColorTokens.BackgroundDark,
        textPrimary = AppColorTokens.TextPrimaryDark,
        textSecondary = AppColorTokens.TextSecondaryDark,
        textError = AppColorTokens.TextError,
        border = AppColorTokens.BorderDark,
        surface = AppColorTokens.SurfaceDark,
        success = AppColorTokens.SuccessDark,
        warning = AppColorTokens.WarningDark,
        shadow = AppColorTokens.ShadowDark,
        scrim = Color.Black.copy(alpha = 0.45f),
        controlTrack = Color(0xFF191D23),
        controlSelected = Color(0xFF30363D),
        controlSelectedBorder = Color(0xFF3C4551),
    )

private object AppColorTokens {
    val Primary = Color(0xFF2563EB)
    val Accent = Color(0xFF4F86F7)

    val BackgroundLight = Color(0xFFF0F2F5)
    val BackgroundDark = Color(0xFF0D1117)

    val TextPrimaryLight = Color(0xFF000000)
    val TextPrimaryDark = Color(0xFFE6EDF3)

    val TextSecondaryLight = Color(0xFF6B7280)
    val TextSecondaryDark = Color(0xFF7D8590)
    val TextError = Color(0xFFF87171)

    val BorderLight = Color(0xFFE5E7EB)
    val BorderDark = Color(0xFF30363D)

    val SurfaceLight = Color(0xFFFEFEFE)
    val SurfaceDark = Color(0xFF191D23)

    val Success = Color(0xFF16A34A)
    val SuccessDark = Color(0xFF4ADE80)
    val Warning = Color(0xFFB45309)
    val WarningDark = Color(0xFFFBBF24)

    val ShadowLight = Color.Black.copy(alpha = 0.25f)
    val ShadowDark = Color.Black.copy(alpha = 0.50f)
}

@ChecksSdkIntAtLeast(api = 34)
fun dynamicColorAvailable(): Boolean = Build.VERSION.SDK_INT >= 34

@RequiresApi(34)
fun dynamicAppColors(context: Context, isDark: Boolean): AppColors {
    fun tone(@ColorRes id: Int) = Color(context.resources.getColor(id, context.theme))

    return if (isDark) {
        AppColors(
            primary = tone(android.R.color.system_primary_dark),
            accent = tone(android.R.color.system_secondary_dark),
            onPrimary = tone(android.R.color.system_on_primary_dark),
            background = tone(android.R.color.system_background_dark),
            textPrimary = tone(android.R.color.system_on_background_dark),
            textSecondary = tone(android.R.color.system_on_surface_variant_dark),
            textError = AppColorTokens.TextError,
            border = tone(android.R.color.system_outline_variant_dark),
            surface = tone(android.R.color.system_surface_container_high_dark),
            success = AppColorTokens.SuccessDark,
            warning = AppColorTokens.WarningDark,
            shadow = AppColorTokens.ShadowDark,
            scrim = tone(android.R.color.system_background_dark).copy(alpha = 0.5f),
            controlTrack = tone(android.R.color.system_surface_container_low_dark),
            controlSelected = tone(android.R.color.system_surface_container_highest_dark),
            controlSelectedBorder = tone(android.R.color.system_outline_variant_dark),
        )
    } else {
        AppColors(
            primary = tone(android.R.color.system_primary_light),
            accent = tone(android.R.color.system_secondary_light),
            onPrimary = tone(android.R.color.system_on_primary_light),
            background = tone(android.R.color.system_background_light),
            textPrimary = tone(android.R.color.system_on_background_light),
            textSecondary = tone(android.R.color.system_on_surface_variant_light),
            textError = AppColorTokens.TextError,
            border = tone(android.R.color.system_outline_variant_light),
            surface = tone(android.R.color.system_surface_container_high_light),
            success = AppColorTokens.Success,
            warning = AppColorTokens.Warning,
            shadow = AppColorTokens.ShadowLight,
            scrim = tone(android.R.color.system_surface_container_high_light).copy(alpha = 0.55f),
            controlTrack = tone(android.R.color.system_surface_container_highest_light),
            controlSelected = tone(android.R.color.system_surface_container_low_light),
            controlSelectedBorder = tone(android.R.color.system_outline_variant_light),
        )
    }
}
