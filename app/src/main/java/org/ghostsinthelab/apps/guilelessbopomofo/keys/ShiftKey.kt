/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
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
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class ShiftKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    override val LOGTAG: String = "ShiftKeyImageButton"

    // manage Shift key state
    enum class ShiftKeyState { RELEASED, PRESSED, HOLD }

    var currentShiftKeyState = ShiftKeyState.RELEASED
    var isLocked: Boolean = false
    var isActive: Boolean = false

    override lateinit var mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)

        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyImageButton, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyImageButton_keyCodeString)
                keyType = this.getInt(R.styleable.KeyImageButton_keyTypeEnum, -1)
                keySymbol = this.getString(R.styleable.KeyImageButton_keySymbolString)
                keyShiftSymbol = this.getString(R.styleable.KeyImageButton_keyShiftSymbolString)
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

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(), Vibratable {
        override fun onDown(e: MotionEvent?): Boolean {
            performVibrate(context, Vibratable.VibrationStrength.NORMAL)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
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

    fun switchToState(state: ShiftKeyState) {
        Log.v(LOGTAG, "Switch to state: ${state}")
        this.currentShiftKeyState = state

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        lateinit var buttonStateBackgroundColors: Map<ShiftKeyState, Int>

        when(currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                buttonStateBackgroundColors = mapOf(
                    ShiftKeyState.RELEASED to R.color.colorKeyboardSpecialKeyBackground,
                    ShiftKeyState.PRESSED to R.color.colorKeyboardSpecialKeyBackgroundPressed,
                    ShiftKeyState.HOLD to R.color.colorKeyboardSpecialKeyBackgroundHold
                )
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                buttonStateBackgroundColors = mapOf(
                    ShiftKeyState.RELEASED to R.color.colorKeyboardSpecialKeyBackgroundDark,
                    ShiftKeyState.PRESSED to R.color.colorKeyboardSpecialKeyBackgroundDarkPressed,
                    ShiftKeyState.HOLD to R.color.colorKeyboardSpecialKeyBackgroundDarkHold
                )
            }
        }

        when (state) {
            ShiftKeyState.RELEASED -> {
                isActive = false
                isLocked = false
                background.setTint(
                    ContextCompat.getColor(
                        context,
                        buttonStateBackgroundColors.getValue(state)
                    )
                )
            }
            ShiftKeyState.PRESSED -> {
                isActive = true
                isLocked = false
                background.setTint(
                    ContextCompat.getColor(
                        context,
                        buttonStateBackgroundColors.getValue(state)
                    )
                )
            }
            ShiftKeyState.HOLD -> {
                isActive = true
                isLocked = true
                background.setTint(
                    ContextCompat.getColor(
                        context,
                        buttonStateBackgroundColors.getValue(state)
                    )
                )
            }
        }
    }
}