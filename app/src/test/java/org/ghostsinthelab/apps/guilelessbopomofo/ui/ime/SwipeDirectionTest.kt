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

import androidx.compose.ui.geometry.Offset
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeDirectionTest {
    private val threshold = 10f

    @Test
    fun shortMoves_areNoSwipe() {
        assertNull(swipeDirectionOf(Offset(9f, -9f), threshold))
    }

    @Test
    fun theLongerAxis_decides() {
        assertEquals(SwipeDirection.UP, swipeDirectionOf(Offset(5f, -30f), threshold))
        assertEquals(SwipeDirection.DOWN, swipeDirectionOf(Offset(-5f, 30f), threshold))
        assertEquals(SwipeDirection.RIGHT, swipeDirectionOf(Offset(30f, 12f), threshold))
        assertEquals(SwipeDirection.LEFT, swipeDirectionOf(Offset(-30f, -12f), threshold))
    }
}
