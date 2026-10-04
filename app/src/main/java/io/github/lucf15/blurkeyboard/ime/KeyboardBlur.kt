package io.github.lucf15.blurkeyboard.ime

import android.content.*
import io.github.lucf15.blurkeyboard.BlurSettings
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import java.util.function.Consumer

internal enum class BlurBackend(val label: String) { Android("Android blur"), Samsung("Samsung blur"), Opaque("Opaque") }

internal class KeyboardBlur(
    private val host: View,
    private val settings: () -> BlurSettings,
    private val changed: (BlurBackend) -> Unit,
) {
    private val context = host.context
    private val manager = context.getSystemService(WindowManager::class.java)
    private val samsung = SamsungWindowBlur(host)
    private var window: Window? = null
    private var listener: Consumer<Boolean>? = null
    private val policies = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = refresh()
    }
    private val accessibility = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = refresh()
    }
    private var observing = false
    private var appliedBackend: BlurBackend? = null
    private var appliedRadius = -1
    fun attach(target: Window) {
        if (window === target) { refresh(); return }
        detach()
        window = target
        target.setFormat(PixelFormat.TRANSLUCENT)
        // DecorView derives public blur corners from the background's round-rect outline.
        // A ColorDrawable has a rectangular outline, regardless of Compose clipping.
        target.setBackgroundDrawable(GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = KeyboardGeometry.TOP_CORNER_RADIUS_DP * context.resources.displayMetrics.density
        })
        target.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        if (Build.VERSION.SDK_INT >= 31) {
            listener = Consumer { refresh() }
            manager.addCrossWindowBlurEnabledListener(ContextCompat.getMainExecutor(context), listener!!)
        }
        ContextCompat.registerReceiver(context, policies, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
        context.contentResolver.registerContentObserver(Settings.System.getUriFor("accessibility_reduce_transparency"), false, accessibility)
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor("disable_window_blurs"), false, accessibility)
        observing = true
        refresh()
    }
    fun refresh() {
        val target = window ?: return
        val started = SystemClock.elapsedRealtimeNanos()
        Trace.beginSection("BlurSample.applyBlur")
        try {
            val permitted = !context.getSystemService(PowerManager::class.java).isPowerSaveMode &&
                Settings.System.getInt(context.contentResolver, "accessibility_reduce_transparency", 0) == 0 &&
                Settings.Global.getInt(context.contentResolver, "disable_window_blurs", 0) == 0
            val radius = (settings().radius * context.resources.displayMetrics.density).toInt().coerceAtMost(150)
            val backend = when {
                !permitted || Build.VERSION.SDK_INT < 31 -> BlurBackend.Opaque
                manager.isCrossWindowBlurEnabled -> BlurBackend.Android
                else -> BlurBackend.Samsung
            }
            if (backend == appliedBackend && radius == appliedRadius) return
            if (appliedBackend == BlurBackend.Samsung && backend != BlurBackend.Samsung) samsung.clear()
            if (appliedBackend == BlurBackend.Android && backend != BlurBackend.Android && Build.VERSION.SDK_INT >= 31)
                target.setBackgroundBlurRadius(0)
            val actual = when (backend) {
                BlurBackend.Android -> {
                    if (Build.VERSION.SDK_INT >= 31) target.setBackgroundBlurRadius(radius)
                    BlurBackend.Android
                }
                BlurBackend.Samsung -> if (samsung.apply(radius)) BlurBackend.Samsung else BlurBackend.Opaque
                BlurBackend.Opaque -> BlurBackend.Opaque
            }
            if (actual != appliedBackend) {
                changed(actual)
                Log.i("BlurSample", "backend=$actual applyMs=${(SystemClock.elapsedRealtimeNanos() - started) / 1_000_000.0}")
            }
            appliedBackend = actual
            appliedRadius = radius
        } finally { Trace.endSection() }
    }
    fun detach() {
        if (Build.VERSION.SDK_INT >= 31) {
            listener?.let(manager::removeCrossWindowBlurEnabledListener)
            window?.setBackgroundBlurRadius(0)
        }
        if (observing) {
            context.unregisterReceiver(policies)
            context.contentResolver.unregisterContentObserver(accessibility)
        }
        observing = false
        listener = null
        samsung.clear()
        window = null
        appliedBackend = null
        appliedRadius = -1
    }
}
