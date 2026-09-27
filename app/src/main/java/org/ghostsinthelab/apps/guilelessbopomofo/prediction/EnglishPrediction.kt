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

/**
 * Keeps the word list in memory only while the user has English word prediction turned on,
 * and reads it off the main thread. Meant to be used from the main thread only.
 *
 * @param scope where the word list is loaded in, it has to run on the main thread.
 * @param openWordList opens the SCOWL word list, [ScowlWordPredictor.load] closes it.
 */
class EnglishPrediction(
    private val scope: CoroutineScope,
    private val openWordList: () -> InputStream,
) {
    private val logTag = "EnglishPrediction"

    private var predictor: WordPredictor? = null
    private var loadingJob: Job? = null

    var isEnabled: Boolean = false
        set(value) {
            field = value
            if (value) load() else unload()
        }

    /** Nothing, until the word list has been read. */
    fun suggest(prefix: String): List<String> =
        predictor?.suggest(prefix, MAX_SUGGESTIONS).orEmpty()

    private fun load() {
        if (predictor != null || loadingJob?.isActive == true) return

        loadingJob = scope.launch {
            predictor = try {
                withContext(Dispatchers.IO) { ScowlWordPredictor.load(openWordList()) }
            } catch (exception: IOException) {
                Log.e(logTag, "Failed to read the word list", exception)
                null
            } catch (exception: IllegalArgumentException) {
                Log.e(logTag, "The word list is malformed", exception)
                null
            }
        }
    }

    private fun unload() {
        loadingJob?.cancel()
        loadingJob = null
        predictor = null
    }

    companion object {
        /** How many words the suggestion strip offers at most, the rest is a swipe away. */
        const val MAX_SUGGESTIONS = 16
    }
}
