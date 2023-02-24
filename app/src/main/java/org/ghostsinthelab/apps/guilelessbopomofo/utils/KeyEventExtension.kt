package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.view.KeyEvent

interface KeyEventExtension {
    fun KeyEvent.isNumPadKey(): Boolean {
        if (this.keyCode in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_SUBTRACT) {
            return true
        }
        return false
    }
}