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

import android.util.Log
import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.CapsLock
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Del
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Enter
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Escape
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Left
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.LeftAlt
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyHandler
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Right
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.RightShift
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.Space
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PhysicalKeyHandlerInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataDir = File(dataPath)
    private val chewingDataFiles =
        listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    // Event tracking lists
    private val receivedUpdateBufferViews = mutableListOf<Events.UpdateBufferViews>()
    private val receivedSendDownUpKeyEvents = mutableListOf<Events.SendDownUpKeyEvents>()
    private val receivedEnterKeyDownWhenBufferIsEmpty =
        mutableListOf<Events.EnterKeyDownWhenBufferIsEmpty>()
    private val receivedRequestHideIme = mutableListOf<Events.RequestHideIme>()
    private val receivedExitKeyboardSubLayouts = mutableListOf<Events.ExitKeyboardSubLayouts>()
    private val receivedUpdateCursorPosition = mutableListOf<Events.UpdateCursorPosition>()
    private val receivedDirectionKeyDown = mutableListOf<Events.DirectionKeyDown>()
    private val receivedToggleKeyboardMainLayoutMode =
        mutableListOf<Events.ToggleKeyboardMainLayoutMode>()
    private val receivedToggleFullOrHalfWidthMode =
        mutableListOf<Events.ToggleFullOrHalfWidthMode>()
    private val receivedSwitchToLayout = mutableListOf<Events.SwitchToLayout>()

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onUpdateBufferViews(event: Events.UpdateBufferViews) {
        receivedUpdateBufferViews.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onSendDownUpKeyEvents(event: Events.SendDownUpKeyEvents) {
        receivedSendDownUpKeyEvents.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEnterKeyDownWhenBufferIsEmpty(event: Events.EnterKeyDownWhenBufferIsEmpty) {
        receivedEnterKeyDownWhenBufferIsEmpty.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onRequestHideIme(event: Events.RequestHideIme) {
        receivedRequestHideIme.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onExitKeyboardSubLayouts(event: Events.ExitKeyboardSubLayouts) {
        receivedExitKeyboardSubLayouts.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onUpdateCursorPosition(event: Events.UpdateCursorPosition) {
        receivedUpdateCursorPosition.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onDirectionKeyDown(event: Events.DirectionKeyDown) {
        receivedDirectionKeyDown.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onToggleKeyboardMainLayoutMode(event: Events.ToggleKeyboardMainLayoutMode) {
        receivedToggleKeyboardMainLayoutMode.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onToggleFullOrHalfWidthMode(event: Events.ToggleFullOrHalfWidthMode) {
        receivedToggleFullOrHalfWidthMode.add(event)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onSwitchToLayout(event: Events.SwitchToLayout) {
        receivedSwitchToLayout.add(event)
    }

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

        // Register this test instance as an EventBus subscriber
        EventBus.getDefault().register(this)
    }

    @After
    fun tearDown() {
        // Unregister EventBus subscriber
        EventBus.getDefault().unregister(this)

        // Close Chewing
        ChewingBridge.chewing.delete()
    }

    private fun clearEventLists() {
        receivedUpdateBufferViews.clear()
        receivedSendDownUpKeyEvents.clear()
        receivedEnterKeyDownWhenBufferIsEmpty.clear()
        receivedRequestHideIme.clear()
        receivedExitKeyboardSubLayouts.clear()
        receivedUpdateCursorPosition.clear()
        receivedDirectionKeyDown.clear()
        receivedToggleKeyboardMainLayoutMode.clear()
        receivedToggleFullOrHalfWidthMode.clear()
        receivedSwitchToLayout.clear()
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
        val result = del.onKeyDown(appContext, KeyEvent.KEYCODE_DEL, event)

        assertTrue(result)
        assertEquals(1, receivedUpdateBufferViews.size)
    }

    @Test
    fun del_onKeyDown_withEmptyBuffer_postsKeyEvent() {
        val del = Del()
        // Ensure buffer is empty
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyDown(appContext, KeyEvent.KEYCODE_DEL, event)

        assertTrue(result)
        assertEquals(1, receivedSendDownUpKeyEvents.size)
        assertEquals(KeyEvent.KEYCODE_DEL, receivedSendDownUpKeyEvents[0].keycode)
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
        val result = enter.onKeyDown(appContext, KeyEvent.KEYCODE_ENTER, event)

        assertTrue(result)
        assertEquals(1, receivedUpdateBufferViews.size)
    }

    @Test
    fun enter_onKeyDown_withEmptyBuffer_postsEnterKeyDownWhenBufferIsEmpty() {
        val enter = Enter()
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        val result = enter.onKeyDown(appContext, KeyEvent.KEYCODE_ENTER, event)

        assertTrue(result)
        assertEquals(1, receivedEnterKeyDownWhenBufferIsEmpty.size)
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
        val result = space.onKeyDown(appContext, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
    }

    @Test
    fun space_onKeyDown_withEmptyBuffer_postsSendDownUpKeyEvents() {
        val space = Space()
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isEmpty())
        assertTrue(ChewingBridge.chewing.bopomofoStringStatic().isEmpty())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE)
        val result = space.onKeyDown(appContext, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, receivedSendDownUpKeyEvents.size)
        assertEquals(KeyEvent.KEYCODE_SPACE, receivedSendDownUpKeyEvents[0].keycode)
    }

    @Test
    fun space_onKeyDown_withShiftPressed_postsToggleKeyboardMainLayoutMode() {
        val space = Space()
        // Create a KeyEvent with Shift pressed
        val downTime = android.os.SystemClock.uptimeMillis()
        val eventTime = android.os.SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, 0,
            KeyEvent.META_SHIFT_ON
        )

        val result = space.onKeyDown(appContext, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, receivedToggleKeyboardMainLayoutMode.size)
    }

    @Test
    fun space_onKeyDown_withAltPressed_postsToggleFullOrHalfWidthMode() {
        val space = Space()
        // Create a KeyEvent with Alt pressed
        val downTime = android.os.SystemClock.uptimeMillis()
        val eventTime = android.os.SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, 0,
            KeyEvent.META_ALT_ON
        )

        val result = space.onKeyDown(appContext, KeyEvent.KEYCODE_SPACE, event)

        assertTrue(result)
        assertEquals(1, receivedToggleFullOrHalfWidthMode.size)
    }

    // ========== Left tests ==========

    @Test
    fun left_onKeyDown_plainLeft_postsDirectionKeyDown() {
        val left = Left()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)
        val result = left.onKeyDown(appContext, KeyEvent.KEYCODE_DPAD_LEFT, event)

        assertTrue(result)
        assertEquals(1, receivedDirectionKeyDown.size)
        assertEquals(DirectionKey.LEFT, receivedDirectionKeyDown[0].direction)
    }

    @Test
    fun left_onKeyDown_withCtrlPressed_postsUpdateCursorPosition() {
        val left = Left()
        // Create a KeyEvent with Ctrl pressed
        val downTime = android.os.SystemClock.uptimeMillis()
        val eventTime = android.os.SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0,
            KeyEvent.META_CTRL_ON
        )

        val result = left.onKeyDown(appContext, KeyEvent.KEYCODE_DPAD_LEFT, event)

        assertTrue(result)
        assertEquals(1, receivedUpdateCursorPosition.size)
        // Should NOT post DirectionKeyDown when Ctrl is pressed
        assertEquals(0, receivedDirectionKeyDown.size)
    }

    // ========== Right tests ==========

    @Test
    fun right_onKeyDown_plainRight_postsDirectionKeyDown() {
        val right = Right()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
        val result = right.onKeyDown(appContext, KeyEvent.KEYCODE_DPAD_RIGHT, event)

        assertTrue(result)
        assertEquals(1, receivedDirectionKeyDown.size)
        assertEquals(DirectionKey.RIGHT, receivedDirectionKeyDown[0].direction)
    }

    @Test
    fun right_onKeyDown_withCtrlPressed_postsUpdateCursorPosition() {
        val right = Right()
        // Create a KeyEvent with Ctrl pressed
        val downTime = android.os.SystemClock.uptimeMillis()
        val eventTime = android.os.SystemClock.uptimeMillis()
        val event = KeyEvent(
            downTime, eventTime,
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0,
            KeyEvent.META_CTRL_ON
        )

        val result = right.onKeyDown(appContext, KeyEvent.KEYCODE_DPAD_RIGHT, event)

        assertTrue(result)
        assertEquals(1, receivedUpdateCursorPosition.size)
        assertEquals(0, receivedDirectionKeyDown.size)
    }

    // ========== Escape tests ==========

    @Test
    fun escape_onKeyDown_postsRequestHideIme() {
        val escape = Escape()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE)
        val result = escape.onKeyDown(appContext, KeyEvent.KEYCODE_ESCAPE, event)

        assertTrue(result)
        assertEquals(1, receivedRequestHideIme.size)
        assertEquals(1, receivedExitKeyboardSubLayouts.size)
        assertEquals(1, receivedUpdateCursorPosition.size)
    }

    // ========== RightShift tests ==========

    @Test
    fun rightShift_onKeyDown_returnsTrue() {
        val rightShift = RightShift()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = rightShift.onKeyDown(appContext, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertTrue(result)
    }

    @Test
    fun rightShift_onKeyLongPress_inChineseMode_postsEvents() {
        val rightShift = RightShift()
        // Ensure we are in Chinese mode
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = rightShift.onKeyLongPress(appContext, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertTrue(result)
        assertEquals(1, receivedSwitchToLayout.size)
    }

    @Test
    fun rightShift_onKeyLongPress_inSymbolMode_returnsFalse() {
        val rightShift = RightShift()
        // Switch to symbol (English) mode
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())

        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_RIGHT)
        val result = rightShift.onKeyLongPress(appContext, KeyEvent.KEYCODE_SHIFT_RIGHT, event)

        assertFalse(result)
        assertEquals(0, receivedSwitchToLayout.size)
    }

    // ========== CapsLock tests ==========

    @Test
    fun capsLock_onKeyDown_returnsTrue() {
        val capsLock = CapsLock()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CAPS_LOCK)
        val result = capsLock.onKeyDown(appContext, KeyEvent.KEYCODE_CAPS_LOCK, event)

        assertTrue(result)
    }

    @Test
    fun capsLock_onKeyUp_returnsTrue() {
        val capsLock = CapsLock()
        val event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CAPS_LOCK)
        // CapsLock.onKeyUp shows a Toast, which requires the main thread Looper
        var result = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            result = capsLock.onKeyUp(appContext, KeyEvent.KEYCODE_CAPS_LOCK, event)
        }
        assertTrue(result)
    }

    // ========== LeftAlt tests ==========

    @Test
    fun leftAlt_onKeyDown_returnsTrue() {
        val leftAlt = LeftAlt()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT)
        val result = leftAlt.onKeyDown(appContext, KeyEvent.KEYCODE_ALT_LEFT, event)

        assertTrue(result)
    }

    // ========== PhysicalKeyHandler default implementation tests ==========

    @Test
    fun physicalKeyHandler_defaultOnKeyUp_returnsFalse() {
        // Use a concrete handler that does NOT override onKeyUp (e.g. Del)
        val del = Del()
        val event = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyUp(appContext, KeyEvent.KEYCODE_DEL, event)

        assertFalse(result)
    }

    @Test
    fun physicalKeyHandler_defaultOnKeyLongPress_returnsFalse() {
        // Use a concrete handler that does NOT override onKeyLongPress (e.g. Del)
        val del = Del()
        val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val result = del.onKeyLongPress(appContext, KeyEvent.KEYCODE_DEL, event)

        assertFalse(result)
    }
}
