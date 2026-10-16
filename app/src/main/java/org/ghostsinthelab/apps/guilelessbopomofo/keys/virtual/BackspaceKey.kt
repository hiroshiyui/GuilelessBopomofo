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
import android.os.SystemClock
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

object BackspaceKey : VirtualKey {
    private const val BACKSPACE_DEBOUNCE_MS = 100L

    private var lastBackspaceDownTime: Long = 0

    override val repeatsWhileHeld: Boolean = true

    override fun onDown(context: Context) {
        // avoids buzzing on every one of too fast repeat clicks
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackspaceDownTime < BACKSPACE_DEBOUNCE_MS) return
        lastBackspaceDownTime = now
        performVibration(context, Vibratable.VibrationStrength.NORMAL)
    }

    override fun onTap(context: Context) {
        ChewingUtil.handleBackspaceAction()
    }

    // Deleting goes through libchewing, whose context may only be touched from the main
    // thread; the gesture that repeats this runs there too.
    override fun onRepeat(context: Context) {
        ChewingUtil.handleBackspaceAction()
    }
}
