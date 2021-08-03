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
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

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
        if (strength.strength == 0L) {
            return
        }

        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(strength.strength, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(strength.strength)
        }
    }

    fun performVibrate(context: Context, strength: Long) {
        if (strength == 0L) {
            return
        }

        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(strength, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(strength)
        }
    }
}