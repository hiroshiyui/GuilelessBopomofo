/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE_NAME = "org.ghostsinthelab.apps.guilelessbopomofo"
private const val LAUNCH_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
@LargeTest
class GuilelessBopomofoServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var uiDevice: UiDevice
    private lateinit var originalImeId: String

    @Before
    fun setUp() {
        Log.d("TestSetup", "setUp started.")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        uiDevice = UiDevice.getInstance(instrumentation)
        val context = ApplicationProvider.getApplicationContext<Context>()

        originalImeId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        Log.d("TestSetup", "Original IME ID: $originalImeId")

        // launch Guileless Bopomofo App (Settings), which will let you do enable the IME first.
        val intent = context.packageManager.getLaunchIntentForPackage(
            PACKAGE_NAME).apply {
            this!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        uiDevice.wait(
            Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )

        // time to wait the initial manual operations been completed
        Thread.sleep(10000)

        // enable Guileless Bopomofo IME
        val myImeId = "${context.packageName}/.GuilelessBopomofoService"
        setSystemIme(myImeId)
        Log.d("TestSetup", "System IME set to: $myImeId")

        uiDevice.pressHome()
        Thread.sleep(5000)

        Log.d("TestSetup", "setUp finished.")
    }

    private fun setSystemIme(imeId: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("ime set $imeId")
        Thread.sleep(2000)
    }

    @Test
    fun testServiceStartsAndKeyboardAppears() {
        uiDevice.pressSearch()
        Thread.sleep(2000) // Wait for UI

        // Check if your keyboard view is visible using UI Automator
        // You'll need resource IDs or descriptive text for your keyboard elements
        val keyboardView = uiDevice.wait(
            Until.hasObject(
                By.res(
                    ApplicationProvider.getApplicationContext<Context>().packageName,
                    "keyboardView"
                )
            ), 5000
        )
        assert(keyboardView != null) { "Keyboard panel did not appear." }
    }

    // More tests:
    // - Test input bopomofo symbols and characters
    // - Test switching layouts (e.g., to symbols, to English)
    // - Test candidate selection
    // - Test preference changes (e.g., toggling compact layout via Escape+Alt)
    // - Test physical keyboard interactions if possible (though harder to simulate exact physical events)

    @After
    fun tearDown() {
        Log.d("TestTeardown", "tearDown started.")
        // Restore original IME only if it was successfully captured
        if (this::originalImeId.isInitialized) {
            Log.d("TestTeardown", "originalImeId is initialized. Restoring IME to: $originalImeId")
            setSystemIme(originalImeId)
        } else {
            Log.w("TestTeardown", "originalImeId was NOT initialized. Skipping IME restore.")
        }
        Log.d("TestTeardown", "tearDown finished.")
    }
}