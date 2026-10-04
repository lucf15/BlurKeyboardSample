package io.github.lucf15.blurkeyboard

import android.content.Context

internal enum class Appearance { System, Light, Dark }
internal data class BlurSettings(
    val appearance: Appearance = Appearance.System,
    val radius: Int = 22,
    val tint: Float = .52f,
)
internal class SettingsStore(context: Context) {
    val preferences = context.getSharedPreferences("demo", Context.MODE_PRIVATE)
    fun read() = BlurSettings(
        appearance = Appearance.entries.getOrElse(preferences.getInt("appearance", 0)) { Appearance.System },
        radius = preferences.getInt("radius", 22).coerceIn(0, 40),
        tint = preferences.getFloat("tint", .52f).coerceIn(0f, 1f))
    fun write(value: BlurSettings) = preferences.edit()
        .putInt("appearance", value.appearance.ordinal)
        .putInt("radius", value.radius).putFloat("tint", value.tint).apply()
}
