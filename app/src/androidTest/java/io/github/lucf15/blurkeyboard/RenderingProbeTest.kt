package io.github.lucf15.blurkeyboard

import android.os.Build
import org.junit.Assume.assumeTrue
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RenderingProbeTest {
    @Test fun slidersAndScrollWithKeyboardVisible() {
        assumeTrue("Rendering probe is emulator-only", Build.HARDWARE in listOf("ranchu", "goldfish"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val previous = device.executeShellCommand("settings get secure default_input_method").trim()
        val component = "io.github.lucf15.blurkeyboard/.ime.BlurImeService"
        val wasEnabled = device.executeShellCommand("settings get secure enabled_input_methods").contains(component)
        val preferences = instrumentation.targetContext.getSharedPreferences("demo", 0)
        val radius = preferences.getInt("radius", 22)
        val tint = preferences.getFloat("tint", .52f)
        val output = instrumentation.targetContext.getExternalFilesDir(null)
        fun sample(name: String, action: () -> Unit) {
            device.executeShellCommand("dumpsys gfxinfo io.github.lucf15.blurkeyboard reset")
            action()
            device.waitForIdle()
            File(output, "frames-$name.txt").writeText(device.executeShellCommand("dumpsys gfxinfo io.github.lucf15.blurkeyboard framestats"))
        }
        try {
            device.executeShellCommand("ime enable $component")
            device.executeShellCommand("ime set $component")
            device.executeShellCommand("am start -W -n io.github.lucf15.blurkeyboard/.MainActivity")
            device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000)!!.click()
            check(device.wait(Until.hasObject(By.desc("Sample keyboard")), 5000))
            val blur = device.wait(Until.findObject(By.desc("Blur")), 5000)!!.visibleBounds
            val tintTrack = device.findObject(By.desc("Tint")).visibleBounds
            check(tintTrack.bottom < device.findObject(By.desc("Sample keyboard")).visibleBounds.top) { "Sliders must be visible above the keyboard" }
            repeat(2) {
                device.swipe(blur.left + 30, blur.centerY(), blur.right - 30, blur.centerY(), 40)
                device.swipe(blur.right - 30, blur.centerY(), blur.left + 30, blur.centerY(), 40)
            }
            sample("sliders") {
                repeat(6) {
                    for (track in listOf(blur, tintTrack)) {
                        device.swipe(track.left + 30, track.centerY(), track.right - 30, track.centerY(), 40)
                        device.swipe(track.right - 30, track.centerY(), track.left + 30, track.centerY(), 40)
                    }
                }
            }
            val top = device.findObject(By.clazz("android.widget.EditText")).visibleBounds.bottom + 40
            val bottom = device.findObject(By.desc("Sample keyboard")).visibleBounds.top - 40
            sample("scroll") {
                repeat(10) {
                    device.swipe(device.displayWidth / 2, bottom, device.displayWidth / 2, top, 30)
                    device.swipe(device.displayWidth / 2, top, device.displayWidth / 2, bottom, 30)
                }
            }
        } finally {
            preferences.edit().putInt("radius", radius).putFloat("tint", tint).apply()
            if (previous.contains('/')) device.executeShellCommand("ime set $previous")
            if (!wasEnabled) device.executeShellCommand("ime disable $component")
        }
    }
}
