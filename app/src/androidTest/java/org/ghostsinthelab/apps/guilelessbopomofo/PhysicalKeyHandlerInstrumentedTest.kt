/*
 * Guileless Bopomofo
 * Copyright (C) 2024 YOU, HUI-HONG
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

import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.CapsLock
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Del
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Enter
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Escape
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Left
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.LeftAlt
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyHandler
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Right
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Shift
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Space
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalKeyHandlerInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataDir = File(dataPath)
    private val chewingDataFiles =
        listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    private val host = RecordingImeHost(appContext)

    @Before
    fun setupChewingEngine() {
        // Copy Chewing data files to data directory
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d("PhysicalKeyHandlerTest", "Copying ${file}...")
            val dataInputStream = appContext.assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e("PhysicalKeyHandlerTest", it)
                }
            } finally {
                Log.d("PhysicalKeyHandlerTest", "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }

        // Initialize Chewing
        ChewingBridge.chewing.connect(dataPath)

    }

    @After
    fun tearDown() {
        // Close Chewing
        ChewingBridge.chewing.delete()
    }

    private fun clearEventLists() {
        host.actions.clear()
    }

    /**
     * Type bopomofo keys into the chewing engine to populate the buffer.
     * Uses Dachen layout: 'j' = ㄗ, '3' = ˇ (third tone), etc.
     * For example, typing "su3" produces ㄦˇ in the bopomofo buffer.
     */
    private fun typeBopomofoKeys(keys: String) {
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
    }

    // ========== Del tests ==========

    @Test
    fun del_onKeyDown_withBufferContent_returnsTrue() {
        val del = Del()
        // Type a bopomofo key to populate the buffer
        typeBopomofoKeys("j")
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyDown(host, KeyEvent.KEYCODE_DEL, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.UpdateBufferViews>().size)
    }

    @Test
    fun del_onKeyDown_withEmptyBuffer_postsKeyEvent() {
        val del = Del()
        // Ensure buffer is empty
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyDown(host, KeyEvent.KEYCODE_DEL, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.SendDownUpKeyEvents>().size)
        assertEquals(KeyEvent.KEYCODE_DEL, host.received<ImeAction.SendDownUpKeyEvents>()[0].keycode)
    }

    // ========== Enter tests ==========

    @Test
    fun enter_onKeyDown_withBufferContent_commitsAndPostsUpdate() {
        val enter = Enter()
        // Type bopomofo to populate buffer: 'j' = ㄗ, 'i' = ㄛ, '4' = ˋ
        typeBopomofoKeys("ji4")
        assertTrue(
            ChewingBridge.chewing.bufferStringStatic().isNotEmpty() ||
                    ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty()
        )

        clearEventLists()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        val result = enter.onKeyDown(host, KeyEvent.KEYCODE_ENTER, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.UpdateBufferViews>().size)
    }

    @Test
    fun enter_onKeyDown_withEmptyBuffer_postsEnterKeyDownWhenBufferIsEmpty() {
        val enter = Enter()
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        val result = enter.onKeyDown(host, KeyEvent.KEYCODE_ENTER, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.EnterKeyDownWhenBufferIsEmpty>().size)
    }

    // ========== Space tests ==========

    @Test
    fun space_onKeyDown_withBufferContent_handlesSpace() {
        val space = Space()
        // Type bopomofo to populate buffer
        typeBopomofoKeys("j")
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty())

        clearEventLists()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)
        val result = space.onKeyDown(host, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
    }

    @Test
    fun space_onKeyDown_withEmptyBuffer_postsSendDownUpKeyEvents() {
        val space = Space()
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)
        val result = space.onKeyDown(host, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.SendDownUpKeyEvents>().size)
        assertEquals(KeyEvent.KEYCODE_SPACE, host.received<ImeAction.SendDownUpKeyEvents>()[0].keycode)
    }

    @Test
    fun space_onKeyDown_withShiftPressed_postsToggleKeyboardMainLayoutMode() {
        val space = Space()
        // Create a KeyEvent with Shift pressed
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, 0,
            KeyEvent.META_SHIFT_ON
        )

        val result = space.onKeyDown(host, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun space_onKeyDown_withAltPressed_postsToggleFullOrHalfWidthMode() {
        val space = Space()
        // Create a KeyEvent with Alt pressed
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, 0,
            KeyEvent.META_ALT_ON
        )

        val result = space.onKeyDown(host, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.ToggleFullOrHalfWidthMode>().size)
    }

    // ========== Left tests ==========

    @Test
    fun left_onKeyDown_plainLeft_postsDirectionKeyDown() {
        val left = Left()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)
        val result = left.onKeyDown(host, KeyEvent.KEYCODE_DPAD_LEFT, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.DirectionKeyDown>().size)
        assertEquals(DirectionKey.LEFT, host.received<ImeAction.DirectionKeyDown>()[0].direction)
    }

    @Test
    fun left_onKeyDown_withCtrlPressed_postsUpdateCursorPosition() {
        val left = Left()
        // Create a KeyEvent with Ctrl pressed
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0,
            KeyEvent.META_CTRL_ON
        )

        val result = left.onKeyDown(host, KeyEvent.KEYCODE_DPAD_LEFT, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.UpdateCursorPosition>().size)
        // Should NOT post DirectionKeyDown when Ctrl is pressed
        assertEquals(0, host.received<ImeAction.DirectionKeyDown>().size)
    }

    // ========== Right tests ==========

    @Test
    fun right_onKeyDown_plainRight_postsDirectionKeyDown() {
        val right = Right()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
        val result = right.onKeyDown(host, KeyEvent.KEYCODE_DPAD_RIGHT, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.DirectionKeyDown>().size)
        assertEquals(DirectionKey.RIGHT, host.received<ImeAction.DirectionKeyDown>()[0].direction)
    }

    @Test
    fun right_onKeyDown_withCtrlPressed_postsUpdateCursorPosition() {
        val right = Right()
        // Create a KeyEvent with Ctrl pressed
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0,
            KeyEvent.META_CTRL_ON
        )

        val result = right.onKeyDown(host, KeyEvent.KEYCODE_DPAD_RIGHT, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.UpdateCursorPosition>().size)
        assertEquals(0, host.received<ImeAction.DirectionKeyDown>().size)
    }

    // ========== Escape tests ==========

    @Test
    fun escape_onKeyDown_postsRequestHideIme() {
        val escape = Escape()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE)
        val result = escape.onKeyDown(host, KeyEvent.KEYCODE_ESCAPE, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.RequestHideIme>().size)
        assertEquals(1, host.received<ImeAction.ExitKeyboardSubLayouts>().size)
        assertEquals(1, host.received<ImeAction.UpdateCursorPosition>().size)
    }

    // ========== Shift tests ==========

    @Test
    fun shift_rightKey_onKeyDown_returnsTrue() {
        val shift = Shift()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = shift.onKeyDown(host, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertTrue(result)
    }

    @Test
    fun shift_leftKey_passesThroughToApp() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val left = KeyEvent.KEYCODE_SHIFT_LEFT

        assertFalse(shift.onKeyDown(host, left, shiftEvent(KeyEvent.ACTION_DOWN, left, downTime, downTime)))
        assertFalse(shift.onKeyUp(host, left, shiftEvent(KeyEvent.ACTION_UP, left, downTime, downTime + 80)))
        // passing it on doesn't stop the tap from counting
        assertEquals(1, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_rightKey_onKeyLongPress_inChineseMode_postsEvents() {
        val shift = Shift()
        // Ensure we are in Chinese mode
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = shift.onKeyLongPress(host, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertTrue(result)
        assertEquals(1, host.received<ImeAction.SwitchToLayout>().size)
    }

    @Test
    fun shift_rightKey_onKeyLongPress_inSymbolMode_returnsFalse() {
        val shift = Shift()
        // Switch to symbol (English) mode
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = shift.onKeyLongPress(host, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertFalse(result)
        assertEquals(0, host.received<ImeAction.SwitchToLayout>().size)
    }

    private fun shiftEvent(action: Int, keyCode: Int, downTime: Long, eventTime: Long) =
        KeyEvent(downTime, eventTime, action, keyCode, 0)

    private fun tapShift(shift: Shift, keyCode: Int, heldMs: Long, between: () -> Unit = {}) {
        val downTime = SystemClock.uptimeMillis()
        shift.onKeyDown(host, keyCode, shiftEvent(KeyEvent.ACTION_DOWN, keyCode, downTime, downTime))
        between()
        shift.onKeyUp(host, keyCode, shiftEvent(KeyEvent.ACTION_UP, keyCode, downTime, downTime + heldMs))
    }

    @Test
    fun shift_quickTap_eitherKey_togglesMainLayoutMode() {
        val shift = Shift()
        tapShift(shift, KeyEvent.KEYCODE_SHIFT_LEFT, heldMs = 80)
        tapShift(shift, KeyEvent.KEYCODE_SHIFT_RIGHT, heldMs = 80)

        assertEquals(2, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_heldTooLong_doesNotToggle() {
        val shift = Shift()
        tapShift(shift, KeyEvent.KEYCODE_SHIFT_LEFT, heldMs = Shift.TAP_TIMEOUT_MS + 1)

        assertEquals(0, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_repeatWhileHeld_doesNotToggle() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val left = KeyEvent.KEYCODE_SHIFT_LEFT
        val down = shiftEvent(KeyEvent.ACTION_DOWN, left, downTime, downTime)
        shift.onKeyDown(host, left, down)
        shift.onKeyDown(host, left, KeyEvent.changeTimeRepeat(down, downTime + 400, 1))
        // a key-up stamped with the key-down's time, as `adb shell input --longpress` sends it
        shift.onKeyUp(host, left, shiftEvent(KeyEvent.ACTION_UP, left, downTime, downTime))

        assertEquals(0, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_chordWithAnotherKey_doesNotToggle() {
        val shift = Shift()
        tapShift(shift, KeyEvent.KEYCODE_SHIFT_LEFT, heldMs = 80) { shift.onOtherInput() }

        assertEquals(0, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_bothShiftKeysTogether_doesNotToggle() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val left = KeyEvent.KEYCODE_SHIFT_LEFT
        val right = KeyEvent.KEYCODE_SHIFT_RIGHT
        shift.onKeyDown(host, left, shiftEvent(KeyEvent.ACTION_DOWN, left, downTime, downTime))
        shift.onKeyDown(host, right, shiftEvent(KeyEvent.ACTION_DOWN, right, downTime + 10, downTime + 10))
        shift.onKeyUp(host, left, shiftEvent(KeyEvent.ACTION_UP, left, downTime, downTime + 50))
        shift.onKeyUp(host, right, shiftEvent(KeyEvent.ACTION_UP, right, downTime + 10, downTime + 60))

        assertEquals(0, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_tapWhileCtrlHeld_doesNotToggle() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val keyCode = KeyEvent.KEYCODE_SHIFT_LEFT
        val ctrl = KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        shift.onKeyDown(host, keyCode, KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, ctrl))
        shift.onKeyUp(host, keyCode, KeyEvent(downTime, downTime + 80, KeyEvent.ACTION_UP, keyCode, 0, ctrl))

        assertEquals(0, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_tapWithCapsLockOn_toggles() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val keyCode = KeyEvent.KEYCODE_SHIFT_LEFT
        val capsLock = KeyEvent.META_CAPS_LOCK_ON
        shift.onKeyDown(host, keyCode, KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, capsLock))
        shift.onKeyUp(host, keyCode, KeyEvent(downTime, downTime + 80, KeyEvent.ACTION_UP, keyCode, 0, capsLock))

        assertEquals(1, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_afterReset_otherShiftTapToggles() {
        val shift = Shift()
        val downTime = SystemClock.uptimeMillis()
        val left = KeyEvent.KEYCODE_SHIFT_LEFT
        // Left Shift goes down, then the input view closes before its key-up arrives
        shift.onKeyDown(host, left, shiftEvent(KeyEvent.ACTION_DOWN, left, downTime, downTime))
        shift.reset()
        tapShift(shift, KeyEvent.KEYCODE_SHIFT_RIGHT, heldMs = 80)

        assertEquals(1, host.received<ImeAction.ToggleKeyboardMainLayoutMode>().size)
    }

    @Test
    fun shift_onKeyLongPress_leftShift_returnsFalse() {
        val shift = Shift()
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT)
        val result = shift.onKeyLongPress(host, KeyEvent.KEYCODE_SHIFT_LEFT, event)

        assertFalse(result)
        assertEquals(0, host.received<ImeAction.SwitchToLayout>().size)
    }

    // ========== CapsLock tests ==========

    @Test
    fun capsLock_onKeyDown_returnsTrue() {
        val capsLock = CapsLock()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAPS_LOCK)
        val result = capsLock.onKeyDown(host, KeyEvent.KEYCODE_CAPS_LOCK, event)

        assertTrue(result)
    }

    @Test
    fun capsLock_onKeyUp_returnsTrue() {
        val capsLock = CapsLock()
        val event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CAPS_LOCK)
        // CapsLock.onKeyUp shows a Toast, which requires the main thread Looper
        var result = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            result = capsLock.onKeyUp(host, KeyEvent.KEYCODE_CAPS_LOCK, event)
        }
        assertTrue(result)
    }

    // ========== LeftAlt tests ==========

    @Test
    fun leftAlt_onKeyDown_returnsTrue() {
        val leftAlt = LeftAlt()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT)
        val result = leftAlt.onKeyDown(host, KeyEvent.KEYCODE_ALT_LEFT, event)

        assertTrue(result)
    }

    // ========== PhysicalKeyHandler default implementation tests ==========

    @Test
    fun physicalKeyHandler_defaultOnKeyUp_returnsFalse() {
        // Use a concrete handler that does NOT override onKeyUp (e.g. Del)
        val del = Del()
        val event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyUp(host, KeyEvent.KEYCODE_DEL, event)

        assertFalse(result)
    }

    @Test
    fun physicalKeyHandler_defaultOnKeyLongPress_returnsFalse() {
        // Use a concrete handler that does NOT override onKeyLongPress (e.g. Del)
        val del = Del()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyLongPress(host, KeyEvent.KEYCODE_DEL, event)

        assertFalse(result)
    }
}
