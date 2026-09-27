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
import android.util.AttributeSet
import android.view.MotionEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyButton
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

/** One of the English words offered on the suggestion strip. */
class SuggestionButton(context: Context, attrs: AttributeSet) :
    KeyButton(context, attrs), Vibratable {

    override fun createGestureListener() = MyGestureListener()

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            performVibration(context, Vibratable.VibrationStrength.LIGHT)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (text.isNotEmpty()) {
                EventBus.getDefault().post(Events.SuggestionSelected(text.toString()))
            }
            return true
        }
    }
}
