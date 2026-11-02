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

import android.content.Context
import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.ChiEngMode
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

/**
 * Both `Shift` keys. Tapping either one on its own, a quick press and release with no other
 * key pressed in between, toggles the main layout mode, like `Shift-Space` does. Holding
 * the right one down opens the frequently used candidates.
 *
 * Unlike the other handlers this one remembers the press in progress, so the service keeps a
 * single instance for both keys and tells it about every other key going down.
 */
class Shift : PhysicalKeyHandler {
    // the Shift key currently held down, if any
    private var heldKeyCode: Int? = null

    // whether another key went down while Shift was held, making it a chord rather than a tap
    private var chorded = false

    override fun onKeyDown(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        event?.startTracking()
        if (event?.repeatCount == 0) {
            // Pressing one Shift while the other is held is no tap of either. The same key going
            // down again only means we never saw it go up, so that press starts afresh.
            chorded = heldKeyCode != null && heldKeyCode != keyCode
            heldKeyCode = keyCode
        }
        return true
    }

    override fun onKeyUp(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        val tapped = keyCode == heldKeyCode && !chorded && event != null && event.isQuickTap()
        heldKeyCode = null
        chorded = false
        if (tapped) {
            EventBus.getDefault().post(Events.ToggleKeyboardMainLayoutMode())
        }
        return true
    }

    override fun onKeyLongPress(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT && ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
            ChewingUtil.openFrequentlyUsedCandidates()
            EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            return true
        }
        return super.onKeyLongPress(context, keyCode, event)
    }

    /** Tells a press of `Shift` apart from a chord such as `Shift-Space` or `Shift-A`. */
    fun onOtherKeyDown() {
        if (heldKeyCode != null) chorded = true
    }

    // A long press that did something cancels the key-up, so it never counts as a tap.
    private fun KeyEvent.isQuickTap(): Boolean =
        !isCanceled && eventTime - downTime < TAP_TIMEOUT_MS

    companion object {
        val KEY_CODES = setOf(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT)

        // longest press that still counts as a tap; well below the long-press timeout
        const val TAP_TIMEOUT_MS = 300L
    }
}
