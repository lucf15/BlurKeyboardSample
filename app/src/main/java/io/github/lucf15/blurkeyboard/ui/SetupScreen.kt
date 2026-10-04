package io.github.lucf15.blurkeyboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.lucf15.blurkeyboard.designsystem.components.AppButton
import io.github.lucf15.blurkeyboard.designsystem.components.AppScaffold
import io.github.lucf15.blurkeyboard.designsystem.components.AppHeader
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import io.github.lucf15.blurkeyboard.designsystem.theme.AppTheme
import android.os.Handler
import android.os.Looper
import android.database.ContentObserver
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
internal fun SetupScreen(enabled: Boolean, enable: () -> Unit, choose: () -> Unit) {
    AppScaffold(Modifier.fillMaxSize().background(AppTheme.colors.background),
        header = { padding -> AppHeader("Blur keyboard", padding) }) { padding ->
        SetupStepper(enabled, enable, choose,
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()))
    }
}

@Composable
private fun SetupStepper(enabled: Boolean, enable: () -> Unit, choose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 24.dp)) {
        SetupStep(1, "Enable keyboard", "Turn on Blur keyboard in Android settings.",
            active = !enabled, complete = enabled, connector = true,
            action = if (!enabled) enable else null, actionLabel = "Open settings")
        SetupStep(2, "Choose keyboard", "Select Blur keyboard from the keyboard list.",
            active = enabled, complete = false, connector = false,
            action = if (enabled) choose else null, actionLabel = "Choose keyboard")
    }
}

@Composable
private fun SetupStep(number: Int, title: String, description: String, active: Boolean, complete: Boolean,
    connector: Boolean, action: (() -> Unit)?, actionLabel: String) {
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.width(32.dp).fillMaxHeight()) {
            if (connector) Canvas(Modifier.fillMaxSize()) {
                drawLine(if (complete) colors.primary.copy(alpha = .25f) else colors.border,
                    Offset(size.width / 2, 40.dp.toPx()), Offset(size.width / 2, size.height - 8.dp.toPx()), 1.5.dp.toPx())
            }
            Box(Modifier.size(32.dp).background(if (active || complete) colors.primary else colors.border, CircleShape)
                .semantics { contentDescription = if (complete) "Step $number complete" else "Step $number" }, contentAlignment = Alignment.Center) {
                if (complete) Canvas(Modifier.size(16.dp)) {
                    drawLine(colors.onPrimary, Offset(size.width * .15f, size.height * .5f), Offset(size.width * .4f, size.height * .75f), 1.7.dp.toPx(), StrokeCap.Round)
                    drawLine(colors.onPrimary, Offset(size.width * .4f, size.height * .75f), Offset(size.width * .85f, size.height * .25f), 1.7.dp.toPx(), StrokeCap.Round)
                } else BasicText("$number", style = AppTheme.typography.cardTitle.copy(color = if (active) colors.onPrimary else colors.textSecondary))
            }
        }
        Column(Modifier.weight(1f).padding(top = 5.dp, bottom = if (connector) 36.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicText(title, style = AppTheme.typography.cardTitle.copy(color = if (active || complete) colors.textPrimary else colors.textSecondary))
            if (complete) BasicText("Enabled", style = AppTheme.typography.caption.copy(color = colors.primary))
            else BasicText(description, style = AppTheme.typography.body.copy(color = colors.textSecondary))
            if (action != null) {
                Spacer(Modifier.height(6.dp))
                AppButton(actionLabel, Modifier.fillMaxWidth(), filled = true, onClick = action)
            }
        }
    }
}



internal data class ImeSetup(val enabled: Boolean, val selected: Boolean) {
    val ready: Boolean get() = enabled && selected
}

@Composable
internal fun rememberImeSetup(context: android.content.Context, lifecycle: Lifecycle): ImeSetup {
    val manager = context.getSystemService(InputMethodManager::class.java)
    fun read() = ImeSetup(
        enabled = manager.enabledInputMethodList.any { it.packageName == context.packageName },
        selected = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.startsWith("${context.packageName}/") == true,
    )
    var state by remember { mutableStateOf(read()) }
    DisposableEffect(Unit) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { state = read() }
        }
        val resumed = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) state = read() }
        lifecycle.addObserver(resumed)
        context.contentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD), false, observer)
        context.contentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS), false, observer)
        onDispose { context.contentResolver.unregisterContentObserver(observer); lifecycle.removeObserver(resumed) }
    }
    return state
}
