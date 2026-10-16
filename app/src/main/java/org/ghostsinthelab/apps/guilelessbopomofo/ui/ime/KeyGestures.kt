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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import android.content.Context
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey

// The shortest gap between two taps that still counts as a double tap, as GestureDetector has it.
private const val DOUBLE_TAP_MIN_TIME_MS = 40L

// Once a held key starts repeating: the wait before the first repeat, then between repeats.
private const val REPEAT_INITIAL_DELAY_MS = 50L
private const val REPEAT_INTERVAL_MS = 100L

/**
 * Feeds touches on this element to [key], the way `GestureDetector` used to:
 *  - [VirtualKey.onDown] as soon as a finger lands;
 *  - [VirtualKey.onTap] when it lifts again, unless it was held past the long-press timeout;
 *  - for a key that [detects double taps][VirtualKey.detectsDoubleTap], a second quick tap
 *    goes to [VirtualKey.onDoubleTap] (as it lands) and not to [VirtualKey.onTap];
 *  - [VirtualKey.onLongPress] once held past the long-press timeout, and then, for a key that
 *    [repeats][VirtualKey.repeatsWhileHeld], [VirtualKey.onRepeat] every so often until it is let go.
 *
 * Presses are reported to [interactionSource], for the ripple.
 */
fun Modifier.keyGestures(
    key: VirtualKey,
    context: Context,
    interactionSource: MutableInteractionSource,
): Modifier = pointerInput(key) {
    // When the last tap that could start a double tap was let go of.
    var lastTapUpTime: Long? = null

    coroutineScope {
        val gestureScope = this
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            val press = PressInteraction.Press(down.position)
            interactionSource.tryEmit(press)

            val isSecondTap = key.detectsDoubleTap && lastTapUpTime?.let {
                down.uptimeMillis - it in DOUBLE_TAP_MIN_TIME_MS..viewConfiguration.doubleTapTimeoutMillis
            } == true
            lastTapUpTime = null

            key.onDown(context)
            if (isSecondTap) key.onDoubleTap(context)

            var up: PointerInputChange? = null
            try {
                up = if (key.detectsLongPress || key.repeatsWhileHeld) {
                    awaitUpOrLongPress(key, context, gestureScope)
                } else {
                    waitForUpOrCancellation()
                }
            } finally {
                interactionSource.tryEmit(
                    if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press)
                )
            }

            if (up != null && !isSecondTap) {
                key.onTap(context)
                lastTapUpTime = up.uptimeMillis
            }
        }
    }
}

/**
 * The lifting pointer if the key was let go of before the long-press timeout; null once it
 * has been long pressed (and let go of), or the gesture was cancelled.
 */
private suspend fun AwaitPointerEventScope.awaitUpOrLongPress(
    key: VirtualKey,
    context: Context,
    gestureScope: CoroutineScope,
): PointerInputChange? {
    try {
        return withTimeout(viewConfiguration.longPressTimeoutMillis) { waitForUpOrCancellation() }
    } catch (_: PointerEventTimeoutCancellationException) {
        if (key.detectsLongPress) key.onLongPress(context)
        // Deleting goes through libchewing, which may only be touched from the main thread;
        // the gesture scope runs there.
        val repeating = if (key.repeatsWhileHeld) {
            gestureScope.launch {
                delay(REPEAT_INITIAL_DELAY_MS)
                while (isActive) {
                    key.onRepeat(context)
                    delay(REPEAT_INTERVAL_MS)
                }
            }
        } else {
            null
        }
        try {
            awaitRelease()
        } finally {
            repeating?.cancel()
        }
        return null
    }
}

private suspend fun AwaitPointerEventScope.awaitRelease() {
    do {
        val event = awaitPointerEvent()
    } while (event.changes.any { it.pressed })
}
