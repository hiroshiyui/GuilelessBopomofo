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

import java.io.InputStream
import java.util.Locale

/**
 * Knows which words most often follow a given one in Simple English Wikipedia
 * (https://simple.wikipedia.org/), an encyclopedia rather than a chat, so it is better at
 * `United States` than at `What's up`.
 *
 * The list is built by `tools/english-bigrams/build.sh`.
 */
class BigramPredictor private constructor(private val followers: Map<String, List<String>>) {

    /**
     * @return the words that follow [previous] the most, most frequent first and spelled as
     * the dictionary does, or nothing if [previous] is not known.
     */
    fun followers(previous: String): List<String> =
        followers[previous.lowercase(Locale.ROOT)].orEmpty()

    companion object {
        const val ASSET_PATH = "english/wikipedia-bigrams.txt"

        /**
         * Reads a list of `previous<TAB>follower follower ...` lines, and closes [input] afterwards.
         *
         * @throws IllegalArgumentException if a line is not in that form.
         */
        fun load(input: InputStream): BigramPredictor {
            val followers = input.bufferedReader().useLines { lines ->
                lines.filter { it.isNotBlank() }.associate(::parseLine)
            }
            return BigramPredictor(followers)
        }

        private fun parseLine(line: String): Pair<String, List<String>> {
            val (previous, followers) = line.split('\t').takeIf { it.size == 2 && it.all(String::isNotBlank) }
                ?: throw IllegalArgumentException("Malformed bigram list line: \"$line\"")
            return previous.lowercase(Locale.ROOT) to followers.split(' ').filter { it.isNotEmpty() }
        }
    }
}
