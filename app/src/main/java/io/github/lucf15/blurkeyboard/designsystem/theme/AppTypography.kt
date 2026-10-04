package io.github.lucf15.blurkeyboard.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.lucf15.blurkeyboard.R

@OptIn(ExperimentalTextApi::class)
val Inter
    @Composable
    get() =
        FontFamily(
            Font(R.font.inter, weight = FontWeight.W400, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
            Font(R.font.inter, weight = FontWeight.W500, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
            Font(R.font.inter, weight = FontWeight.W600, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
            Font(R.font.inter, weight = FontWeight.W700, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
        )

@Immutable
data class AppTypography(
    val body: TextStyle,
    val navLabel: TextStyle,
    val headerTitle: TextStyle,
    val headerLargeTitle: TextStyle,
    val cardTitle: TextStyle,
    val sectionTitle: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
)

val AppTypographyDefault: AppTypography
    @Composable
    get() {
        val font = Inter
        return AppTypography(
            body = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
            navLabel = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 15.sp),
            headerTitle =
                TextStyle(
                    fontFamily = font,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.4).sp,
                ),
            headerLargeTitle =
                TextStyle(
                    fontFamily = font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    letterSpacing = (-0.4).sp,
                ),
            cardTitle = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
            sectionTitle =
                TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
            caption = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 17.sp),
            button = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        )
    }
