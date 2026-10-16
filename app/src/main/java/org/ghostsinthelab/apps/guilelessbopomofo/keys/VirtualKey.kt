/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

/**
 * What an on-screen key does when touched. The gestures are recognised for it by
 * [org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.keyGestures]; a key only says how it
 * answers them.
 */
/** Which way a key was swiped. */
enum class SwipeDirection { UP, RIGHT, LEFT, DOWN }

interface VirtualKey : Vibratable {
    /**
     * Whether a quick second tap is a gesture of its own. Leaving it off is what an ordinary
     * key wants: every tap counts, none of them is held back waiting for its twin. With it
     * on, the second of two quick taps goes to [onDoubleTap] instead of [onTap].
     */
    val detectsDoubleTap: Boolean get() = false

    /** Whether holding the key down does something; see [onLongPress]. */
    val detectsLongPress: Boolean get() = false

    /**
     * Whether holding the key down keeps doing [onRepeat] for as long as it is held, once the
     * long-press timeout has passed.
     */
    val repeatsWhileHeld: Boolean get() = false

    /**
     * Whether a swipe starting on the key is a gesture of its own; see [onSwipe]. A swipe
     * key follows the finger beyond its edges, and has no long press.
     */
    val detectsSwipes: Boolean get() = false

    fun onDown(context: Context) {}
    fun onTap(context: Context) {}
    fun onDoubleTap(context: Context) {}
    fun onLongPress(context: Context) {}
    fun onRepeat(context: Context) {}
    fun onSwipe(context: Context, direction: SwipeDirection) {}
}
