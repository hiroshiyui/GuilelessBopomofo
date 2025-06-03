/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.content.Context
import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class Space : PhysicalKeyHandler {
    override fun onKeyDown(
        context: Context,
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        if (event?.isShiftPressed == true) {
            EventBus.getDefault().post(Events.ToggleKeyboardMainLayoutMode())
            return true
        }

        if (ChewingUtil.anyPreEditBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleSpace()
            EventBus.getDefault().post(Events.UpdateBuffers())
            // 空白鍵是否為選字鍵？
            if (ChewingBridge.chewing.getSpaceAsSelection() == 1 && ChewingBridge.chewing.candTotalChoice() > 0) {
                ChewingUtil.openCandidates()
            }
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE))
        }

        return true
    }

    override fun onKeyUp(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(context, keyCode, event)
    }
}