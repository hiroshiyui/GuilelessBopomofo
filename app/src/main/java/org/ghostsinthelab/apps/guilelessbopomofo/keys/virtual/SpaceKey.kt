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
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyImageButton
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class SpaceKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override var mDetector: GestureDetector

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(), Vibratable {
        override fun onDown(e: MotionEvent): Boolean {
            performVibration(context, Vibratable.VibrationStrength.LIGHT)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (ChewingUtil.Companion.anyBufferIsNotEmpty()) {
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
    }
}