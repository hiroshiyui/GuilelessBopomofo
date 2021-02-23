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

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class SpaceKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    init {
        this.setOnClickListener {
            performVibrate(Vibratable.VibrationStrength.LIGHT)
            action()
        }
    }

    companion object {
        fun action() {
            if (ChewingUtil.anyPreeditBufferIsNotEmpty()) {
                ChewingBridge.handleSpace()
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.let {
                    it.textViewPreEditBuffer.update()
                    it.textViewBopomofoBuffer.update()
                }
                // 空白鍵是否為選字鍵？
                if (ChewingBridge.getSpaceAsSelection() == 1 && ChewingBridge.candTotalChoice() > 0) {
                    GuilelessBopomofoServiceContext.serviceInstance.viewBinding.let {
                        it.textViewPreEditBuffer.offset = ChewingBridge.cursorCurrent()
                        it.textViewPreEditBuffer.renderUnderlineSpan()
                        it.keyboardPanel.switchToCandidatesLayout()
                    }
                }
            } else {
                GuilelessBopomofoServiceContext.serviceInstance.sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE)
            }
        }

        // for physical keyboard space key, detect if Shift is pressed first:
        fun action(keyEvent: KeyEvent) {
            if (keyEvent.isShiftPressed) {
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.toggleMainLayoutMode()
                return
            }
            action()
        }
    }
}