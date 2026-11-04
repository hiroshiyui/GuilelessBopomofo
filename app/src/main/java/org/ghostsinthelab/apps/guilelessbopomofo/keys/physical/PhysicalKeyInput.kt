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
package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_C
import android.view.KeyEvent.KEYCODE_GRAVE
import android.view.KeyEvent.KEYCODE_I
import android.view.KeyEvent.KEYCODE_R
import android.view.KeyEvent.KEYCODE_SHIFT_LEFT
import android.view.KeyEvent.KEYCODE_V
import android.view.KeyEvent.KEYCODE_X
import android.view.KeyEvent.KEYCODE_Z
import android.view.KeyEvent.META_SHIFT_ON
import android.view.inputmethod.InputConnection
import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoPhysicalKeyboards
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.ChiEngMode
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardController
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.utils.KeyEventExtension

/**
 * Answers the hardware keyboard: the function keys through their [PhysicalKeyHandler]s, the
 * printing keys through libchewing, and the rest by passing them on to the text field. The
 * on-screen character keys type through [onPrintingKeyDown] too.
 *
 * Each `onKey...` says whether it took the event; the service falls back to Android's own
 * handling when it did not.
 */
class PhysicalKeyInput(
    private val host: ImeHost,
    private val uiState: ImeUiState,
    private val keyboardController: KeyboardController,
    private val editor: Editor,
) : KeyEventExtension {
    /** The little the keys need of the input method service itself. */
    interface Editor {
        val isInputViewShown: Boolean
        val inputConnection: InputConnection?
        fun hideKeyboard()

        /** A key was pressed on a physical keyboard nobody knew was there. */
        fun physicalKeyboardDetected()
        fun showInputMethodPicker()

        /** Types [event] straight into the text field, past libchewing. */
        fun sendKeyEventToField(event: KeyEvent)
    }

    private val logTag = "PhysicalKeyInput"

    // Both Shift keys share this one: it follows a press through to tell a tap from a chord.
    private val shift = Shift()

    // The physical keys we answer to ourselves. Every handler but `shift` is stateless, so one
    // instance of each lasts as long as this object.
    private val handlers: Map<Int, PhysicalKeyHandler> = mapOf(
        KeyEvent.KEYCODE_DPAD_DOWN to Down(),
        KeyEvent.KEYCODE_DPAD_UP to Up(),
        KeyEvent.KEYCODE_DPAD_LEFT to Left(),
        KeyEvent.KEYCODE_DPAD_RIGHT to Right(),
        KeyEvent.KEYCODE_ALT_LEFT to LeftAlt(),
        KeyEvent.KEYCODE_SHIFT_LEFT to shift,
        KeyEvent.KEYCODE_SHIFT_RIGHT to shift,
        KeyEvent.KEYCODE_ENTER to Enter(),
        KeyEvent.KEYCODE_SPACE to Space(),
        KeyEvent.KEYCODE_ESCAPE to Escape(),
        KeyEvent.KEYCODE_DEL to Del(),
        KeyEvent.KEYCODE_CAPS_LOCK to CapsLock(),
        KeyEvent.KEYCODE_MOVE_END to End(),
        KeyEvent.KEYCODE_MOVE_HOME to Home(),
        KeyEvent.KEYCODE_VOLUME_UP to VolumeUp(),
        KeyEvent.KEYCODE_VOLUME_DOWN to VolumeDown(),
        // Add more mappings here for each physical key you want to handle separately
    )

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyDown()")

        // any other key going down turns a Shift press into a chord, e.g. Shift-Space
        if (keyCode !in Shift.KEY_CODES && event?.repeatCount == 0) {
            shift.onOtherInput()
        }

        if (!editor.isInputViewShown) return false

        // have to make Back key work as is at very first, or some back operations will be blocked
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(logTag, "Back key pressed")
            editor.hideKeyboard()
            return false
        }

        if (handlers[keyCode]?.onKeyDown(host, keyCode, event) == true) return true

        if (event != null && event.isPrintingKey) {
            // if a printing key has been pressed, assume that user have a physical keyboard connected anyway...
            if (!physicalKeyboardPresented) {
                physicalKeyboardPresented = true
                editor.physicalKeyboardDetected()
            }
            onPrintingKeyDown(event)
            return true
        }

        // pass-through other KeyEvent, or we will make some physical keys like volume keys invalid
        if (event != null) {
            editor.inputConnection?.sendKeyEvent(event)
        }
        return false
    }

    fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyUp()")

        if (!editor.isInputViewShown) return false
        if (keyCode == KeyEvent.KEYCODE_BACK) return false

        if (handlers[keyCode]?.onKeyUp(host, keyCode, event) == true) return true

        if (event?.isPrintingKey == true) {
            // Detect if a candidate had been chosen by user
            if (keyboardController.currentLayout == Layout.CANDIDATES) {
                keyboardController.candidateKeySelected()
            }
            return true
        }
        return false
    }

    fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyLongPress()")
        if (!editor.isInputViewShown) return false
        return handlers[keyCode]?.onKeyLongPress(host, keyCode, event) == true
    }

    /** The selection moved in the text field: a Shift held meanwhile was a chord, not a tap. */
    fun onSelectionMoved() {
        shift.onOtherInput()
    }

    /** The input view went away: a Shift held now would release unseen. */
    fun onInputViewFinished() {
        shift.reset()
    }

    // handles both physical and virtual printing key-down events, routes to chewing.handleDefault()
    fun onPrintingKeyDown(event: KeyEvent) {
        Log.d(logTag, "onPrintingKeyDown()")

        // Switch to compact layout if physical keyboard is present and current layout is not compact.
        // Skip while the candidates window is shown (e.g. the `‵` symbol picker): a printing key here
        // is a selection key, and switching away would tear down the candidates view before onKeyUp()
        // can render the next-level list, leaving sub-menus invisible (still effective in libchewing).
        if (physicalKeyboardPresented &&
            keyboardController.currentLayout != Layout.COMPACT &&
            keyboardController.currentLayout != Layout.CANDIDATES
        ) {
            keyboardController.switchToCompactLayout()
        }

        if (handleNumPadKey(event)) return
        if (handleGraveKeyForSymbols(event)) return
        if (handleAltIForImePicker(event)) return

        var keyPressed: Char = event.unicodeChar.toChar()

        if (uiState.shiftKeyState.isActive) {
            editor.inputConnection?.sendKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT))
            keyPressed = event.getUnicodeChar(META_SHIFT_ON).toChar()
        }

        if (handleCtrlKeySequence(event)) return

        // If in candidate selection window, the selection keys have to be mapped to DVORAK layout
        // TODO: This should be resolved in the future, in libchewing.
        if (ChewingBridge.chewing.getKBString() == BopomofoPhysicalKeyboards.KB_DVORAK_HSU.layout && keyboardController.currentLayout == Layout.CANDIDATES) {
            keyPressed = ChewingUtil.qwertyToDvorakKeyMapping(keyPressed)
        }

        ChewingBridge.chewing.handleDefault(keyPressed)
        host.dispatch(ImeAction.UpdateBufferViews)
        host.dispatch(ImeAction.UpdateCursorPosition)

        releaseShiftKeyIfNeeded()
    }

    private fun handleNumPadKey(event: KeyEvent): Boolean {
        if (!event.isNumPadKey()) return false
        editor.sendKeyEventToField(event)
        host.dispatch(ImeAction.UpdateBufferViews)
        return true
    }

    private fun handleGraveKeyForSymbols(event: KeyEvent): Boolean {
        if (event.keyCode == KEYCODE_GRAVE && ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode && !event.isShiftPressed) {
            keyboardController.switchToLayout(Layout.SYMBOLS)
            return true
        }
        return false
    }

    private fun handleAltIForImePicker(event: KeyEvent): Boolean {
        if (event.keyCode == KEYCODE_I && event.isAltPressed) {
            editor.showInputMethodPicker()
            return true
        }
        return false
    }

    private fun handleCtrlKeySequence(event: KeyEvent): Boolean {
        if (!event.isCtrlPressed) return false
        editor.inputConnection?.apply {
            when (event.keyCode) {
                KEYCODE_A -> performContextMenuAction(android.R.id.selectAll)
                KEYCODE_Z -> performContextMenuAction(android.R.id.undo)
                KEYCODE_X -> performContextMenuAction(android.R.id.cut)
                KEYCODE_C -> performContextMenuAction(android.R.id.copy)
                KEYCODE_V -> performContextMenuAction(android.R.id.paste)
                KEYCODE_R -> performContextMenuAction(android.R.id.redo)
            }
        }
        return true
    }

    private fun releaseShiftKeyIfNeeded() {
        if (uiState.shiftKeyState.isActive && !uiState.shiftKeyState.isLocked) {
            Log.d(logTag, "Release Shift key")
            releaseShiftKey()
        }
    }

    /** Lets go of the on-screen Shift, and of the one held down in the text field for it. */
    fun releaseShiftKey() {
        keyboardController.releaseShiftKey()
        editor.inputConnection?.sendKeyEvent(KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT))
    }
}
