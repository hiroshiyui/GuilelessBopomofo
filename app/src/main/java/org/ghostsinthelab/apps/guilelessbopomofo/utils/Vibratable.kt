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

package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext

interface Vibratable {
    enum class VibrationStrength(val strength: Long) {
        LIGHT(25),
        NORMAL(50),
        STRONG(100)
    }

    val amplitude: Int
        get() = 128

    val strengthRange: IntRange
        get() = 0..150

    fun performVibrate(context: Context, strength: VibrationStrength) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator
        var vibrationStrength: Long = strength.strength

        // If users want to use a consistent haptic feedback value for all buttons,
        // just do as they wish.
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)
        val hapticFeedbackPreferenceStrength: Int =
            sharedPreferences.getInt(
                "user_haptic_feedback_strength",
                GuilelessBopomofoServiceContext.defaultHapticFeedbackStrength
            )
        val sameHapticFeedbackToFuncButtons: Boolean =
            sharedPreferences.getBoolean("same_haptic_feedback_to_function_buttons", false)
        if (sameHapticFeedbackToFuncButtons) {
            vibrationStrength = hapticFeedbackPreferenceStrength.toLong()
        }

        // do nothing if user set vibration strength to 0
        if (vibrationStrength == 0L) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(vibrationStrength, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(vibrationStrength)
        }
    }

    fun performVibrate(context: Context, strength: Long) {
        if (strength == 0L) {
            return
        }

        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator
        // reduces UI blocking by vibrator (if user be typing too fast)
        vibrator.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(strength, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(strength)
        }
    }
}