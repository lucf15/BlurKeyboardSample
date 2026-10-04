package io.github.lucf15.blurkeyboard.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.*
import io.github.lucf15.blurkeyboard.designsystem.components.*
import io.github.lucf15.blurkeyboard.designsystem.theme.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PlaygroundScreen(settingsState: State<BlurSettings>, change: (BlurSettings) -> Unit) {
    val colors = AppTheme.colors
    val appearance by remember(settingsState) { derivedStateOf { settingsState.value.appearance } }
    var text by rememberSaveable { mutableStateOf("") }
    AppScaffold(Modifier.fillMaxSize().background(colors.background),
        header = { padding -> AppHeader("Blur keyboard", padding) }) { scaffoldPadding ->
            LazyColumn(Modifier.fillMaxSize(), contentPadding = imeContentPadding(scaffoldPadding)) {
                stickyHeader {
                    Column(Modifier.fillMaxWidth().background(colors.background.copy(alpha = .98f)).padding(top = scaffoldPadding.calculateTopPadding() + 8.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        AppTextField(text, { text = it }, "Type something", showLabel = false)
                    }
                }
                item {
                    Column(Modifier.padding(top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        BasicText("Appearance", style = AppTheme.typography.cardTitle.copy(color = colors.textPrimary))
                        AppearanceControl(appearance) { change(settingsState.value.copy(appearance = it)) }
                    }
                    SectionDivider()
                }
                item {
                    val settings = settingsState.value
                    Column(Modifier.padding(top = 12.dp, bottom = 20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            BasicText("Glass", style = AppTheme.typography.cardTitle.copy(color = colors.textPrimary))
                            BasicText("Reset", Modifier.clickable { change(BlurSettings(appearance = settings.appearance)) }.padding(vertical = 4.dp), style = AppTheme.typography.caption.copy(color = colors.primary))
                        }
                        TuningSlider("Blur", settings.radius / 40f, 0f..1f, "${kotlin.math.round(settings.radius / 40f * 100).toInt()}%") { change(settings.copy(radius = kotlin.math.round(it * 40).toInt())) }
                        Spacer(Modifier.height(4.dp))
                        TuningSlider("Tint", settings.tint, 0f..1f, "${kotlin.math.round(settings.tint * 100).toInt()}%") { change(settings.copy(tint = kotlin.math.round(it * 100) / 100f)) }
                    }
                    SectionDivider()
                }
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 21.dp, bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Behind the glass", style = AppTheme.typography.cardTitle.copy(color = colors.textPrimary))
                        BasicText("Sample conversation", style = AppTheme.typography.caption.copy(color = colors.textSecondary, fontSize = 11.sp))
                    }
                    Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
                        BasicText("Today", style = AppTheme.typography.caption.copy(color = colors.textSecondary, fontSize = 11.sp))
                    }
                }
                itemsIndexed(SampleConversation) { _, message -> ConversationMessage(message) }
            }
    }
}

@Composable
private fun AppearanceControl(appearance: Appearance, change: (Appearance) -> Unit) {
    AppSegmentedControl(Appearance.entries.map { it.name }, appearance.ordinal) { change(Appearance.entries[it]) }
}

@Composable
private fun SectionDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.border))
}

@Composable
private fun imeContentPadding(scaffold: PaddingValues): PaddingValues {
    val insets = WindowInsets.ime.union(WindowInsets.navigationBars)
    val density = LocalDensity.current
    return remember(insets, density, scaffold) {
        object : PaddingValues {
            override fun calculateLeftPadding(layoutDirection: LayoutDirection) = scaffold.calculateLeftPadding(layoutDirection)
            override fun calculateTopPadding() = 0.dp
            override fun calculateRightPadding(layoutDirection: LayoutDirection) = scaffold.calculateRightPadding(layoutDirection)
            override fun calculateBottomPadding() = with(density) { insets.getBottom(this).toDp() } + 40.dp
        }
    }
}



@Composable
internal fun TuningSlider(title: String, value: Float, range: ClosedFloatingPointRange<Float>, label: String, change: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(title, style = AppTheme.typography.body.copy(fontSize = 14.sp, color = AppTheme.colors.textPrimary))
            BasicText(label, style = AppTheme.typography.caption.copy(color = AppTheme.colors.textSecondary))
        }
        AppSlider(value = value, onValueChange = change, valueRange = range,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = title })
    }
}



internal data class DemoMessage(val outgoing: Boolean, val text: String, val time: String)
internal val SampleConversation = listOf(
    DemoMessage(false, "Ciao! Hai già deciso cosa fare sabato?", "09:41"),
    DemoMessage(true, "Pensavo a una passeggiata sul lago. Poi ci fermiamo a pranzo, senza fretta.", "09:42"),
    DemoMessage(false, "Mi piace. Porto anche la macchina fotografica.", "09:43"),
    DemoMessage(true, "Perfetto! Ci troviamo alle 10 davanti alla stazione? Posso prendere il caffè mentre ti aspetto.", "09:44"),
    DemoMessage(false, "Yes, sounds like a plan. See you there!", "09:45"),
    DemoMessage(true, "A sabato allora. Speriamo ci sia una bella luce sul lago ☀", "09:46"),
    DemoMessage(false, "Ti mando un messaggio quando parto.", "09:47"),
    DemoMessage(true, "Va bene, a presto!", "09:48"),
)

@Composable
internal fun ConversationMessage(message: DemoMessage) {
    val colors = AppTheme.colors
    val background = if (message.outgoing) colors.primary else colors.surface
    val ink = if (message.outgoing) colors.onPrimary else colors.textPrimary
    Box(Modifier.fillMaxWidth().padding(vertical = 5.dp), contentAlignment = if (message.outgoing) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(Modifier.fillMaxWidth(.86f).background(background,
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (message.outgoing) 18.dp else 5.dp,
                bottomEnd = if (message.outgoing) 5.dp else 18.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
            BasicText(message.text, style = AppTheme.typography.body.copy(color = ink, fontSize = 14.sp, lineHeight = 21.sp))
            BasicText(message.time, Modifier.align(Alignment.End).padding(top = 8.dp),
                style = AppTheme.typography.caption.copy(color = ink.copy(alpha = .65f), fontSize = 10.sp))
        }
    }
}
