package io.github.lucf15.blurkeyboard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.core.view.WindowCompat
import io.github.lucf15.blurkeyboard.designsystem.theme.AppThemeProvider
import io.github.lucf15.blurkeyboard.ui.PlaygroundScreen
import io.github.lucf15.blurkeyboard.ui.SetupScreen
import io.github.lucf15.blurkeyboard.ui.rememberImeSetup

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        val store = SettingsStore(this)
        setContent {
            val settings = remember { mutableStateOf(store.read()) }
            val appearance by remember { derivedStateOf { settings.value.appearance } }
            DisposableEffect(store) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> settings.value = store.read() }
                store.preferences.registerOnSharedPreferenceChangeListener(listener)
                onDispose { store.preferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            val systemDark = isSystemInDarkTheme()
            val dark = when (appearance) { Appearance.System -> systemDark; Appearance.Dark -> true; Appearance.Light -> false }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            AppThemeProvider(isDark = dark) {
                val setup = rememberImeSetup(this, lifecycle)
                if (setup.ready) PlaygroundScreen(settings, store::write)
                else SetupScreen(setup.enabled,
                    enable = { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
                    choose = { getSystemService(InputMethodManager::class.java).showInputMethodPicker() })
            }
        }
    }
}
