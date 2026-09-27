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
 * Predicts from the SCOWL word list (http://wordlist.aspell.net/), which tells how common a
 * word is only by the size of the smallest list it made it into. Words from a smaller list
 * come first, then the shorter ones, since those are the ones worth a tap the most.
 *
 * The list is built by `tools/english-wordlist/build.sh`.
 */
class ScowlWordPredictor private constructor(private val entries: List<Entry>) : WordPredictor {

    private class Entry(val word: String, val level: Int) {
        val key: String = word.lowercase(Locale.ROOT)
    }

    override fun suggest(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty() || limit <= 0) return emptyList()

        val key = prefix.lowercase(Locale.ROOT)
        return entries.subList(firstIndexNotBelow(key), entries.size)
            .takeWhile { it.key.startsWith(key) }
            .sortedWith(RANKING)
            .asSequence()
            .map { EnglishWords.matchCase(it.word, prefix) }
            .filter { it != prefix }
            .take(limit)
            .toList()
    }

    // binary search over the keys, which are kept sorted
    private fun firstIndexNotBelow(key: String): Int {
        var low = 0
        var high = entries.size
        while (low < high) {
            val middle = (low + high) ushr 1
            if (entries[middle].key < key) low = middle + 1 else high = middle
        }
        return low
    }

    companion object {
        const val ASSET_PATH = "english/scowl-words.txt"

        private val RANKING: Comparator<Entry> =
            compareBy<Entry> { it.level }.thenBy { it.key.length }.thenBy { it.key }

        /**
         * Reads a list of `word<TAB>level` lines, and closes [input] afterwards.
         *
         * @throws IllegalArgumentException if a line is not in that form.
         */
        fun load(input: InputStream): ScowlWordPredictor {
            val entries = input.bufferedReader().useLines { lines ->
                lines.filter { it.isNotBlank() }.map(::parseEntry).toList()
            }
            return ScowlWordPredictor(entries.sortedBy { it.key })
        }

        private fun parseEntry(line: String): Entry {
            val (word, level) = line.split('\t').takeIf { it.size == 2 }
                ?: throw IllegalArgumentException("Malformed word list line: \"$line\"")
            return Entry(
                word,
                level.toIntOrNull() ?: throw IllegalArgumentException("Malformed word level: \"$line\"")
            )
        }
    }
}
