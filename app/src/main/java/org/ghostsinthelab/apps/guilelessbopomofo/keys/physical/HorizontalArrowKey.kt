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

import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost

/**
 * The `Left` and `Right` keys, which walk the cursor through the pre-edit buffer one
 * character at a time, or jump to either end of it when `Ctrl` is held down.
 */
abstract class HorizontalArrowKey(private val direction: DirectionKey) : PhysicalKeyHandler {
    override fun onKeyDown(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean {
        // simulate Ctrl-Left as Home key, Ctrl-Right as End key
        if (event?.isCtrlPressed == true && ChewingUtil.candidateWindowClosed()) {
            when (direction) {
                DirectionKey.LEFT -> ChewingBridge.chewing.handleHome()
                DirectionKey.RIGHT -> ChewingBridge.chewing.handleEnd()
            }
            host.dispatch(ImeAction.UpdateCursorPosition)
            return true
        }

        ChewingUtil.moveCursorHorizontally(host, direction)
        return true
    }
}
