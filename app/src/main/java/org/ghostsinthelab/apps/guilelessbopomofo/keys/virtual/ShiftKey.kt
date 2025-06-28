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
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.AttrRes
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.KeyImageButton
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class ShiftKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override val logTag: String = "ShiftKeyImageButton"

    // manage Shift key state
    enum class ShiftKeyState { RELEASED, PRESSED, HOLD }

    var currentShiftKeyState = ShiftKeyState.RELEASED
    var isActive: Boolean = false
    var isLocked: Boolean = false

    override var mDetector: GestureDetector

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)

        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyImageButton, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyImageButton_keyCodeString)
            } finally {
                recycle()
            }
        }

        this.apply {
            if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                elevation = convertDpToPx(2F)
            }
        }
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            performVibration(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            when (currentShiftKeyState) {
                ShiftKeyState.RELEASED -> {
                    switchToState(ShiftKeyState.PRESSED)
                }

                ShiftKeyState.PRESSED -> {
                    switchToState(ShiftKeyState.HOLD)
                }

                ShiftKeyState.HOLD -> {
                    switchToState(ShiftKeyState.RELEASED)
                }
            }

            return true
        }
    }

    private fun Context.getThemeColor(@AttrRes attrResId: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    fun switchToState(state: ShiftKeyState) {
        Log.d(logTag, "Switch to state: $state")
        this.currentShiftKeyState = state

        val colorPrimary = context.getThemeColor(R.attr.colorPrimary)
        val colorSecondary = context.getThemeColor(R.attr.colorSecondary)
        val colorTertiary = context.getThemeColor(R.attr.colorTertiary)

        val buttonStateBackgroundColors: Map<ShiftKeyState, Int> = mapOf(
            ShiftKeyState.RELEASED to colorTertiary,
            ShiftKeyState.PRESSED to colorSecondary,
            ShiftKeyState.HOLD to colorPrimary
        )

        val backgroundColorToSet = buttonStateBackgroundColors.getValue(state)

        when (state) {
            ShiftKeyState.RELEASED -> {
                isActive = false
                isLocked = false
            }

            ShiftKeyState.PRESSED -> {
                isActive = true
                isLocked = false
            }

            ShiftKeyState.HOLD -> {
                isActive = true
                isLocked = true
            }
        }

        background.setTint(backgroundColorToSet)


        // notify GuilelessBopomofoService of shift key state change
        EventBus.getDefault().post(Events.UpdateShiftKeyState(isActive, isLocked))
    }
}