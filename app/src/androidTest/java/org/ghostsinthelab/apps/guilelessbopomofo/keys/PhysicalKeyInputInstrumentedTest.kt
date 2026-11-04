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
package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.inputmethod.InputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingTestSetup
import org.ghostsinthelab.apps.guilelessbopomofo.ChiEngMode
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardController
import org.ghostsinthelab.apps.guilelessbopomofo.RecordingImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.RecordingInputConnection
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyInput
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalKeyInputInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private class FakeEditor(private val textField: RecordingInputConnection) : PhysicalKeyInput.Editor {
        override var isInputViewShown = true
        override val inputConnection: InputConnection get() = textField.connection
        var hidden = false
        var keyboardDetected = false
        var pickerShown = false
        val sentToField = mutableListOf<KeyEvent>()
        override fun hideKeyboard() { hidden = true }
        override fun physicalKeyboardDetected() { keyboardDetected = true }
        override fun showInputMethodPicker() { pickerShown = true }
        override fun sendKeyEventToField(event: KeyEvent) { sentToField += event }
    }

    private val host = RecordingImeHost(appContext)
    private val field = RecordingInputConnection()
    private val editor = FakeEditor(field)
    private val uiState = ImeUiState()
    private val physicalKeyboard = PhysicalKeyboard()
    private lateinit var keyboardController: KeyboardController
    private lateinit var input: PhysicalKeyInput

    @Before
    fun setUp() {
        ChewingTestSetup.connect(appContext)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        val preferences = appContext.getSharedPreferences(APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        keyboardController = KeyboardController(uiState, preferences, physicalKeyboard, host)
        input = PhysicalKeyInput(host, uiState, keyboardController, physicalKeyboard, editor)
    }

    @After
    fun tearDown() {
        ChewingBridge.chewing.delete()
    }

    private fun down(keyCode: Int, metaState: Int = 0) = KeyEvent(0, 0, ACTION_DOWN, keyCode, 0, metaState)
    private fun up(keyCode: Int) = KeyEvent(0, 0, ACTION_UP, keyCode, 0, 0)
    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)

    @Test
    fun aPrintingKey_typesIntoLibchewing_andAsksForTheBuffersToBeShown() {
        var handled = false
        onMain { handled = input.onKeyDown(KeyEvent.KEYCODE_J, down(KeyEvent.KEYCODE_J)) }
        assertTrue(handled)
        // 'j' is ㄨ on the Dachen layout, and sits in the Bopomofo buffer now
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty())
        assertEquals(
            listOf<ImeAction>(ImeAction.UpdateBufferViews, ImeAction.UpdateCursorPosition),
            host.actions.filter { it is ImeAction.UpdateBufferViews || it is ImeAction.UpdateCursorPosition },
        )
    }

    @Test
    fun theFirstPrintingKey_tellsOfThePhysicalKeyboard() {
        assertFalse(physicalKeyboard.isPresent)
        onMain { input.onKeyDown(KeyEvent.KEYCODE_J, down(KeyEvent.KEYCODE_J)) }
        assertTrue(physicalKeyboard.isPresent)
        assertTrue(editor.keyboardDetected)
    }

    @Test
    fun withThePhysicalKeyboardPresent_aPrintingKeyBringsUpTheCompactLayout() {
        physicalKeyboard.isPresent = true
        onMain {
            keyboardController.switchToLayout(Layout.MAIN)
            input.onKeyDown(KeyEvent.KEYCODE_J, down(KeyEvent.KEYCODE_J))
        }
        assertEquals(Layout.COMPACT, keyboardController.currentLayout)
    }

    @Test
    fun back_hidesTheKeyboard_andIsLeftToAndroid() {
        var handled = true
        onMain { handled = input.onKeyDown(KeyEvent.KEYCODE_BACK, down(KeyEvent.KEYCODE_BACK)) }
        assertFalse(handled)
        assertTrue(editor.hidden)
    }

    @Test
    fun whileTheInputViewIsHidden_nothingIsTaken() {
        editor.isInputViewShown = false
        var handled = true
        onMain { handled = input.onKeyDown(KeyEvent.KEYCODE_J, down(KeyEvent.KEYCODE_J)) }
        assertFalse(handled)
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
        assertEquals(emptyList<ImeAction>(), host.actions)
    }

    @Test
    fun aFunctionKeyWithAHandler_isTaken() {
        var handled = false
        onMain { handled = input.onKeyDown(KeyEvent.KEYCODE_DEL, down(KeyEvent.KEYCODE_DEL)) }
        assertTrue(handled)
        // nothing in the buffers, so Backspace goes to the text field
        assertEquals(listOf<ImeAction>(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)), host.actions)
    }

    @Test
    fun aKeyNobodyAnswers_isPassedToTheTextField() {
        var handled = true
        onMain { handled = input.onKeyDown(KeyEvent.KEYCODE_F1, down(KeyEvent.KEYCODE_F1)) }
        assertFalse(handled)
        assertEquals(listOf(KeyEvent.KEYCODE_F1), field.sentKeyEvents.map { it.keyCode })
    }

    @Test
    fun ctrlA_selectsAll_insteadOfTyping() {
        onMain { input.onKeyDown(KeyEvent.KEYCODE_A, down(KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON)) }
        assertEquals(listOf(android.R.id.selectAll), field.contextMenuActions)
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
    }

    @Test
    fun altI_showsTheInputMethodPicker() {
        onMain { input.onKeyDown(KeyEvent.KEYCODE_I, down(KeyEvent.KEYCODE_I, KeyEvent.META_ALT_ON)) }
        assertTrue(editor.pickerShown)
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
    }

    @Test
    fun aNumpadKey_goesStraightToTheTextField() {
        onMain { input.onKeyDown(KeyEvent.KEYCODE_NUMPAD_5, down(KeyEvent.KEYCODE_NUMPAD_5)) }
        assertEquals(listOf(KeyEvent.KEYCODE_NUMPAD_5), editor.sentToField.map { it.keyCode })
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
    }

    @Test
    fun graveInChineseMode_opensTheSymbols() {
        onMain { input.onKeyDown(KeyEvent.KEYCODE_GRAVE, down(KeyEvent.KEYCODE_GRAVE)) }
        // the symbol picker shows its symbols as candidates
        assertTrue(keyboardController.currentLayout == Layout.SYMBOLS || keyboardController.currentLayout == Layout.CANDIDATES)
        assertFalse(keyboardController.isDismissible)
    }

    @Test
    fun aPrintingKeyUp_isTaken_butBackIsNot() {
        var printing = false
        var back = true
        onMain {
            printing = input.onKeyUp(KeyEvent.KEYCODE_J, up(KeyEvent.KEYCODE_J))
            back = input.onKeyUp(KeyEvent.KEYCODE_BACK, up(KeyEvent.KEYCODE_BACK))
        }
        assertTrue(printing)
        assertFalse(back)
    }

    @Test
    fun releaseShiftKey_letsGoOfTheHeldShiftInTheTextField() {
        onMain { input.releaseShiftKey() }
        assertEquals(listOf(KeyEvent.KEYCODE_SHIFT_LEFT to ACTION_UP), field.sentKeyEvents.map { it.keyCode to it.action })
    }
}
