package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy

val AppContentMaxWidth = 640.dp

internal val ContentGutter = 20.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    header: @Composable (PaddingValues) -> Unit = {},
    footer: @Composable (PaddingValues) -> Unit = {},
    contentWindowInsets: WindowInsets = ContentWindowInsets,
    applyContentHorizontalPadding: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    val safeInsets = remember(contentWindowInsets) { MutableWindowInsets(contentWindowInsets) }
    Box(modifier = modifier.onConsumedWindowInsetsChanged { consumed -> safeInsets.insets = contentWindowInsets.exclude(consumed) }) {
        ScaffoldLayout(
            header = header,
            footer = footer,
            content = content,
            contentWindowInsets = safeInsets,
            applyContentHorizontalPadding = applyContentHorizontalPadding,
        )
    }
}

@Composable
private fun ScaffoldLayout(
    header: @Composable (PaddingValues) -> Unit,
    footer: @Composable (PaddingValues) -> Unit,
    contentWindowInsets: WindowInsets,
    applyContentHorizontalPadding: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val contentPadding = remember {
        object : PaddingValues {
            var paddingHolder by mutableStateOf(PaddingValues(0.dp))

            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateLeftPadding(layoutDirection)

            override fun calculateTopPadding(): Dp = paddingHolder.calculateTopPadding()

            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp = paddingHolder.calculateRightPadding(layoutDirection)

            override fun calculateBottomPadding(): Dp = paddingHolder.calculateBottomPadding()
        }
    }

    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val layoutDirection = this@SubcomposeLayout.layoutDirection
        val insets = contentWindowInsets.asPaddingValues(this@SubcomposeLayout)
        val startInset = insets.calculateStartPadding(layoutDirection)
        val endInset = insets.calculateEndPadding(layoutDirection)
        val topInset = insets.calculateTopPadding()
        val bottomInset = insets.calculateBottomPadding()

        val extraHorizontal = ((layoutWidth.toDp() - startInset - endInset - AppContentMaxWidth) / 2).coerceAtLeast(0.dp)
        val gutterStart = ContentGutter + extraHorizontal
        val gutterEnd = endInset + ContentGutter + extraHorizontal

        val footerPlaceables =
            subcompose(ScaffoldSlot.Footer) { footer(PaddingValues(start = startInset + gutterStart, end = gutterEnd, bottom = bottomInset)) }
                .fastMap { it.measure(looseConstraints) }
        val footerHeight = footerPlaceables.fastMaxBy { it.height }?.height

        val headerPlaceables =
            subcompose(ScaffoldSlot.Header) { header(PaddingValues(start = startInset + gutterStart, top = topInset, end = gutterEnd)) }
                .fastMap { it.measure(looseConstraints.copy(maxHeight = layoutHeight - (footerHeight ?: 0))) }
        val headerHeight = headerPlaceables.fastMaxBy { it.height }?.height ?: 0

        contentPadding.paddingHolder =
            PaddingValues(
                top = if (headerPlaceables.isEmpty()) topInset else headerHeight.toDp(),
                bottom = if (footerPlaceables.isEmpty() || footerHeight == null) bottomInset else footerHeight.toDp(),
                start = if (applyContentHorizontalPadding) startInset + gutterStart else 0.dp,
                end = if (applyContentHorizontalPadding) gutterEnd else 0.dp,
            )

        val bodyPlaceables = subcompose(ScaffoldSlot.Body) { content(contentPadding) }.fastMap { it.measure(looseConstraints) }

        layout(layoutWidth, layoutHeight) {
            bodyPlaceables.fastForEach { it.place(0, 0) }
            headerPlaceables.fastForEach { it.place(0, 0) }
            footerPlaceables.fastForEach { it.place(0, layoutHeight - (footerHeight ?: 0)) }
        }
    }
}

private enum class ScaffoldSlot {
    Header,
    Body,
    Footer,
}
