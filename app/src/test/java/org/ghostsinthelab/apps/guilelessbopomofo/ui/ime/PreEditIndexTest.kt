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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The pre-edit buffer's cursor counts characters, its text UTF-16 units. */
class PreEditIndexTest {
    // 𠮷 is outside the BMP: one character, two UTF-16 units.
    private val text = "𠮷好"

    @Test
    fun utf16Offset_mapsToTheCharacterItFallsAt() {
        assertEquals(0, text.characterIndexOf(0))
        assertEquals(1, text.characterIndexOf(2))
        assertEquals(2, text.characterIndexOf(3))
        assertEquals("past the end clamps", 2, text.characterIndexOf(99))
    }

    @Test
    fun character_mapsToAllOfItsUtf16Units() {
        assertEquals(0..1, text.utf16RangeOfCharacter(0))
        assertEquals(2..2, text.utf16RangeOfCharacter(1))
        assertNull(text.utf16RangeOfCharacter(2))
        assertNull(text.utf16RangeOfCharacter(-1))
    }

    @Test
    fun bmpText_isOneUnitPerCharacter() {
        assertEquals(1..1, "你好".utf16RangeOfCharacter(1))
        assertEquals(1, "你好".characterIndexOf(1))
    }
}
