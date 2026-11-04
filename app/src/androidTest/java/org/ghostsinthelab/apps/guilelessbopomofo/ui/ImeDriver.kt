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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.EditText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.EngineeringModeActivity
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.CANDIDATE_TEST_TAG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.IME_ROOT_TEST_TAG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.KEY_TEST_TAG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeySpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardSpecs
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardVariant
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Drives the real input method with UI Automator. The keys show icons, not text, so a key
 * is found by its place in the keyboard spec: the keys come back from the accessibility
 * tree in the order they are laid out, row by row.
 */
class ImeDriver(private val device: UiDevice) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val imeId = "${context.packageName}/.${GuilelessBopomofoService::class.simpleName}"
    private var originalImeId: String? = null

    /** Makes Guileless Bopomofo the current keyboard, remembering the one it replaces. */
    fun takeOverInputMethod() {
        originalImeId = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        device.executeShellCommand("ime enable $imeId")
        device.executeShellCommand("ime set $imeId")
        Thread.sleep(IME_SWITCH_SETTLE_MS)
    }

    fun restoreInputMethod() {
        originalImeId?.let { device.executeShellCommand("ime set $it") }
    }

    /** Opens the engineering screen, whose first text field takes the focus and the keyboard. */
    fun openTestField() {
        context.startActivity(
            Intent(context, EngineeringModeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        assertNotNull("The test field did not appear", device.wait(Until.findObject(By.clazz(EditText::class.java)), LAUNCH_TIMEOUT_MS))
        assertNotNull("The keyboard did not appear", device.wait(Until.findObject(By.res(IME_ROOT_TEST_TAG)), LAUNCH_TIMEOUT_MS))
        // The service shares the one libchewing context with every other test in this
        // process, and some of them delete it from under it. If that has happened, a fresh
        // context, which the service is told about as the settings screens would tell it,
        // puts it back in Bopomofo mode. The service is up by now, so this never leaves a
        // context behind for it to connect over.
        if (ChewingBridge.chewing.context == 0L) {
            instrumentation.runOnMainSync { ChewingUtil.flushContext(context) }
            device.waitForIdle()
        }
    }

    fun closeTestField() {
        device.pressBack() // the keyboard
        device.pressBack() // the screen
    }

    /** What the focused test field holds. */
    fun fieldText(): String = device.findObjects(By.clazz(EditText::class.java)).first().text.orEmpty()

    /** Waits for the field to hold [length] characters and returns them. */
    fun waitForFieldLength(length: Int): String {
        val deadline = System.currentTimeMillis() + ACTION_TIMEOUT_MS
        while (fieldText().length != length && System.currentTimeMillis() < deadline) Thread.sleep(100)
        val text = fieldText()
        assertEquals("the field should hold $length character(s), has '$text'", length, text.length)
        return text
    }

    fun waitForFieldText(expected: String) {
        device.wait(Until.hasObject(By.clazz(EditText::class.java).text(expected)), ACTION_TIMEOUT_MS)
        assertEquals(expected, fieldText())
    }

    fun keyboardView(): UiObject2 =
        requireNotNull(device.wait(Until.findObject(By.res(IME_ROOT_TEST_TAG)), ACTION_TIMEOUT_MS)) { "no keyboard view" }

    fun keyboardShows(text: String): Boolean =
        device.wait(Until.hasObject(By.res(IME_ROOT_TEST_TAG).hasDescendant(By.text(text))), ACTION_TIMEOUT_MS) == true

    fun keyboardNoLongerShows(text: String): Boolean =
        device.wait(Until.gone(By.res(IME_ROOT_TEST_TAG).hasDescendant(By.text(text))), ACTION_TIMEOUT_MS) == true

    /** The keys on screen, in layout order. */
    fun keys(): List<UiObject2> = keyboardView().findObjects(By.res(KEY_TEST_TAG))

    /** The keys [keyboard] is drawn with, in the same order, the IME switch key left out as it is by default. */
    fun specKeys(keyboard: SoftKeyboard): List<KeySpec> =
        requireNotNull(KeyboardSpecs.of(keyboard, KeyboardVariant.DEFAULT)).rows.flatten()
            .filterNot { it is KeySpec.Function && it.key == FunctionKey.IME_SWITCH }

    fun tapCharacter(keyboard: SoftKeyboard, keyCode: String) =
        tapKeyAt(keyboard, specKeys(keyboard).indexOfFirst { it is KeySpec.Character && it.keyCode == "KEYCODE_$keyCode" })

    fun tapFunction(keyboard: SoftKeyboard, key: FunctionKey) =
        tapKeyAt(keyboard, specKeys(keyboard).indexOfFirst { it is KeySpec.Function && it.key == key })

    private fun tapKeyAt(keyboard: SoftKeyboard, index: Int) {
        require(index >= 0) { "no such key on $keyboard" }
        val keys = keys()
        assertEquals("the keys on screen are not the $keyboard keyboard's", specKeys(keyboard).size, keys.size)
        keys[index].click()
        device.waitForIdle()
    }

    /** The candidates on screen, once there are any. */
    fun waitForCandidates(): List<UiObject2> {
        device.wait(Until.hasObject(By.res(CANDIDATE_TEST_TAG)), ACTION_TIMEOUT_MS)
        return keyboardView().findObjects(By.res(CANDIDATE_TEST_TAG))
    }

    companion object {
        private const val LAUNCH_TIMEOUT_MS = 5000L
        private const val ACTION_TIMEOUT_MS = 3000L
        private const val IME_SWITCH_SETTLE_MS = 1500L
    }
}
