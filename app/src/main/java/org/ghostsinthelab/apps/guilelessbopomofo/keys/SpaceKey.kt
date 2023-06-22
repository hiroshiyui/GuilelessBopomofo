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
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.SendSingleDownUpKeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.SwitchToCandidatesLayoutEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.ToggleToMainLayoutModeEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.UpdateBuffersEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.UpdateCursorEvent
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class SpaceKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    inner class MyGestureListener: GestureDetector.SimpleOnGestureListener(), Vibratable {
        override fun onDown(e: MotionEvent): Boolean {
            performVibrate(context, Vibratable.VibrationStrength.LIGHT)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            action()
            return true
        }
    }

    companion object {
        fun action() {
            if (ChewingUtil.anyPreeditBufferIsNotEmpty()) {
                ChewingBridge.handleSpace()
                EventBus.getDefault().post(UpdateBuffersEvent())
                // 空白鍵是否為選字鍵？
                if (ChewingBridge.getSpaceAsSelection() == 1 && ChewingBridge.candTotalChoice() > 0) {
                    EventBus.getDefault().let {
                        it.post(UpdateCursorEvent())
                        it.post(SwitchToCandidatesLayoutEvent())
                    }
                }
            } else {
                EventBus.getDefault().post(SendSingleDownUpKeyEvent(KeyEvent.KEYCODE_SPACE))
            }
        }

        // for physical keyboard space key, detect if Shift is pressed first:
        fun action(keyEvent: KeyEvent) {
            if (keyEvent.isShiftPressed) {
                EventBus.getDefault().post(ToggleToMainLayoutModeEvent())
                return
            }
            action()
        }
    }
}