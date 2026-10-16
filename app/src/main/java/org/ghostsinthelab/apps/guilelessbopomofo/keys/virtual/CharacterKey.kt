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
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.greenrobot.eventbus.EventBus

/** A key that types: [keyCodeString] is the name of the key code it stands for, `KEYCODE_A` say. */
class CharacterKey(private val keyCodeString: String) : VirtualKey {
    // Everything happens on the way down, so that fast typing never waits for a finger to lift.
    override fun onDown(context: Context) {
        performVibration(context, GuilelessBopomofoService.userHapticFeedbackStrength)
        EventBus.getDefault().post(Events.PrintingKeyDown(keyCodeString))
    }
}
