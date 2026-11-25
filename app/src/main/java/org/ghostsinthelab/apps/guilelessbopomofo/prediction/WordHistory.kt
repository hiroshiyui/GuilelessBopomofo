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

import java.util.Locale

/**
 * The user's own English word history: how many times each suggestion was chosen, and after
 * which word. Only plain words go in (letters and apostrophes, see [isStorable]), and the
 * caller only records words from the built-in word list, so nothing else the user types can
 * ever end up here.
 *
 * Holds at most [capacity] pairs, the least recently chosen one goes first. Not thread safe,
 * meant to be used from the main thread only.
 */
class WordHistory(private val capacity: Int = DEFAULT_CAPACITY) {

    /** [previous] is lower case, or empty when the word was chosen with no word before it. */
    data class Entry(val previous: String, val word: String, val count: Int)

    private data class Choice(val previous: String, val word: String)

    // access-ordered: the least recently chosen pair is the first one iterated, and evicted
    private val counts = LinkedHashMap<Choice, Int>(INITIAL_MAP_CAPACITY, LOAD_FACTOR, true)

    val size: Int get() = counts.size

    /**
     * Counts one more choice of [word] right after [previous], or with nothing before it when
     * [previous] is empty. Anything that is not a plain word is ignored.
     */
    fun record(previous: String, word: String) {
        if (!isStorable(word) || (previous.isNotEmpty() && !isStorable(previous))) return
        val choice = Choice(previous.lowercase(Locale.ROOT), word)
        counts[choice] = ((counts[choice] ?: 0) + 1).coerceAtMost(MAX_COUNT)
        trimToCapacity()
    }

    /** The words chosen after [previous], most often chosen first, then the latest. */
    fun followers(previous: String): List<String> {
        if (previous.isEmpty()) return emptyList()
        val key = previous.lowercase(Locale.ROOT)
        return ranked(counts.entries.filter { it.key.previous == key }.map { it.key.word to it.value })
    }

    /**
     * The words chosen that [prefix] could become, whatever came before them, most often chosen
     * first, then the latest. Spelled as they were stored, never [prefix] itself.
     */
    fun words(prefix: String): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val key = prefix.lowercase(Locale.ROOT)
        val totals = LinkedHashMap<String, Int>()
        counts.forEach { (choice, count) ->
            val word = choice.word.lowercase(Locale.ROOT)
            if (word.startsWith(key) && word != key) {
                // re-inserted so that the order stays the one of the latest choice
                val total = (totals.remove(choice.word) ?: 0) + count
                totals[choice.word] = total.coerceAtMost(MAX_COUNT)
            }
        }
        return ranked(totals.toList())
    }

    // most chosen first; among equals, the later in the map, the more recently chosen
    private fun ranked(choices: List<Pair<String, Int>>): List<String> =
        choices.withIndex()
            .sortedWith(compareByDescending<IndexedValue<Pair<String, Int>>> { it.value.second }
                .thenByDescending { it.index })
            .map { it.value.first }
            .distinct()

    /** Every pair, the least recently chosen first. */
    fun entries(): List<Entry> = counts.map { (choice, count) -> Entry(choice.previous, choice.word, count) }

    /**
     * Takes in [older] entries, read from storage while choices were already being made: they
     * count as less recent than anything chosen since.
     */
    fun mergeOlder(older: List<Entry>) {
        val newer = counts.toList()
        counts.clear()
        older.forEach { entry ->
            if (entry.count > 0 && isStorable(entry.word) && (entry.previous.isEmpty() || isStorable(entry.previous))) {
                counts[Choice(entry.previous.lowercase(Locale.ROOT), entry.word)] = entry.count.coerceAtMost(MAX_COUNT)
            }
        }
        newer.forEach { (choice, count) ->
            counts[choice] = ((counts.remove(choice) ?: 0) + count).coerceAtMost(MAX_COUNT)
        }
        trimToCapacity()
    }

    private fun trimToCapacity() {
        val iterator = counts.entries.iterator()
        while (counts.size > capacity && iterator.hasNext()) {
            iterator.next()
            iterator.remove()
        }
    }

    /** The history as `previous<TAB>word<TAB>count` lines in UTF-8, the least recent first. */
    fun encode(): ByteArray = buildString {
        counts.forEach { (choice, count) -> append(choice.previous).append('\t').append(choice.word).append('\t').append(count).append('\n') }
    }.toByteArray(Charsets.UTF_8)

    companion object {
        const val DEFAULT_CAPACITY = 2000

        // Long enough for any real English word, short enough to keep out anything else.
        const val MAX_WORD_LENGTH = 32

        private const val MAX_COUNT = 1_000_000
        private const val INITIAL_MAP_CAPACITY = 64
        private const val LOAD_FACTOR = 0.75f

        /** Whether [word] is a plain word that may be kept: letters and apostrophes only. */
        fun isStorable(word: String): Boolean =
            word.length in 1..MAX_WORD_LENGTH &&
                    word.all { it in 'a'..'z' || it in 'A'..'Z' || it == '\'' } &&
                    word.any { it != '\'' }

        /**
         * Reads what [encode] wrote.
         *
         * @throws IllegalArgumentException if it is not in that form.
         */
        fun decode(bytes: ByteArray): List<Entry> =
            bytes.toString(Charsets.UTF_8).lineSequence()
                .filter { it.isNotEmpty() }
                .map(::parseLine)
                .toList()

        // No line is ever quoted in the message: it would be the user's history in the log.
        private fun parseLine(line: String): Entry {
            val fields = line.split('\t')
            require(fields.size == FIELDS) { "Malformed word history line" }
            val (previous, word, count) = fields
            require(isStorable(word) && (previous.isEmpty() || isStorable(previous))) { "Malformed word history word" }
            val number = requireNotNull(count.toIntOrNull()?.takeIf { it > 0 }) { "Malformed word history count" }
            return Entry(previous, word, number)
        }

        private const val FIELDS = 3
    }
}
