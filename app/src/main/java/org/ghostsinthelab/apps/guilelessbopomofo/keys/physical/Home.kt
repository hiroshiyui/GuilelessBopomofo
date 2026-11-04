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
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost

/** Home: to the start of the buffer, or of the text field once the buffer is empty. */
class Home : PhysicalKeyHandler {
    override fun onKeyDown(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean {
        if (ChewingBridge.chewing.bufferLen() > 0) {
            host.dispatch(ImeAction.UpdateCursorPositionToBegin)
        } else {
            host.dispatch(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME))
        }
        return true
    }
}