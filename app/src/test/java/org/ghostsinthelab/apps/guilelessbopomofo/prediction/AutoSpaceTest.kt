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

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSpaceTest {

    @Test
    fun nothingPendingAtFirst() {
        val autoSpace = AutoSpace()
        assertEquals(0, autoSpace.lookbehind)
        assertFalse(autoSpace.takesBack("hello ", ","))
    }

    @Test
    fun takesBackBeforePunctuationRightAfterTheWord() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        assertEquals("hello ".length, autoSpace.lookbehind)
        assertTrue(autoSpace.takesBack("say hello ", ","))
    }

    @Test
    fun onlyOnce() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        assertTrue(autoSpace.takesBack("hello ", "."))
        assertEquals(0, autoSpace.lookbehind)
        assertFalse(autoSpace.takesBack("hello ", "."))
    }

    @Test
    fun notBeforeALetter() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        assertFalse(autoSpace.takesBack("hello ", "w"))
        // and that has settled it, a later comma is the user's own business
        assertFalse(autoSpace.takesBack("hello w", ","))
    }

    @Test
    fun notWhenTheCursorHasMovedToAnotherSpace() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        assertFalse(autoSpace.takesBack("the ", ","))
    }

    @Test
    fun notWhenTheTextFieldGivesNothing() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        assertFalse(autoSpace.takesBack(null, ","))
    }

    @Test
    fun forgetDropsIt() {
        val autoSpace = AutoSpace()
        autoSpace.committed("hello")
        autoSpace.forget()
        assertEquals(0, autoSpace.lookbehind)
        assertFalse(autoSpace.takesBack("hello ", ","))
    }
}
