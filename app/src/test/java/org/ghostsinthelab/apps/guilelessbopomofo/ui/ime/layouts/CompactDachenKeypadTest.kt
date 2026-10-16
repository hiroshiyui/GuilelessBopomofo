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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts

import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoSoftKeyboards
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompactDachenKeypadTest {
    private val keys = CompactDachenKeypad.rows.flatten()

    @Test
    fun everySymbolAndTone_isOnTheKeypadExactlyOnce() {
        val glyphs = keys.flatMap { it.glyphs }
        assertEquals(BopomofoGlyph.entries.toSet(), glyphs.toSet())
        assertEquals("no symbol on two keys", glyphs.size, glyphs.toSet().size)
    }

    @Test
    fun everyGlyph_typesTheKeyThatCarriesItOnTheDachenKeyboard() {
        val dachenKeyCodeByIcon = KeyboardSpecs.of(SoftKeyboard.DACHEN, KeyboardVariant.DEFAULT).rows.flatten()
            .filterIsInstance<KeySpec.Character>()
            .associate { it.icon to it.keyCode }
        BopomofoGlyph.entries.forEach { glyph ->
            assertEquals("$glyph", dachenKeyCodeByIcon[glyph.icon], glyph.dachenKeyCode)
        }
    }

    @Test
    fun keypad_isThreeKeysWideAndLeavesTheHashSlotToPunctuation() {
        assertEquals(listOf(3, 3, 3, 2), CompactDachenKeypad.rows.map { it.size })
    }

    @Test
    fun oneKey_followsThePhoneArrangement() {
        val one = CompactDachenKeypad.rows[0][0]
        assertEquals(BopomofoGlyph.B, one.tap)
        assertEquals(BopomofoGlyph.P, one.glyphFor(SwipeDirection.UP))
        assertEquals(BopomofoGlyph.M, one.glyphFor(SwipeDirection.RIGHT))
        assertEquals(BopomofoGlyph.F, one.glyphFor(SwipeDirection.LEFT))
        assertNull(one.glyphFor(SwipeDirection.DOWN))
    }

    @Test
    fun compactDachen_tellsChewingTheDachenLayout() {
        assertEquals("KB_DEFAULT", BopomofoSoftKeyboards.KB_DACHEN_COMPACT.chewingLayout)
        assertEquals(BopomofoSoftKeyboards.KB_DACHEN_COMPACT, BopomofoSoftKeyboards.fromLayout("KB_DACHEN_COMPACT"))
        assertEquals(BopomofoSoftKeyboards.KB_DEFAULT, BopomofoSoftKeyboards.fromLayout("KB_UNHEARD_OF"))
    }
}
