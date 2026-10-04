package io.github.lucf15.blurkeyboard.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.graphics.Color
import android.os.Build
import android.view.View

internal class SamsungWindowBlur(private val host: View) {
    private var applied = false
    fun apply(radiusPx: Int): Boolean {
        val api = methods ?: return false
        return runCatching {
            val builder = api.builder.newInstance(0) // Samsung MODE_WINDOW: live content behind the view.
            api.radius.invoke(builder, radiusPx)
            api.color.invoke(builder, Color.TRANSPARENT)
            val corner = KeyboardGeometry.TOP_CORNER_RADIUS_DP * host.resources.displayMetrics.density
            // Samsung uses TL, TR, BL, BR order.
            api.corners.invoke(builder, corner, corner, 0f, 0f)
            api.apply.invoke(host, api.build.invoke(builder))
            applied = true
            true
        }.getOrElse { clear(); false }
    }
    fun clear() {
        if (applied) runCatching { methods?.apply?.invoke(host, null) }
        host.setBackgroundColor(Color.TRANSPARENT)
        applied = false
    }
    private class Methods {
        private val info = Class.forName("android.view.SemBlurInfo")
        private val type = Class.forName("android.view.SemBlurInfo\$Builder")
        val builder = type.getConstructor(Int::class.javaPrimitiveType)
        val radius = type.getMethod("setRadius", Int::class.javaPrimitiveType)
        val color = type.getMethod("setBackgroundColor", Int::class.javaPrimitiveType)
        val corners = type.getMethod("setBackgroundCornerRadius",
            Float::class.javaPrimitiveType, Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType, Float::class.javaPrimitiveType)
        val build = type.getMethod("build")
        val apply = View::class.java.getMethod("semSetBlurInfo", info)
    }
    private companion object {
        val methods: Methods? by lazy {
            if (Build.VERSION.SDK_INT >= 31 && Build.MANUFACTURER.equals("samsung", true))
                runCatching { Methods() }.getOrNull() else null
        }
    }
}
