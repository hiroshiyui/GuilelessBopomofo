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

package org.ghostsinthelab.apps.guilelessbopomofo.ui

import android.content.Context
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.keyGestures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** [keyGestures] has to answer touches the way GestureDetector did for the old key Views. */
@RunWith(AndroidJUnit4::class)
class KeyGesturesInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private class RecordingKey(
        override val detectsDoubleTap: Boolean = false,
        override val detectsLongPress: Boolean = false,
        override val repeatsWhileHeld: Boolean = false,
        override val detectsSwipes: Boolean = false,
    ) : VirtualKey {
        val calls = mutableListOf<String>()
        override fun onDown(context: Context) { calls += "down" }
        override fun onTap(context: Context) { calls += "tap" }
        override fun onDoubleTap(context: Context) { calls += "doubleTap" }
        override fun onLongPress(context: Context) { calls += "longPress" }
        override fun onRepeat(context: Context) { calls += "repeat" }
        override fun onSwipe(context: Context, direction: SwipeDirection) { calls += "swipe $direction" }
    }

    private fun setKey(key: VirtualKey) {
        composeRule.setContent {
            val context = LocalContext.current
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(64.dp)
                    .testTag(TAG)
                    .keyGestures(key, context, interactionSource)
            )
        }
    }

    @Test
    fun tap_downThenTap() {
        val key = RecordingKey()
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput { down(center); up() }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "tap"), key.calls)
    }

    @Test
    fun quickSecondTap_isADoubleTapAndNoTap_whenDetected() {
        val key = RecordingKey(detectsDoubleTap = true)
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput {
            down(center); advanceEventTime(50); up()
            advanceEventTime(100)
            down(center); advanceEventTime(50); up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "tap", "down", "doubleTap"), key.calls)
    }

    @Test
    fun quickSecondTap_isAnotherTap_whenDoubleTapsAreNotDetected() {
        val key = RecordingKey()
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput {
            down(center); advanceEventTime(50); up()
            advanceEventTime(100)
            down(center); advanceEventTime(50); up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "tap", "down", "tap"), key.calls)
    }

    @Test
    fun longPress_firesInsteadOfTap() {
        val key = RecordingKey(detectsLongPress = true)
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput {
            down(center); advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100); up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "longPress"), key.calls)
    }

    @Test
    fun hold_repeatsUntilReleased() {
        val key = RecordingKey(repeatsWhileHeld = true)
        setKey(key)
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag(TAG).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(HOLD_MILLIS)
        composeRule.onNodeWithTag(TAG).performTouchInput { up() }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals("down", key.calls.first())
        assertTrue("held key should repeat, got ${key.calls}", key.calls.count { it == "repeat" } >= 2)
        assertTrue("a held key is no tap", "tap" !in key.calls)
    }

    @Test
    fun swipe_inEachDirection() {
        val key = RecordingKey(detectsSwipes = true)
        setKey(key)
        val swipes = listOf(
            SwipeDirection.UP to Offset(0f, -1f),
            SwipeDirection.RIGHT to Offset(1f, 0f),
            SwipeDirection.LEFT to Offset(-1f, 0f),
            SwipeDirection.DOWN to Offset(0f, 1f),
        )
        swipes.forEach { (_, unit) ->
            composeRule.onNodeWithTag(TAG).performTouchInput {
                val distance = with(density) { SWIPE_DISTANCE.toPx() }
                swipe(center, center + unit * distance, durationMillis = 100)
            }
            composeRule.waitForIdle()
        }
        assertEquals(swipes.flatMap { (direction, _) -> listOf("down", "swipe $direction") }, key.calls)
    }

    @Test
    fun shortDrag_onASwipeKey_isATap() {
        val key = RecordingKey(detectsSwipes = true)
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput {
            swipe(center, center + Offset(with(density) { 4.dp.toPx() }, 0f), durationMillis = 100)
        }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "tap"), key.calls)
    }

    @Test
    fun swipe_thatLeavesTheKey_stillCounts() {
        val key = RecordingKey(detectsSwipes = true)
        setKey(key)
        composeRule.onNodeWithTag(TAG).performTouchInput {
            // The key is 64dp wide; end well past its right edge.
            swipe(center, center + Offset(with(density) { 120.dp.toPx() }, 0f), durationMillis = 100)
        }
        composeRule.waitForIdle()
        assertEquals(listOf("down", "swipe RIGHT"), key.calls)
    }

    private companion object {
        const val TAG = "key"
        const val HOLD_MILLIS = 1_500L
        val SWIPE_DISTANCE = 30.dp
    }
}
