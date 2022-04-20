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
import android.os.Build
import android.os.IBinder
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class ImeSwitchFunctionKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
    }

    inner class MyGestureListener : KeyImageButton.GestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            performVibrate(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (sharedPreferences.getBoolean("user_enable_double_touch_ime_switch", false)) {
                return true
            } else {
                switchInputMethod()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (sharedPreferences.getBoolean("user_enable_double_touch_ime_switch", false)) {
                switchInputMethod()
            } else {
                return true
            }
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            performVibrate(context, Vibratable.VibrationStrength.STRONG)
            val imm =
                GuilelessBopomofoServiceContext.serviceInstance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        private fun switchInputMethod() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                GuilelessBopomofoServiceContext.serviceInstance.switchToNextInputMethod(false)
            } else {
                // backward compatibility, support IME switch on legacy devices
                val imm =
                    GuilelessBopomofoServiceContext.serviceInstance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val imeToken: IBinder? =
                    GuilelessBopomofoServiceContext.serviceInstance.window?.let {
                        it.window?.attributes?.token
                    }
                imm.switchToNextInputMethod(imeToken, false)
            }
        }
    }
}