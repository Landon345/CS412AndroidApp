
package com.example.intentsapp

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class MainToSecondActivityTest {

    companion object {
        // Generous because SecondActivity forces a rotation to landscape on open.
        private const val TIMEOUT_MS = 10_000L
        private const val EXPECTED_CHALLENGE = "Device Fragmentation"
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device: UiDevice = UiDevice.getInstance(instrumentation)
    private val context = instrumentation.targetContext
    private val appPackage = context.packageName
    private val appLabel = context.applicationInfo.loadLabel(context.packageManager).toString()

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            device.executeShellCommand("pm grant $appPackage android.permission.POST_NOTIFICATIONS")
        }
        // No "am force-stop" here: the test runs inside the app's process, so
        // force-stopping the app would kill the test too.
        device.pressHome()
        device.wait(Until.hasObject(By.pkg(device.launcherPackageName).depth(0)), TIMEOUT_MS)
    }

    @Test
    fun launchFromHome_clickStartActivityExplicitly_showsChallenge() {
        // Step 1: launch the app by clicking its launcher icon.
        val icon = checkNotNull(findLauncherIcon()) { "Launcher icon '$appLabel' not found" }
        icon.click()
        check(device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), TIMEOUT_MS)) {
            "App did not open after clicking the launcher icon"
        }

        // Step 2: go to the second activity.
        // Pattern ignores case in case the button style shows its text in ALL CAPS.
        val button = checkNotNull(
            device.wait(
                Until.findObject(
                    By.text(Pattern.compile("Start Activity Explicitly", Pattern.CASE_INSENSITIVE))
                ),
                TIMEOUT_MS
            )
        ) { "'Start Activity Explicitly' button not found" }
        button.click()

        // Step 3: the second activity shows at least one challenge.
        val challenge = device.wait(Until.findObject(By.textContains(EXPECTED_CHALLENGE)), TIMEOUT_MS)
        assertNotNull("Second activity should display a mobile software engineering challenge", challenge)
    }

    /** Looks on the home screen first, then opens the app drawer by swiping up. */
    private fun findLauncherIcon(): UiObject2? {
        device.findObject(By.text(appLabel))?.let { return it }
        device.findObject(By.desc(appLabel))?.let { return it }

        device.swipe(
            device.displayWidth / 2, device.displayHeight * 4 / 5,
            device.displayWidth / 2, device.displayHeight / 5,
            20
        )
        return device.wait(Until.findObject(By.text(appLabel)), TIMEOUT_MS)
            ?: device.findObject(By.desc(appLabel))
    }
}