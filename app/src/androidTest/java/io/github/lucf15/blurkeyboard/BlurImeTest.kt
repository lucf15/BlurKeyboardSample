package io.github.lucf15.blurkeyboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlurImeTest {
    @Test fun setupTracksEnabledAndSelectedSteps() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val component = "io.github.lucf15.blurkeyboard/.ime.BlurImeService"
        val previous = device.executeShellCommand("settings get secure default_input_method").trim()
        val wasEnabled = device.executeShellCommand("settings get secure enabled_input_methods").contains(component)
        val files = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        try {
            device.executeShellCommand("ime disable $component")
            device.executeShellCommand("am start -W -n io.github.lucf15.blurkeyboard/.MainActivity")
            assertNotNull(device.wait(Until.findObject(By.text("Open settings")), 5000))
            assertNull(device.findObject(By.text("Type something")))
            device.takeScreenshot(java.io.File(files, "setup-enable.png"))
            device.executeShellCommand("ime enable $component")
            assertNotNull(device.wait(Until.findObject(By.desc("Step 1 complete")), 5000))
            assertNotNull(device.wait(Until.findObject(By.text("Enabled")), 5000))
            device.takeScreenshot(java.io.File(files, "setup-choose.png"))
            device.executeShellCommand("ime set $component")
            assertNotNull(device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000))
        } finally {
            if (!wasEnabled) device.executeShellCommand("ime disable $component")
            if (previous.contains('/')) device.executeShellCommand("ime set $previous")
        }
    }

    @Test fun typingModesAndReopeningWork() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val previous = device.executeShellCommand("settings get secure default_input_method").trim()
        fun node(description: String): UiObject2 = device.wait(Until.findObject(By.desc(description)), 5000)
            ?: error("Missing $description")
        try {
            device.executeShellCommand("ime enable io.github.lucf15.blurkeyboard/.ime.BlurImeService")
            device.executeShellCommand("ime set io.github.lucf15.blurkeyboard/.ime.BlurImeService")
            device.executeShellCommand("am start -W -n io.github.lucf15.blurkeyboard/.MainActivity")
            val editor = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000)!!
            editor.text = ""
            editor.click()
            node("Sample keyboard")
            "abc".forEach { node("Key $it").click() }
            assertEquals("Abc", device.findObject(By.clazz("android.widget.EditText")).text)
            node("Delete").click()
            node("Key d").click()
            assertEquals("Abd", device.findObject(By.clazz("android.widget.EditText")).text)
            repeat(3) { node("Delete").click() }
            node("Delete").click() // Empty editor stays valid.
            assertEquals("", device.findObject(By.clazz("android.widget.EditText")).text.orEmpty())
            node("Key e").click()
            assertEquals("e", device.findObject(By.clazz("android.widget.EditText")).text)
            device.pressBack()
            assertTrue(device.wait(Until.gone(By.desc("Sample keyboard")), 3000))
            device.findObject(By.clazz("android.widget.EditText")).click()
            node("Sample keyboard")
            node("Key f").click()
            assertEquals("ef", device.findObject(By.clazz("android.widget.EditText")).text)
            node("Suggestion Ciao").click()
            assertEquals("efCiao ", device.findObject(By.clazz("android.widget.EditText")).text)
            val files = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
            device.takeScreenshot(java.io.File(files, "playground.png"))
            device.pressBack()
            val slider = node("Blur")
            val bounds = slider.visibleBounds
            device.swipe(bounds.left + bounds.width() / 3, bounds.centerY(), bounds.right - 40, bounds.centerY(), 25)
            assertTrue(device.hasObject(By.desc("Tint")))
            val preferences = InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("demo", 0)
            for (control in listOf("Blur", "Tint")) {
                val track = node(control).visibleBounds
                device.click(track.left + 2, track.centerY())
                device.waitForIdle()
                if (control == "Blur") assertEquals(0, preferences.getInt("radius", -1))
                else assertEquals(0f, preferences.getFloat("tint", -1f), .001f)
                device.click(track.right - 2, track.centerY())
                device.waitForIdle()
                if (control == "Blur") assertEquals(40, preferences.getInt("radius", -1))
                else assertEquals(1f, preferences.getFloat("tint", -1f), .001f)
            }
            device.findObject(By.text("Reset")).click()
            device.waitForIdle()
            for (theme in listOf("Light", "Dark")) {
                device.findObject(UiSelector().text(theme)).click()
                device.waitForIdle()
                device.findObject(By.clazz("android.widget.EditText")).click()
                node("Sample keyboard")
                device.waitForIdle()
                device.takeScreenshot(java.io.File(files, "playground-${theme.lowercase()}.png"))
                device.pressBack()
            }
            device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4, device.displayWidth / 2, device.displayHeight / 3, 30)
            device.waitForIdle()
            device.takeScreenshot(java.io.File(files, "playground-canvas.png"))
            UiScrollable(UiSelector().scrollable(true)).scrollToBeginning(5)
            device.waitForIdle()
            device.findObject(By.text("Reset")).click()
            device.findObject(UiSelector().text("System")).click()
            device.findObject(By.clazz("android.widget.EditText")).click()
        } finally {
            if (previous.contains('/')) device.executeShellCommand("ime set $previous")
        }
    }
}
