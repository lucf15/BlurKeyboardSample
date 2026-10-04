/*
 * Copyright 2023 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Adapted from androidx.compose.material3 Slider.kt 1.5.0-alpha29:
 * horizontal continuous press-offset/drag, RTL, keyboard and semantics logic.
 * Material layout, tokens and rendering replaced with this project's design system.
 */
package io.github.lucf15.blurkeyboard.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme

@Composable
fun AppSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f, enabled: Boolean = true,
    onValueChangeFinished: () -> Unit = {}) {
    require(valueRange.endInclusive > valueRange.start)
    val colors = AppTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val inset = with(LocalDensity.current) { 14.dp.toPx() }
    val state = remember(valueRange) { SliderGestureState(valueRange) }
    val change by rememberUpdatedState(onValueChange)
    val finish by rememberUpdatedState(onValueChangeFinished)
    val current by rememberUpdatedState(value.coerceIn(valueRange))
    val interactions = remember { MutableInteractionSource() }
    val pressed = interactions.collectIsPressedAsState()
    val dragged = interactions.collectIsDraggedAsState()
    val focused = interactions.collectIsFocusedAsState()
    SideEffect {
        state.rtl = rtl
        state.inset = inset
        if (!state.dragging && !state.pressing) state.sync(current)
    }
    fun update(next: Float) {
        val bounded = next.coerceIn(valueRange)
        if (bounded != current) change(bounded)
    }
    val drag = rememberDraggableState { delta -> update(state.delta(delta)) }
    Canvas(modifier.fillMaxWidth().height(48.dp)
        .onSizeChanged { state.width = it.width.toFloat(); if (!state.dragging) state.sync(current) }
        .semantics {
            if (!enabled) disabled()
            progressBarRangeInfo = ProgressBarRangeInfo(current, valueRange)
            setProgress { requested ->
                val resolved = requested.coerceIn(valueRange)
                if (!enabled || resolved == current) false else { change(resolved); finish(); true }
            }
        }
        .onKeyEvent { event ->
            val direction = if (rtl) -1 else 1
            val increment = (valueRange.endInclusive - valueRange.start) / 100f
            val next = when (event.key) {
                Key.MoveHome, Key.NumPadMoveHome -> valueRange.start
                Key.MoveEnd, Key.NumPadMoveEnd -> valueRange.endInclusive
                Key.DirectionRight, Key.NumPadDirectionRight -> current + direction * increment
                Key.DirectionLeft, Key.NumPadDirectionLeft -> current - direction * increment
                Key.PageUp, Key.NumPadPageUp -> current + increment * 10
                Key.PageDown, Key.NumPadPageDown -> current - increment * 10
                else -> null
            }
            if (!enabled || next == null) false
            else if (event.type == KeyEventType.KeyDown) { update(next); true }
            else if (event.type == KeyEventType.KeyUp) { finish(); true } else false
        }
        .focusable(enabled, interactions)
        .pointerInput(state, enabled) {
            if (enabled) detectTapGestures(
                onPress = { position ->
                    val press = PressInteraction.Press(position)
                    // Suspending here can let onTap run before the press offset is recorded.
                    state.pressing = true
                    state.press(position.x)
                    interactions.tryEmit(press)
                    var released = false
                    try { released = tryAwaitRelease() }
                    finally {
                        state.pressing = false
                        interactions.tryEmit(if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                    }
                },
                onTap = { update(state.delta(0f)); if (!state.dragging) finish() },
            )
        }
        .draggable(drag, Orientation.Horizontal, enabled = enabled, interactionSource = interactions,
            reverseDirection = rtl, startDragImmediately = state.dragging,
            onDragStarted = { state.dragging = true },
            onDragStopped = { state.dragging = false; finish() })) {
        val fraction = ((current - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val start = if (rtl) size.width - inset else inset
        val end = if (rtl) inset else size.width - inset
        val x = start + fraction * (end - start)
        val center = Offset(x, size.height / 2)
        val alpha = if (enabled) 1f else .38f
        drawLine(colors.border.copy(alpha = alpha), Offset(start, center.y), Offset(end, center.y), 5.dp.toPx(), StrokeCap.Round)
        drawLine(colors.primary.copy(alpha = alpha), Offset(start, center.y), center, 5.dp.toPx(), StrokeCap.Round)
        if (pressed.value || dragged.value || focused.value) drawCircle(colors.primary.copy(alpha = .10f), 21.dp.toPx(), center)
        drawCircle(colors.shadow.copy(alpha = .10f), 11.dp.toPx(), center.copy(y = center.y + 1.dp.toPx()))
        drawCircle(colors.surface, 10.5.dp.toPx(), center)
        drawCircle(colors.primary.copy(alpha = alpha), 10.5.dp.toPx(), center, style = Stroke(2.dp.toPx()))
    }
}

private class SliderGestureState(val range: ClosedFloatingPointRange<Float>) {
    var width = 0f
    var inset = 0f
    var rtl = false
    var dragging by mutableStateOf(false)
    var pressing = false
    private var raw = 0f
    private var pressOffset = 0f
    private val track get() = (width - inset * 2).coerceAtLeast(1f)
    fun sync(value: Float) { raw = inset + (value - range.start) / (range.endInclusive - range.start) * track }
    fun press(x: Float) { pressOffset = (if (rtl) width - x else x) - raw }
    fun delta(delta: Float): Float {
        raw += delta + pressOffset
        pressOffset = 0f
        return range.start + ((raw - inset) / track).coerceIn(0f, 1f) * (range.endInclusive - range.start)
    }
}
