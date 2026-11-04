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
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost

/** Down arrow: opens the candidates while there is a buffer, moves in the text field otherwise. */
class Down : PhysicalKeyHandler {
    override fun onKeyDown(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("Down", "onKeyDown()")
        if (ChewingBridge.chewing.bufferLen() > 0) {
            ChewingUtil.openCandidates(host)
        } else {
            host.dispatch(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN))
        }
        return true
    }
}