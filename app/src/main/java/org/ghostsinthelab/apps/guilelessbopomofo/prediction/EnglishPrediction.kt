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

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.Locale

/**
 * Keeps the word list, and the list of which word follows which, in memory only while the
 * user has the prediction they serve turned on, and reads them off the main thread. Meant
 * to be used from the main thread only.
 *
 * @param scope where the lists are loaded in, it has to run on the main thread.
 * @param openWordList opens the SCOWL word list, [ScowlWordPredictor.load] closes it.
 * @param openBigramList opens the Wikipedia bigram list, [BigramPredictor.load] closes it.
 */
class EnglishPrediction(
    private val scope: CoroutineScope,
    private val openWordList: () -> InputStream,
    private val openBigramList: () -> InputStream,
) {
    private val logTag = "EnglishPrediction"

    private var predictor: WordPredictor? = null
    private var loadingJob: Job? = null

    private var bigramPredictor: BigramPredictor? = null
    private var bigramLoadingJob: Job? = null

    var isEnabled: Boolean = false
        set(value) {
            field = value
            if (value) load() else unload()
            updateBigrams()
        }

    /** Next-word prediction, which only works along with [isEnabled]. */
    var isNextWordEnabled: Boolean = false
        set(value) {
            field = value
            updateBigrams()
        }

    /**
     * Nothing, until the lists have been read.
     *
     * @param previous the word before the one being typed, if only spaces part them.
     * @param prefix the part of the word typed so far, empty right after a space.
     */
    fun suggest(previous: String, prefix: String): List<String> {
        val followers = previous.takeIf { it.isNotEmpty() }
            ?.let { bigramPredictor?.followers(it) }
            .orEmpty()
        val completions = predictor?.suggest(prefix, MAX_SUGGESTIONS).orEmpty()
        return rank(followers, completions, prefix, MAX_SUGGESTIONS)
    }

    private fun load() {
        if (predictor != null || loadingJob?.isActive == true) return

        loadingJob = scope.launch {
            predictor = readList("word list") { ScowlWordPredictor.load(openWordList()) }
        }
    }

    private fun unload() {
        loadingJob?.cancel()
        loadingJob = null
        predictor = null
    }

    private fun updateBigrams() {
        if (isEnabled && isNextWordEnabled) loadBigrams() else unloadBigrams()
    }

    private fun loadBigrams() {
        if (bigramPredictor != null || bigramLoadingJob?.isActive == true) return

        bigramLoadingJob = scope.launch {
            bigramPredictor = readList("bigram list") { BigramPredictor.load(openBigramList()) }
        }
    }

    private fun unloadBigrams() {
        bigramLoadingJob?.cancel()
        bigramLoadingJob = null
        bigramPredictor = null
    }

    private suspend fun <T> readList(name: String, read: () -> T): T? =
        try {
            withContext(Dispatchers.IO) { read() }
        } catch (exception: IOException) {
            Log.e(logTag, "Failed to read the $name", exception)
            null
        } catch (exception: IllegalArgumentException) {
            Log.e(logTag, "The $name is malformed", exception)
            null
        }

    companion object {
        /** How many words the suggestion strip offers at most, the rest is a swipe away. */
        const val MAX_SUGGESTIONS = 16

        /**
         * Right after a space, the words that follow the previous one. While a word is being
         * typed, those of them it could become come first, in the letter case [prefix]
         * suggests, then the plain [completions] of it.
         */
        internal fun rank(followers: List<String>, completions: List<String>, prefix: String, limit: Int): List<String> {
            if (prefix.isEmpty()) return followers.take(limit)

            val key = prefix.lowercase(Locale.ROOT)
            val likelyCompletions = followers
                .filter { it.lowercase(Locale.ROOT).let { word -> word.startsWith(key) && word != key } }
                .map { EnglishWords.matchCase(it, prefix) }
            return (likelyCompletions + completions).distinct().take(limit)
        }
    }
}
