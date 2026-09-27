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

import android.text.InputType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnglishWordsTest {

    // --- trailingWord ---

    @Test
    fun trailingWord_ofPlainEnglish() {
        assertEquals("wor", EnglishWords.trailingWord("hello wor"))
    }

    @Test
    fun trailingWord_stopsAtHanCharacters() {
        assertEquals("Andr", EnglishWords.trailingWord("我喜歡Andr"))
    }

    @Test
    fun trailingWord_keepsContractions() {
        assertEquals("don't", EnglishWords.trailingWord("I don't"))
    }

    @Test
    fun trailingWord_dropsLeadingQuotes() {
        assertEquals("hel", EnglishWords.trailingWord("say 'hel"))
    }

    @Test
    fun trailingWord_isEmptyAfterSpaceOrPunctuation() {
        assertEquals("", EnglishWords.trailingWord("hello "))
        assertEquals("", EnglishWords.trailingWord("hello,"))
        assertEquals("", EnglishWords.trailingWord("你好。"))
        assertEquals("", EnglishWords.trailingWord(""))
    }

    @Test
    fun trailingWord_ignoresDigits() {
        assertEquals("", EnglishWords.trailingWord("abc1"))
    }

    // --- matchCase ---

    @Test
    fun matchCase_lowercasePrefixKeepsDictionarySpelling() {
        assertEquals("hello", EnglishWords.matchCase("hello", "he"))
        assertEquals("I", EnglishWords.matchCase("I", "i"))
        assertEquals("Taiwan", EnglishWords.matchCase("Taiwan", "tai"))
    }

    @Test
    fun matchCase_capitalizedPrefixCapitalizes() {
        assertEquals("Hello", EnglishWords.matchCase("hello", "He"))
        assertEquals("Hello", EnglishWords.matchCase("hello", "H"))
    }

    @Test
    fun matchCase_allCapitalsPrefixShouts() {
        assertEquals("HELLO", EnglishWords.matchCase("hello", "HE"))
    }

    // --- fieldAllowsSuggestions ---

    @Test
    fun plainTextFields_allowSuggestions() {
        assertTrue(EnglishWords.fieldAllowsSuggestions(InputType.TYPE_CLASS_TEXT))
        assertTrue(
            EnglishWords.fieldAllowsSuggestions(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            )
        )
    }

    @Test
    fun passwordFields_refuseSuggestions() {
        listOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        ).forEach { variation ->
            assertFalse(
                "variation $variation",
                EnglishWords.fieldAllowsSuggestions(InputType.TYPE_CLASS_TEXT or variation)
            )
        }
    }

    @Test
    fun noSuggestionsFlag_isRespected() {
        assertFalse(
            EnglishWords.fieldAllowsSuggestions(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            )
        )
    }

    @Test
    fun nonTextFields_refuseSuggestions() {
        assertFalse(EnglishWords.fieldAllowsSuggestions(InputType.TYPE_CLASS_NUMBER))
        assertFalse(EnglishWords.fieldAllowsSuggestions(InputType.TYPE_CLASS_PHONE))
        assertFalse(EnglishWords.fieldAllowsSuggestions(InputType.TYPE_NULL))
    }
}
