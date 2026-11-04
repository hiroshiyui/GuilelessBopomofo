/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.ghostsinthelab.apps.guilelessbopomofo.ui

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.MainActivity
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The settings screens, as the user walks through them. */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsUiAutomatorTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val preferences get() = context.appSharedPreferences

    private fun string(id: Int) = context.getString(id)

    @Before
    fun setUp() {
        preferences.edit().clear().commit()
        context.startActivity(
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        assertNotNull(device.wait(Until.findObject(By.text(string(R.string.general_settings_label))), LAUNCH_TIMEOUT_MS))
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
        device.pressBack()
    }

    private fun openTab(title: Int) {
        // The tab's label is the last match, the screen's own header carries the same words,
        // and it is clipped by the navigation bar, so the item around it takes the tap.
        val label = device.findObjects(By.text(string(title))).last()
        (label.parent ?: label).click()
        device.waitForIdle()
    }

    private fun scrollTo(label: Int): UiObject2 {
        val text = string(label)
        device.findObject(By.scrollable(true))?.scrollUntil(Direction.DOWN, Until.findObject(By.text(text)))
        return requireNotNull(device.wait(Until.findObject(By.text(text)), ACTION_TIMEOUT_MS)) { "no switch labelled $text" }
    }

    private fun toggle(label: Int) {
        val text = scrollTo(label)
        // the row around the label is the checkable, or holds the switch that is
        val row = text.parent
        val switch = when {
            row == null -> text
            row.isCheckable -> row
            else -> row.findObject(By.checkable(true)) ?: row
        }
        switch.click()
        device.waitForIdle()
    }

    private fun waitForPreference(key: String, expected: Boolean): Boolean {
        val deadline = System.currentTimeMillis() + ACTION_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (preferences.getBoolean(key, !expected) == expected) return true
            Thread.sleep(100)
        }
        return false
    }

    @Test
    fun theTabs_openTheirScreens() {
        openTab(R.string.user_interface_settings)
        assertNotNull(scrollTo(R.string.switch_setting_enable_ime_switch))
        openTab(R.string.physical_keyboard_settings)
        assertTrue(device.wait(Until.hasObject(By.text(string(R.string.physical_keyboard_settings))), ACTION_TIMEOUT_MS))
        openTab(R.string.general_settings_label)
        assertNotNull(device.wait(Until.findObject(By.text(string(R.string.bopomofo_keyboard_layout))), ACTION_TIMEOUT_MS))
    }

    @Test
    fun aSwitch_writesItsPreference_bothWays() {
        openTab(R.string.user_interface_settings)
        assertFalse(preferences.getBoolean(USER_ENABLE_IME_SWITCH, false))
        toggle(R.string.switch_setting_enable_ime_switch)
        assertTrue("turning the switch on should store true", waitForPreference(USER_ENABLE_IME_SWITCH, true))
        toggle(R.string.switch_setting_enable_ime_switch)
        assertTrue("turning it off again should store false", waitForPreference(USER_ENABLE_IME_SWITCH, false))
    }

    companion object {
        private const val LAUNCH_TIMEOUT_MS = 5000L
        private const val ACTION_TIMEOUT_MS = 3000L
    }
}
