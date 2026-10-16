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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual

import android.content.Context
import android.util.Log
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class ShiftKey(private val uiState: ImeUiState) : VirtualKey {
    // manage Shift key state
    enum class ShiftKeyState {
        RELEASED,
        PRESSED,
        HOLD,
        ;

        val isActive: Boolean get() = this != RELEASED
        val isLocked: Boolean get() = this == HOLD

        /** Tapping the key walks through the states, and around again. */
        fun next(): ShiftKeyState = entries[(ordinal + 1) % entries.size]
    }

    override fun onDown(context: Context) {
        performVibration(context, Vibratable.VibrationStrength.NORMAL)
    }

    override fun onTap(context: Context) {
        switchToState(uiState, uiState.shiftKeyState.next())
    }

    companion object {
        private const val LOG_TAG = "ShiftKey"

        fun switchToState(uiState: ImeUiState, state: ShiftKeyState) {
            Log.d(LOG_TAG, "Switch to state: $state")
            uiState.shiftKeyState = state
            // notify GuilelessBopomofoService of shift key state change
            EventBus.getDefault().post(Events.UpdateShiftKeyState(state.isActive, state.isLocked))
        }
    }
}
