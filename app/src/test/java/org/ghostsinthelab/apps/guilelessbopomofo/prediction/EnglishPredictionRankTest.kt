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
import org.junit.Test

class EnglishPredictionRankTest {

    private fun rank(followers: List<String>, completions: List<String>, prefix: String, limit: Int = 16) =
        EnglishPrediction.rank(followers, completions, prefix, limit)

    @Test
    fun rightAfterASpace_theFollowersAreOffered() {
        assertEquals(listOf("well", "long"), rank(listOf("well", "long"), emptyList(), ""))
    }

    @Test
    fun rightAfterASpace_withoutFollowers_nothingIsOffered() {
        assertEquals(emptyList<String>(), rank(emptyList(), emptyList(), ""))
    }

    @Test
    fun whileTyping_matchingFollowersComeFirst_thenTheCompletions() {
        assertEquals(
            listOf("well", "was", "with", "would"),
            rank(listOf("long", "well", "soon"), listOf("was", "with", "would"), "w"),
        )
    }

    @Test
    fun whileTyping_withoutFollowers_completionsStayAsTheyAre() {
        assertEquals(listOf("was", "with"), rank(emptyList(), listOf("was", "with"), "w"))
    }

    @Test
    fun duplicatesAreOfferedOnce() {
        assertEquals(listOf("well", "was"), rank(listOf("well"), listOf("was", "well"), "w"))
    }

    @Test
    fun followersFollowTheTypedCase() {
        assertEquals(listOf("Well"), rank(listOf("well"), emptyList(), "W"))
        assertEquals(listOf("WELL"), rank(listOf("well"), emptyList(), "WE"))
        assertEquals(listOf("States"), rank(listOf("States"), emptyList(), "s"))
    }

    @Test
    fun theTypedWordItselfIsNotOffered() {
        assertEquals(listOf("wells"), rank(listOf("well", "wells"), emptyList(), "well"))
    }

    @Test
    fun theLimitIsKept() {
        assertEquals(listOf("well", "was"), rank(listOf("well"), listOf("was", "with"), "w", limit = 2))
        assertEquals(listOf("well"), rank(listOf("well", "long"), emptyList(), "", limit = 1))
    }
}
