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

import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Typing on the real keyboard, in a real text field: what the user sees, end to end.
 * Needs the phone unlocked, and leaves Guileless Bopomofo enabled as a keyboard.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ImeTypingUiAutomatorTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val driver = ImeDriver(device)
    private val dachen = SoftKeyboard.DACHEN

    @Before
    fun setUp() {
        // the defaults: Dachen keyboard, no IME switch key, Space opens the candidates
        instrumentation.targetContext.appSharedPreferences.edit().clear().commit()
        driver.takeOverInputMethod()
        driver.openTestField()
    }

    @After
    fun tearDown() {
        driver.closeTestField()
        driver.restoreInputMethod()
    }

    private fun Char.isHan(): Boolean = Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN

    @Test
    fun theDachenKeyboard_comesUpWithAllItsKeys() {
        assertEquals(driver.specKeys(dachen).size, driver.keys().size)
    }

    @Test
    fun bopomofo_showsInTheBuffer_andBackspaceTakesItBack() {
        driver.tapCharacter(dachen, "J") // ㄨ
        assertTrue("ㄨ should be in the Bopomofo buffer", driver.keyboardShows("ㄨ"))
        driver.tapFunction(dachen, FunctionKey.BACKSPACE)
        assertTrue("the buffer should be empty again", driver.keyboardNoLongerShows("ㄨ"))
        assertEquals("", driver.fieldText())
    }

    @Test
    fun aSyllable_convertsToAHanCharacter_andEnterCommitsIt() {
        driver.tapCharacter(dachen, "J") // ㄨ
        driver.tapCharacter(dachen, "I") // ㄛ
        driver.tapCharacter(dachen, "3") // ˇ
        // Which character libchewing picks depends on what it has learned, so only its kind is
        // checked: the Bopomofo is gone from the buffer, and one Han character comes out.
        assertTrue("the syllable should have been converted", driver.keyboardNoLongerShows("ㄨㄛˇ"))
        assertEquals("nothing is committed before Enter", "", driver.fieldText())
        driver.tapFunction(dachen, FunctionKey.ENTER)
        val committed = driver.waitForFieldLength(1)
        assertTrue("$committed should be a Han character", committed.single().isHan())
    }

    @Test
    fun space_opensTheCandidates_andChoosingOnePutsItInTheBuffer() {
        driver.tapCharacter(dachen, "J")
        driver.tapCharacter(dachen, "I")
        driver.tapCharacter(dachen, "3")
        driver.tapFunction(dachen, FunctionKey.SPACE)
        val candidates = driver.waitForCandidates()
        assertTrue("there should be more than one candidate for ㄨㄛˇ", candidates.size > 1)
        // the second one, so that the choice is seen to replace what was converted first
        val chosen = candidates[1]
        val chosenText = requireNotNull(chosen.findObject(By.clazz(TextView::class.java))?.text) { "a candidate without text" }
        chosen.click()
        device.waitForIdle()
        // the candidates are gone, the keys are back, and the choice is in the buffer
        assertEquals(driver.specKeys(dachen).size, driver.keys().size)
        assertTrue(driver.keyboardShows(chosenText))
        driver.tapFunction(dachen, FunctionKey.ENTER)
        driver.waitForFieldText(chosenText)
    }

    @Test
    fun theModeKey_switchesToEnglish_whichTypesStraightIntoTheField() {
        driver.tapFunction(dachen, FunctionKey.MODE_SWITCH_TO_ENGLISH)
        device.waitForIdle()
        assertEquals(driver.specKeys(SoftKeyboard.QWERTY).size, driver.keys().size)
        driver.tapCharacter(SoftKeyboard.QWERTY, "A")
        driver.waitForFieldText("a")
        // and back again
        driver.tapFunction(SoftKeyboard.QWERTY, FunctionKey.MODE_SWITCH_TO_BOPOMOFO)
        device.waitForIdle()
        assertEquals(driver.specKeys(dachen).size, driver.keys().size)
    }
}
