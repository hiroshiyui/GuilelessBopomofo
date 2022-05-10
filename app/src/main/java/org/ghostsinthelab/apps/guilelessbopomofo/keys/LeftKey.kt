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
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardPanel
import org.ghostsinthelab.apps.guilelessbopomofo.buffers.PreEditBufferTextView

class LeftKey {
    companion object {
        fun action() {
            ChewingBridge.handleLeft()
            if (ChewingBridge.bufferLen() > 0) {
                val preEditBuffer =
                    GuilelessBopomofoServiceContext.service.viewBinding.textViewPreEditBuffer
                preEditBuffer.cursorMovedBy(PreEditBufferTextView.CursorMovedBy.PHYSICAL_KEYBOARD)
            } else {
                GuilelessBopomofoServiceContext.service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
            }

            // toggle to previous page of candidates
            val keyboardPanel =
                GuilelessBopomofoServiceContext.service.viewBinding.keyboardPanel
            if (keyboardPanel.currentKeyboardLayout == KeyboardPanel.KeyboardLayout.CANDIDATES && ChewingUtil.candWindowOpened()) {
                keyboardPanel.renderCandidatesLayout()
            }
        }
    }
}