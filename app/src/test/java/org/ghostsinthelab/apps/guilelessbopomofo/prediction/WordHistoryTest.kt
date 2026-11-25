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

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordHistoryTest {

    @Test
    fun followers_mostChosenFirst_thenTheLatest() {
        val history = WordHistory()
        history.record("go", "home")
        history.record("go", "ahead")
        history.record("go", "ahead")
        history.record("go", "back")
        assertEquals(listOf("ahead", "back", "home"), history.followers("go"))
    }

    @Test
    fun followers_ignoreTheCaseOfThePreviousWord() {
        val history = WordHistory()
        history.record("Go", "ahead")
        assertEquals(listOf("ahead"), history.followers("GO"))
        assertEquals(emptyList<String>(), history.followers(""))
    }

    @Test
    fun words_addUpEveryChoice_whateverCameBefore() {
        val history = WordHistory()
        history.record("go", "ahead")
        history.record("", "ahead")
        history.record("", "about")
        history.record("", "about")
        history.record("", "able")
        history.record("", "ahead")
        assertEquals(listOf("ahead", "about", "able"), history.words("a"))
        assertEquals(listOf("ahead"), history.words("Ah"))
    }

    @Test
    fun words_neverOfferTheTypedWordItself() {
        val history = WordHistory()
        history.record("", "ahead")
        assertEquals(emptyList<String>(), history.words("ahead"))
        assertEquals(emptyList<String>(), history.words(""))
    }

    @Test
    fun onlyPlainWordsAreKept() {
        val history = WordHistory()
        history.record("", "hunter2")
        history.record("", "me@example.com")
        history.record("", "two words")
        history.record("", "'")
        history.record("", "a".repeat(WordHistory.MAX_WORD_LENGTH + 1))
        history.record("1234", "ahead")
        assertEquals(0, history.size)
        history.record("", "don't")
        assertEquals(1, history.size)
    }

    @Test
    fun theLeastRecentlyChosen_goFirst_onceFull() {
        val history = WordHistory(capacity = 2)
        history.record("", "able")
        history.record("", "about")
        history.record("", "able")
        history.record("", "ahead")
        assertEquals(listOf("able", "ahead"), history.words("a").sorted())
    }

    @Test
    fun encodeThenDecode_givesTheSameEntries() {
        val history = WordHistory()
        history.record("go", "ahead")
        history.record("go", "ahead")
        history.record("", "I")
        val decoded = WordHistory.decode(history.encode())
        assertEquals(
            listOf(WordHistory.Entry("go", "ahead", 2), WordHistory.Entry("", "I", 1)),
            decoded
        )
    }

    @Test
    fun mergeOlder_addsUp_andKeepsNewerChoicesMoreRecent() {
        val history = WordHistory()
        history.record("go", "home")
        history.mergeOlder(listOf(WordHistory.Entry("go", "ahead", 1), WordHistory.Entry("go", "home", 2)))
        assertEquals(
            listOf(WordHistory.Entry("go", "ahead", 1), WordHistory.Entry("go", "home", 3)),
            history.entries()
        )
    }

    @Test
    fun mergeOlder_dropsAnythingThatIsNotAPlainWord() {
        val history = WordHistory()
        history.mergeOlder(listOf(WordHistory.Entry("", "hunter2", 1), WordHistory.Entry("go", "ahead", 0)))
        assertEquals(0, history.size)
    }

    @Test
    fun decode_rejectsMalformedLines_withoutQuotingThem() {
        listOf("go\tahead", "go\tahead\tmany", "go\tahead\t0", "go\thunter2\t1").forEach { line ->
            try {
                WordHistory.decode(line.toByteArray())
                throw AssertionError("decoded")
            } catch (exception: IllegalArgumentException) {
                assertFalse(exception.message.orEmpty().contains("ahead") || exception.message.orEmpty().contains("hunter2"))
            }
        }
    }

    @Test
    fun isStorable() {
        assertTrue(WordHistory.isStorable("ahead"))
        assertTrue(WordHistory.isStorable("o'clock"))
        assertFalse(WordHistory.isStorable(""))
        assertFalse(WordHistory.isStorable("naïve"))
        assertFalse(WordHistory.isStorable("你好"))
    }
}
