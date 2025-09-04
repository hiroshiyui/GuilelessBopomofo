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

package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.appSharedPreferences
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.enums.RegisteredSharedPreferences
import kotlin.properties.Delegates

interface Vibratable {
    enum class VibrationStrength(val strength: Int) {
        LIGHT(25), NORMAL(50), STRONG(100)
    }

    val vibrationMilliSeconds: Long
        get() = 50

    fun <T> performVibration(context: Context, vibrationStrength: T) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator

        // If device doesn't have vibrator, do nothing (e.g: many tablets)
        if (!vibrator.hasVibrator()) {
            return
        }

        var amplitude by Delegates.notNull<Int>()

        when (vibrationStrength) {
            is VibrationStrength -> {
                amplitude = vibrationStrength.strength
            }

            is Int -> {
                amplitude = vibrationStrength.toInt()
            }
        }

        // reduces UI blocking by vibrator (if user be typing too fast)
        vibrator.cancel()

        // If users want to use a consistent haptic feedback value for all buttons,
        // just do as they wish.
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(appSharedPreferences, AppCompatActivity.MODE_PRIVATE)
        val hapticFeedbackPreferenceStrength: Int = sharedPreferences.getInt(
            RegisteredSharedPreferences.USER_HAPTIC_FEEDBACK_STRENGTH.key, GuilelessBopomofoService.defaultHapticFeedbackStrength
        )
        val sameHapticFeedbackToFuncButtons: Boolean =
            sharedPreferences.getBoolean(RegisteredSharedPreferences.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS.key, false)
        if (sameHapticFeedbackToFuncButtons) {
            // might be 0
            amplitude = hapticFeedbackPreferenceStrength
        }

        // do nothing if user set vibration strength to 0
        if (amplitude == 0) {
            return
        }

        // perform vibration
        runBlocking {
            launch {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationEffect =
                        VibrationEffect.createOneShot(vibrationMilliSeconds, amplitude)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    // deprecated in API 26 (Android 8.0), for older devices, we just support time-based vibration. (treat amplitude as time in milliseconds)
                    vibrator.vibrate(amplitude.toLong())
                }
            }
        }
    }
}