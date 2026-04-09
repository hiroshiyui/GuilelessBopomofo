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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.util.Log

/**
 * Provides character-to-bopomofo reverse lookup using libchewing's word.csv dictionary.
 * Supports polyphone characters (多音字) by returning all possible readings.
 */
object BopomofoLookup {
    private const val logTag = "BopomofoLookup"
    private var charToBopomofoMap: Map<Char, List<String>>? = null

    fun init(context: Context) {
        if (charToBopomofoMap != null) return
        charToBopomofoMap = loadWordCsv(context)
        Log.d(logTag, "Loaded ${charToBopomofoMap?.size} character entries")
    }

    /**
     * Returns all possible bopomofo readings for a single character.
     */
    fun lookup(char: Char): List<String> {
        return charToBopomofoMap?.get(char) ?: emptyList()
    }

    /**
     * Generates all possible bopomofo reading combinations for a phrase.
     * Each character may have multiple readings (多音字), so the result
     * is the cartesian product of all per-character readings.
     *
     * Returns at most [maxCombinations] results to prevent memory issues.
     */
    fun generateCombinations(phrase: String, maxCombinations: Int = 100): List<String> {
        if (phrase.isEmpty()) return emptyList()

        val readingsPerChar = phrase.map { char ->
            val readings = lookup(char)
            if (readings.isEmpty()) return emptyList()
            readings
        }

        return cartesianProduct(readingsPerChar, maxCombinations)
    }

    private fun cartesianProduct(lists: List<List<String>>, maxResults: Int): List<String> {
        var results = lists[0].map { it }
        for (i in 1 until lists.size) {
            val newResults = mutableListOf<String>()
            for (existing in results) {
                for (next in lists[i]) {
                    newResults.add("$existing $next")
                    if (newResults.size >= maxResults) return newResults
                }
            }
            results = newResults
        }
        return results
    }

    private fun loadWordCsv(context: Context): Map<Char, List<String>> {
        val map = mutableMapOf<Char, MutableList<String>>()
        try {
            context.assets.open("word.csv").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith("#") || line.isBlank()) return@forEach
                    val parts = line.split(",")
                    if (parts.size >= 3) {
                        val charStr = parts[0]
                        val bopomofo = parts[2]
                        // Only index single CJK characters, skip bopomofo symbols
                        if (charStr.length == 1 && bopomofo.isNotEmpty()) {
                            val char = charStr[0]
                            val list = map.getOrPut(char) { mutableListOf() }
                            if (!list.contains(bopomofo)) {
                                list.add(bopomofo)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to load word.csv", e)
        }
        return map
    }
}
