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
import java.io.File

class ScowlWordPredictorTest {

    private fun predictorOf(vararg lines: String): ScowlWordPredictor =
        ScowlWordPredictor.load(lines.joinToString("\n").byteInputStream())

    @Test
    fun commonerWordsComeFirst_thenShorterOnes() {
        val predictor = predictorOf("helpful\t35", "help\t10", "hello\t10", "helm\t50", "helping\t20")
        assertEquals(listOf("help", "hello", "helping"), predictor.suggest("hel", 3))
    }

    @Test
    fun theUnsortedListIsSortedOnLoad() {
        val predictor = predictorOf("zebra\t10", "apple\t10", "apply\t20")
        assertEquals(listOf("apple", "apply"), predictor.suggest("app", 3))
    }

    @Test
    fun theTypedWordItselfIsNotSuggested() {
        val predictor = predictorOf("the\t10", "then\t10", "there\t10")
        assertEquals(listOf("then", "there"), predictor.suggest("the", 3))
    }

    @Test
    fun matchingIgnoresCase_andFollowsTheTypedCase() {
        val predictor = predictorOf("Taiwan\t50", "tail\t35")
        assertEquals(listOf("Tail", "Taiwan"), predictor.suggest("Ta", 3))
        assertEquals(listOf("tail", "Taiwan"), predictor.suggest("ta", 3))
        assertEquals(listOf("TAIL", "TAIWAN"), predictor.suggest("TA", 3))
    }

    @Test
    fun pronounI_isCapitalizedFromLowercase() {
        val predictor = predictorOf("I\t10", "if\t10")
        assertEquals(listOf("I", "if"), predictor.suggest("i", 3))
    }

    @Test
    fun noMatchesGivesNothing() {
        assertEquals(emptyList<String>(), predictorOf("hello\t10").suggest("xyz", 3))
    }

    @Test
    fun emptyPrefixOrLimitGivesNothing() {
        val predictor = predictorOf("hello\t10")
        assertEquals(emptyList<String>(), predictor.suggest("", 3))
        assertEquals(emptyList<String>(), predictor.suggest("h", 0))
    }

    @Test
    fun blankLinesAreSkipped() {
        assertEquals(listOf("hello"), predictorOf("", "hello\t10", "  ").suggest("he", 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun lineWithoutLevel_isRejected() {
        predictorOf("hello")
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonNumericLevel_isRejected() {
        predictorOf("hello\tten")
    }

    // --- the word list shipped in the app ---

    private val shippedPredictor: ScowlWordPredictor by lazy {
        ScowlWordPredictor.load(File("src/main/assets/${ScowlWordPredictor.ASSET_PATH}").inputStream())
    }

    @Test
    fun shippedList_offersEverydayWords() {
        val suggestions = shippedPredictor.suggest("hel", 3)
        assertEquals(3, suggestions.size)
        assertTrue(suggestions.toString(), "help" in suggestions)
    }

    @Test
    fun shippedList_fillsAWholeStrip_mostLikelyFirst() {
        val suggestions = shippedPredictor.suggest("co", EnglishPrediction.MAX_SUGGESTIONS)
        assertEquals(EnglishPrediction.MAX_SUGGESTIONS, suggestions.size)
        assertEquals(suggestions.size, suggestions.toSet().size)
        assertTrue(suggestions.toString(), suggestions.all { it.startsWith("co", ignoreCase = true) })
    }

    @Test
    fun shippedList_reachesTheLargerSizes() {
        // a size 55 word, only there since the list goes beyond size 50
        assertTrue("abseiling" in shippedPredictor.suggest("abseil", EnglishPrediction.MAX_SUGGESTIONS))
    }

    @Test
    fun shippedList_offersContractions() {
        assertTrue("don't" in shippedPredictor.suggest("don", 3))
    }
}
