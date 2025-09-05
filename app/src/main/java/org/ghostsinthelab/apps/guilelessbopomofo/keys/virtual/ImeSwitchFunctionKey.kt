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
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyImageButton
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class ImeSwitchFunctionKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {
    override var mDetector: GestureDetector

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        setKeyVisibility()
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            performVibration(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (sharedPreferences.getBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false)) {
                return true
            } else {
                switchNextInputMethod()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (sharedPreferences.getBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false)) {
                switchNextInputMethod()
            } else {
                return true
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            performVibration(context, Vibratable.VibrationStrength.STRONG)
            val imm =
                context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        private fun switchNextInputMethod() {
            EventBus.getDefault().post(Events.SwitchToNextInputMethod())
        }
    }

    private fun setKeyVisibility() {
        val userEnableImeSwitch = sharedPreferences.getBoolean(USER_ENABLE_IME_SWITCH, false)
        this@ImeSwitchFunctionKey.visibility = if (userEnableImeSwitch) {
            VISIBLE
        } else {
            GONE
        }
    }
}