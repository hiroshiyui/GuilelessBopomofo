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

package org.ghostsinthelab.apps.guilelessbopomofo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BopomofoLookupTest {
    @Test
    fun everyReading_coversEveryCharacter_evenWhenCapped() {
        // Six characters with four readings each: 4096 readings, far over the cap.
        val lists = List(6) { char -> List(4) { reading -> "cr" } }
        val readings = BopomofoLookup.cartesianProduct(lists, maxResults = 100)
        assertEquals(100, readings.size)
        assertTrue("every reading has six syllables", readings.all { it.split(' ').size == 6 })
    }

    @Test
    fun underTheCap_everyCombinationIsThere() {
        val readings = BopomofoLookup.cartesianProduct(listOf(listOf("a", "b"), listOf("c", "d")), maxResults = 100)
        assertEquals(listOf("a c", "a d", "b c", "b d"), readings)
    }
}
