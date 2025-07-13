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
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class Del : PhysicalKeyHandler {
    override fun onKeyDown(
        context: Context,
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        if (ChewingUtil.anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleBackspace()
            EventBus.getDefault().post(Events.UpdateBuffers())
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL))
        }

        return true
    }
}