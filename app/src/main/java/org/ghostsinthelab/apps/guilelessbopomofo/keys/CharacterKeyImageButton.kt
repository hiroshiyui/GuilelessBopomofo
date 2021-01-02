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
import android.util.Log
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingEngine
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.events.BufferUpdatedEvent
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class CharacterKeyImageButton(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    var sendCharacter by Delegates.notNull<Char>()

    init {
        this.setOnClickListener {
            this.keySymbol?.let {
                sendCharacter = it.get(0)
            }

            val shiftKeyImageButton =
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.findViewById<ShiftKeyImageButton>(
                    R.id.keyImageButtonShift
                )

            shiftKeyImageButton?.let {
                if (shiftKeyImageButton.isActive) {
                    Log.v(LOGTAG, "Shift is active")
                    if (this.keyShiftSymbol?.isNotEmpty() == true) {
                        sendCharacter = this.keyShiftSymbol.toString().get(0)
                    } else {
                        sendCharacter = this.keySymbol.toString().get(0).toUpperCase()
                    }
                }
            }

            ChewingEngine.handleDefault(sendCharacter)
            EventBus.getDefault().post(BufferUpdatedEvent())

            shiftKeyImageButton?.let {
                if (shiftKeyImageButton.isActive && !shiftKeyImageButton.isLocked) {
                    Log.v(LOGTAG, "Release shift key")
                    shiftKeyImageButton.switchToState(ShiftKeyImageButton.ShiftKeyState.RELEASED)
                }
            }
        }
    }
}