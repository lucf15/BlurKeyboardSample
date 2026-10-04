package io.github.lucf15.blurkeyboard.ime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lucf15.blurkeyboard.designsystem.theme.LocalIsDarkTheme

@Composable
internal fun Keyboard(
    backend: BlurBackend, tint: () -> Float, numeric: Boolean,
    commit: (String) -> Unit, backspace: () -> Unit, enter: () -> Unit,
) {
    val dark = LocalIsDarkTheme.current
    val glass = backend != BlurBackend.Opaque
    val ink = if (dark) Color.White else Color.Black
    val surface = if (glass) (if (dark) Color(0xFF292929) else Color(0xFFDADBE1))
        else if (dark) Color(0xFF202020) else Color(0xFFE2E3E8)
    val face = if (!dark) Color.White else if (glass) Color.White.copy(alpha = .16f) else Color(0xFF444444)
    var shifted by remember { mutableStateOf(true) }
    var symbols by remember(numeric) { mutableStateOf(false) }
    var extraSymbols by remember { mutableStateOf(false) }
    val panel = RoundedCornerShape(topStart = KeyboardGeometry.TOP_CORNER_RADIUS_DP.dp, topEnd = KeyboardGeometry.TOP_CORNER_RADIUS_DP.dp)
    Column(Modifier.fillMaxWidth().clip(panel).drawBehind { drawRect(if (glass) surface.copy(alpha = tint()) else surface) }
        .border(.5.dp, (if (dark) Color.White else Color.Black).copy(alpha = .10f), panel)
        .semantics { contentDescription = "Sample keyboard" }
        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.captionBar))) {
        Row(Modifier.fillMaxWidth().height(51.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("Ciao", "Hello", "Grazie").forEachIndexed { index, suggestion ->
                if (index > 0) Box(Modifier.width(1.dp).height(24.dp).background(ink.copy(alpha = .07f)))
                Box(Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button) { commit("$suggestion "); shifted = false }
                    .semantics { contentDescription = "Suggestion $suggestion" }, contentAlignment = Alignment.Center) {
                    BasicText(suggestion, style = TextStyle(color = ink, fontFamily = FontFamily.SansSerif, fontSize = 17.sp))
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 6.5.dp)) {
            val gap = 6.dp
            val letterWidth = (maxWidth - gap * 9) / 10
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                if (numeric) {
                    listOf("123", "456", "789", ".0-").forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            row.forEach { c -> Key(c.toString(), "Key $c", face, ink, Modifier.weight(1f)) { commit(c.toString()) } }
                        }
                    }
                } else {
                    val first = if (!symbols) "qwertyuiop" else if (extraSymbols) "[]{}#%^*+=" else "1234567890"
                    val second = if (!symbols) "asdfghjkl" else if (extraSymbols) "_\\|~<>€£¥•" else "-/:;()€&@\""
                    listOf(first, second).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)) {
                            row.forEach { c -> Key(if (shifted && !symbols) c.uppercase() else "$c", "Key $c", face, ink, Modifier.width(letterWidth)) {
                                commit(if (shifted && !symbols) c.uppercase() else "$c"); shifted = false
                            } }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Key(if (symbols) "#+=" else "", if (symbols) "More symbols" else "Shift", face, ink,
                            Modifier.width(letterWidth * 1.36f), active = shifted && !symbols) {
                            if (symbols) extraSymbols = !extraSymbols else shifted = !shifted
                        }
                        Spacer(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            (if (symbols) ".,?!'" else "zxcvbnm").forEach { c ->
                                Key(if (shifted && !symbols) c.uppercase() else "$c", "Key $c", face, ink,
                                    Modifier.width(if (symbols) letterWidth * 1.46f else letterWidth)) {
                                    commit(if (shifted && !symbols) c.uppercase() else "$c"); shifted = false
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Key("", "Delete", face, ink, Modifier.width(letterWidth * 1.36f), onClick = backspace)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    if (numeric) Key("", "Delete", face, ink, Modifier.weight(1f), onClick = backspace)
                    else Key(if (symbols) "ABC" else "123", "Symbols", face, ink, Modifier.weight(1f)) { symbols = !symbols; extraSymbols = false }
                    Key("", "Space", face, ink, Modifier.weight(2.07f)) { commit(" ") }
                    Key("", "Enter", face, ink, Modifier.weight(1f), onClick = enter)
                }
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun Key(
    label: String, description: String, background: Color, ink: Color,
    modifier: Modifier, active: Boolean = false, onClick: () -> Unit,
) {
    Box(modifier.height(43.dp).clip(RoundedCornerShape(9.dp)).background(background)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center) {
        if (description == "Shift" || description == "Delete" || description == "Enter") {
            KeyIcon(description, active, ink)
        } else BasicText(label, style = TextStyle(color = ink, fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal, fontSize = if (label.length > 1) 18.sp else 22.sp))
    }
}

@Composable
private fun KeyIcon(kind: String, filled: Boolean, ink: Color) {
    val path = remember(kind) {
        Path().apply {
            when (kind) {
                "Shift" -> {
                    moveTo(3.3f, 11.5f); lineTo(10.9f, 3.4f)
                    quadraticTo(12f, 2.3f, 13.1f, 3.4f); lineTo(20.7f, 11.5f)
                    quadraticTo(21.8f, 12.8f, 20.1f, 12.8f); lineTo(16.5f, 12.8f)
                    lineTo(16.5f, 20f); quadraticTo(16.5f, 21f, 15.5f, 21f)
                    lineTo(8.5f, 21f); quadraticTo(7.5f, 21f, 7.5f, 20f)
                    lineTo(7.5f, 12.8f); lineTo(3.9f, 12.8f)
                    quadraticTo(2.2f, 12.8f, 3.3f, 11.5f); close()
                }
                "Delete" -> {
                    moveTo(9.3f, 4.4f); lineTo(20f, 4.4f)
                    quadraticTo(22f, 4.4f, 22f, 6.4f); lineTo(22f, 17.6f)
                    quadraticTo(22f, 19.6f, 20f, 19.6f); lineTo(9.3f, 19.6f)
                    quadraticTo(8.3f, 19.6f, 7.6f, 18.8f); lineTo(2.6f, 13.3f)
                    quadraticTo(1.4f, 12f, 2.6f, 10.7f); lineTo(7.6f, 5.2f)
                    quadraticTo(8.3f, 4.4f, 9.3f, 4.4f); close()
                }
                else -> {
                    moveTo(15f, 4f); lineTo(19f, 4f)
                    quadraticTo(21f, 4f, 21f, 6f); lineTo(21f, 12f)
                    quadraticTo(21f, 14f, 19f, 14f); lineTo(3f, 14f)
                    moveTo(9f, 8f); lineTo(3f, 14f); lineTo(9f, 20f)
                }
            }
        }
    }
    Canvas(Modifier.size(if (kind == "Enter") 26.dp else 24.dp)) {
        val factor = size.width / 24f
        with(drawContext.canvas) {
            save(); scale(factor, factor)
            if (kind == "Shift" && filled) this@Canvas.drawPath(path, ink)
            else this@Canvas.drawPath(path, ink, style = Stroke(1.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            if (kind == "Delete") {
                this@Canvas.drawLine(ink, Offset(11f, 8.8f), Offset(17.4f, 15.2f), 1.65f, StrokeCap.Round)
                this@Canvas.drawLine(ink, Offset(17.4f, 8.8f), Offset(11f, 15.2f), 1.65f, StrokeCap.Round)
            }
            restore()
        }
    }
}
