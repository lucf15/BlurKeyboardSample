package io.github.lucf15.blurkeyboard.ime

import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.lucf15.blurkeyboard.R
import io.github.lucf15.blurkeyboard.Appearance
import io.github.lucf15.blurkeyboard.BlurSettings
import io.github.lucf15.blurkeyboard.SettingsStore
import io.github.lucf15.blurkeyboard.designsystem.theme.AppThemeProvider

class BlurImeService : InputMethodService() {
    private var owner: ImeViewOwner? = null
    private var blur: KeyboardBlur? = null
    private lateinit var store: SettingsStore
    private var settings by mutableStateOf(BlurSettings())
    private val appearance by derivedStateOf { settings.appearance }
    private val tint by derivedStateOf { settings.tint }
    private var backend by mutableStateOf(BlurBackend.Opaque)
    private var numeric by mutableStateOf(false)
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val previous = settings
        settings = store.read()
        if (previous.radius != settings.radius) blur?.refresh()
        if (previous.appearance != settings.appearance) updateNavigationColors()
    }
    override fun onCreate() {
        setTheme(R.style.KeyboardTheme)
        super.onCreate()
        store = SettingsStore(this)
        settings = store.read()
        store.preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }
    override fun onCreateInputView(): View {
        blur?.detach(); owner?.destroy()
        val lifecycle = ImeViewOwner().also { owner = it }
        window.window?.decorView?.apply {
            setViewTreeLifecycleOwner(lifecycle); setViewTreeSavedStateRegistryOwner(lifecycle)
        }
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycle); setViewTreeSavedStateRegistryOwner(lifecycle)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                val dark = when (appearance) { Appearance.System -> systemDark; Appearance.Dark -> true; Appearance.Light -> false }
                AppThemeProvider(isDark = dark) {
                    Keyboard(backend, { tint }, numeric,
                        commit = { currentInputConnection?.commitText(it, 1) },
                        backspace = ::backspace, enter = ::enter)
                }
            }
        }
        val host = FrameLayout(this).apply { addView(view, FrameLayout.LayoutParams(-1, -2)) }
        blur = KeyboardBlur(host, { settings }) { backend = it }
        return host
    }
    override fun onEvaluateFullscreenMode() = false
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        val type = (attribute?.inputType ?: 0) and InputType.TYPE_MASK_CLASS
        numeric = type == InputType.TYPE_CLASS_NUMBER || type == InputType.TYPE_CLASS_PHONE || type == InputType.TYPE_CLASS_DATETIME
    }
    override fun onWindowShown() {
        super.onWindowShown(); owner?.visible(true)
        window.window?.let { WindowCompat.enableEdgeToEdge(it); blur?.attach(it) }
        updateNavigationColors()
    }
    override fun onWindowHidden() { blur?.detach(); owner?.visible(false); super.onWindowHidden() }
    override fun onConfigurationChanged(newConfig: Configuration) { super.onConfigurationChanged(newConfig); updateNavigationColors() }
    private fun updateNavigationColors() {
        val dark = when (appearance) {
            Appearance.Dark -> true
            Appearance.Light -> false
            Appearance.System -> resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
        window.window?.let { WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightNavigationBars = !dark }
    }
    private fun backspace() {
        val connection = currentInputConnection ?: return
        if (!connection.getSelectedText(0).isNullOrEmpty()) { connection.commitText("", 1); return }
        val before = (connection.getTextBeforeCursor(2, 0) ?: "")
        if (before.isEmpty()) return
        val count = if (before.length >= 2 && Character.isSurrogatePair(before[before.length - 2], before.last())) 2 else 1
        connection.deleteSurroundingText(count, 0)
    }
    private fun enter() {
        val info = currentInputEditorInfo ?: return
        val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED && info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION == 0)
            currentInputConnection?.performEditorAction(action)
        else currentInputConnection?.commitText("\n", 1)
    }
    override fun onDestroy() {
        store.preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        blur?.detach(); owner?.destroy(); super.onDestroy()
    }
}
