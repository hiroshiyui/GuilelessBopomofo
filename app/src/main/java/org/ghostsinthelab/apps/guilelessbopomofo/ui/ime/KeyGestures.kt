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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey

// The shortest gap between two taps that still counts as a double tap, as GestureDetector has it.
private const val DOUBLE_TAP_MIN_TIME_MS = 40L

// Once a held key starts repeating: the wait before the first repeat, then between repeats.
private const val REPEAT_INITIAL_DELAY_MS = 50L
private const val REPEAT_INTERVAL_MS = 100L

// How far a finger has to travel from where it landed for a swipe key to count it a swipe.
private val SWIPE_THRESHOLD = 20.dp

/**
 * Feeds touches on this element to [key], the way `GestureDetector` used to:
 *  - [VirtualKey.onDown] as soon as a finger lands;
 *  - [VirtualKey.onTap] when it lifts again, unless it was held past the long-press timeout
 *    (held that long, a key without a long press does nothing, as with GestureDetector) or
 *    slid farther than the touch slop first;
 *  - for a key that [detects double taps][VirtualKey.detectsDoubleTap], a second quick tap
 *    goes to [VirtualKey.onDoubleTap] (as it lands) and not to [VirtualKey.onTap];
 *  - [VirtualKey.onLongPress] once held past the long-press timeout, and then, for a key that
 *    [repeats][VirtualKey.repeatsWhileHeld], [VirtualKey.onRepeat] every so often until it is let go;
 *  - for a key that [detects swipes][VirtualKey.detectsSwipes], [VirtualKey.onSwipe] when the
 *    finger lifts far enough from where it landed, in whichever direction it mostly went, and
 *    [VirtualKey.onTap] otherwise. [onSwipeDirectionChange] follows the direction while the
 *    finger is still down (null when it is too close to call), and is reset once it lifts.
 *
 * Presses are reported to [interactionSource], for the ripple.
 */
fun Modifier.keyGestures(
    key: VirtualKey,
    context: Context,
    interactionSource: MutableInteractionSource,
    onSwipeDirectionChange: (SwipeDirection?) -> Unit = {},
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

            if (key.detectsSwipes) {
                handleSwipe(key, context, down, onSwipeDirectionChange, interactionSource, press)
                return@awaitEachGesture
            }

            var up: PointerInputChange? = null
            try {
                up = awaitUpOrLongPress(key, context, down, gestureScope)
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
    down: PointerInputChange,
    gestureScope: CoroutineScope,
): PointerInputChange? {
    try {
        return withTimeout(viewConfiguration.longPressTimeoutMillis) { awaitUpInPlace(down) }
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

/** How a swipe gesture ended: let go of, possibly [direction]-wards, or cancelled. */
private class SwipeEnd(val direction: SwipeDirection?)

private suspend fun AwaitPointerEventScope.handleSwipe(
    key: VirtualKey,
    context: Context,
    down: PointerInputChange,
    onSwipeDirectionChange: (SwipeDirection?) -> Unit,
    interactionSource: MutableInteractionSource,
    press: PressInteraction.Press,
) {
    var end: SwipeEnd? = null
    try {
        end = awaitSwipeEnd(down, onSwipeDirectionChange)
    } finally {
        onSwipeDirectionChange(null)
        interactionSource.tryEmit(
            if (end != null) PressInteraction.Release(press) else PressInteraction.Cancel(press)
        )
    }
    when (val direction = end?.direction) {
        null -> if (end != null) key.onTap(context)
        else -> key.onSwipe(context, direction)
    }
}

/**
 * Follows the finger that went [down] until it lifts, wherever it wanders, and says which
 * way it went; null if something else claimed the gesture.
 */
private suspend fun AwaitPointerEventScope.awaitSwipeEnd(
    down: PointerInputChange,
    onSwipeDirectionChange: (SwipeDirection?) -> Unit,
): SwipeEnd? {
    val threshold = maxOf(SWIPE_THRESHOLD.toPx(), viewConfiguration.touchSlop)
    var direction: SwipeDirection? = null
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == down.id } ?: return null
        if (change.isConsumed) return null
        val newDirection = swipeDirectionOf(change.position - down.position, threshold)
        if (newDirection != direction) {
            direction = newDirection
            onSwipeDirectionChange(direction)
        }
        change.consume()
        if (!change.pressed) return SwipeEnd(direction)
    }
}

/** The direction [offset] mostly points in, or null when it is shorter than [threshold] both ways. */
internal fun swipeDirectionOf(offset: Offset, threshold: Float): SwipeDirection? {
    val (dx, dy) = offset
    return when {
        abs(dx) < threshold && abs(dy) < threshold -> null
        abs(dx) >= abs(dy) -> if (dx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
        else -> if (dy > 0) SwipeDirection.DOWN else SwipeDirection.UP
    }
}

/**
 * The finger that went [down] lifting where it landed; null once it has slid farther than
 * the touch slop, or something else (a scrolling row of candidates, say) has claimed the
 * gesture. As with GestureDetector, a finger that has slid is neither a tap nor, later, a
 * long press.
 */
private suspend fun AwaitPointerEventScope.awaitUpInPlace(down: PointerInputChange): PointerInputChange? {
    while (true) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return null
        if (change.isConsumed) return null
        if (!change.pressed) return change
        if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) return null
    }
}

private suspend fun AwaitPointerEventScope.awaitRelease() {
    do {
        val event = awaitPointerEvent()
    } while (event.changes.any { it.pressed })
}
