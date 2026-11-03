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
import android.view.ViewConfiguration
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
 * single instance for both keys and tells it about every other key going down, and about the
 * selection moving, which is how a `Shift`-click with a mouse shows up here.
 *
 * Only the right one is kept from the app; the left one also reaches it as before, so apps
 * that watch `Shift` themselves keep working.
 */
class Shift : PhysicalKeyHandler {
    // the Shift key currently held down, if any
    private var heldKeyCode: Int? = null

    // whether the press can no longer be a tap: another key went down while Shift was held,
    // making it a chord, or Shift started repeating because it was held down
    private var chorded = false

    override fun onKeyDown(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        event?.startTracking()
        if (event?.repeatCount == 0) {
            // Pressing one Shift while the other is held is no tap of either, nor is pressing it
            // while another modifier such as Ctrl or Alt is already held. The same key going
            // down again only means we never saw it go up, so that press starts afresh.
            chorded = (heldKeyCode != null && heldKeyCode != keyCode) || event.hasOtherModifiers()
            heldKeyCode = keyCode
        } else if (keyCode == heldKeyCode) {
            // A repeat means Shift is being held down, not tapped, whatever the key-up's time says.
            chorded = true
        }
        return keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
    }

    override fun onKeyUp(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        val tapped = keyCode == heldKeyCode && !chorded && event != null && event.isQuickTap()
        reset()
        if (tapped) {
            EventBus.getDefault().post(Events.ToggleKeyboardMainLayoutMode())
        }
        return keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
    }

    override fun onKeyLongPress(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT && ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
            ChewingUtil.openFrequentlyUsedCandidates()
            EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            return true
        }
        return super.onKeyLongPress(context, keyCode, event)
    }

    /**
     * Tells a press of `Shift` apart from a chord such as `Shift-Space`, `Shift-A` or
     * `Shift`-click: call it when another key goes down or the selection moves.
     */
    fun onOtherInput() {
        if (heldKeyCode != null) chorded = true
    }

    /**
     * Forgets any press in progress. The service calls this when the input view goes away,
     * since the key-up of a Shift held at that moment never reaches us.
     */
    fun reset() {
        heldKeyCode = null
        chorded = false
    }

    // A long press that did something cancels the key-up, so it never counts as a tap.
    private fun KeyEvent.isQuickTap(): Boolean =
        !isCanceled && !hasOtherModifiers() && eventTime - downTime < tapTimeoutMs()

    // whether a modifier other than Shift (Ctrl, Alt, Meta, Fn, Sym) is held down
    private fun KeyEvent.hasOtherModifiers(): Boolean =
        !KeyEvent.metaStateHasNoModifiers(metaState and KeyEvent.META_SHIFT_MASK.inv())

    companion object {
        val KEY_CODES = setOf(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT)

        // longest press that still counts as a tap
        const val TAP_TIMEOUT_MS = 300L

        // TAP_TIMEOUT_MS, but never as long as the user's long-press timeout
        private fun tapTimeoutMs(): Long =
            minOf(TAP_TIMEOUT_MS, ViewConfiguration.getLongPressTimeout().toLong())
    }
}
