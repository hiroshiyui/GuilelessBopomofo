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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Locale

class BigramPredictorTest {

    private fun predictorOf(vararg lines: String): BigramPredictor =
        BigramPredictor.load(lines.joinToString("\n").byteInputStream())

    @Test
    fun followersComeInTheListedOrder() {
        val predictor = predictorOf("as\twell long soon", "united\tStates Kingdom")
        assertEquals(listOf("well", "long", "soon"), predictor.followers("as"))
        assertEquals(listOf("States", "Kingdom"), predictor.followers("united"))
    }

    @Test
    fun lookupIgnoresCase() {
        val predictor = predictorOf("what's\tthe happening")
        assertEquals(listOf("the", "happening"), predictor.followers("What's"))
        assertEquals(listOf("the", "happening"), predictor.followers("WHAT'S"))
    }

    @Test
    fun unknownWordGivesNothing() {
        assertEquals(emptyList<String>(), predictorOf("as\twell").followers("xyzzy"))
        assertEquals(emptyList<String>(), predictorOf("as\twell").followers(""))
    }

    @Test
    fun blankLinesAreSkipped() {
        assertEquals(listOf("well"), predictorOf("", "as\twell", "  ").followers("as"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun lineWithoutFollowers_isRejected() {
        predictorOf("as")
    }

    @Test(expected = IllegalArgumentException::class)
    fun lineWithBlankFollowers_isRejected() {
        predictorOf("as\t ")
    }

    @Test
    fun theInputIsClosed() {
        var closed = false
        val input = object : ByteArrayInputStream("as\twell".toByteArray()) {
            override fun close() {
                closed = true
                super.close()
            }
        }
        BigramPredictor.load(input)
        assertTrue(closed)
    }

    // --- the bigram list shipped in the app ---

    private val shippedPredictor: BigramPredictor by lazy {
        BigramPredictor.load(File("src/main/assets/${BigramPredictor.ASSET_PATH}").inputStream())
    }

    private val shippedWords: Set<String> by lazy {
        File("src/main/assets/${ScowlWordPredictor.ASSET_PATH}").readLines()
            .filter { it.isNotBlank() }
            .map { it.substringBefore('\t').lowercase(Locale.ROOT) }
            .toSet()
    }

    @Test
    fun shippedList_knowsEverydayWords() {
        for (word in listOf("the", "of", "in", "is")) {
            val followers = shippedPredictor.followers(word)
            assertTrue("$word: $followers", followers.isNotEmpty())
            assertTrue("$word: $followers", followers.size <= EnglishPrediction.MAX_SUGGESTIONS)
        }
    }

    @Test
    fun shippedList_knowsCommonPairs() {
        assertTrue("States" in shippedPredictor.followers("united"))
        assertTrue("well" in shippedPredictor.followers("as"))
    }

    @Test
    fun shippedList_onlyHasFollowersFromTheWordList() {
        File("src/main/assets/${BigramPredictor.ASSET_PATH}").useLines { lines ->
            lines.flatMap { it.substringAfter('\t').split(' ') }.forEach {
                // followers may be capitalized where the pair nearly always is, "United States"
                assertTrue(it, it.lowercase(Locale.ROOT) in shippedWords)
            }
        }
    }
}
