/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.CursorMovedByPhysicalKeyboardEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.SendSingleDownUpKeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.ToggleToPreviousCandidatesPageEvent
import org.greenrobot.eventbus.EventBus

class LeftKey {
    companion object {
        fun action() {
            ChewingBridge.handleLeft()
            if (ChewingBridge.bufferLen() > 0) {
                EventBus.getDefault().post(CursorMovedByPhysicalKeyboardEvent())
            } else {
                if (ChewingUtil.candWindowClosed()) {
                    EventBus.getDefault().post(SendSingleDownUpKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT))
                }
            }

            // toggle to previous page of candidates
            EventBus.getDefault().post(ToggleToPreviousCandidatesPageEvent())
        }
    }
}