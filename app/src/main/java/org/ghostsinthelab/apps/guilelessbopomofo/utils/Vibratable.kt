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
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService

interface Vibratable {
    enum class VibrationStrength(val strength: Int) {
        LIGHT(25), NORMAL(50), STRONG(100)
    }

    val vibrationMilliSeconds: Long
        get() = 50

    fun performVibration(context: Context, vibrationStrength: VibrationStrength) {
        performVibration(context, vibrationStrength.strength)
    }

    fun performVibration(context: Context, amplitude: Int) {
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) ?: return

        // If device doesn't have vibrator, do nothing (e.g: many tablets)
        if (!vibrator.hasVibrator()) {
            return
        }

        // reduces UI blocking by vibrator (if user be typing too fast)
        vibrator.cancel()

        // If users want to use a consistent haptic feedback value for all buttons,
        // just do as they wish. (might be 0)
        val sharedPreferences = context.appSharedPreferences
        val effectiveAmplitude: Int =
            if (sharedPreferences.getBoolean(SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS, false)) {
                sharedPreferences.getInt(
                    USER_HAPTIC_FEEDBACK_STRENGTH, GuilelessBopomofoService.defaultHapticFeedbackStrength
                )
            } else {
                amplitude
            }

        // do nothing if user set vibration strength to 0
        if (effectiveAmplitude == 0) {
            return
        }

        // perform vibration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(vibrationMilliSeconds, effectiveAmplitude))
        } else {
            @Suppress("DEPRECATION")
            // deprecated in API 26 (Android 8.0), for older devices, we just support time-based vibration. (treat amplitude as time in milliseconds)
            vibrator.vibrate(effectiveAmplitude.toLong())
        }
    }
}

/** For callers that are not themselves a [Vibratable], such as composables. */
object Haptics : Vibratable
