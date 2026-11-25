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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.util.Locale

/** What the suggestion strip asks of English prediction; [EnglishPrediction] is the real one. */
interface EnglishPredictor {
    var isEnabled: Boolean

    /** Next-word prediction, which only works along with [isEnabled]. */
    var isNextWordEnabled: Boolean

    /**
     * Learning from the suggestions the user chooses, which only works along with [isEnabled].
     * Turning it off erases everything learned.
     */
    var isLearningEnabled: Boolean

    /** The user chose [word] from the strip, right after [previous] (empty if none). */
    fun learn(previous: String, word: String)

    /** Erases everything learned; learning, if it is on, starts afresh. */
    fun clearHistory()

    /**
     * @param previous the word before the one being typed, if only spaces part them.
     * @param prefix the part of the word typed so far, empty right after a space.
     */
    fun suggest(previous: String, prefix: String): List<String>
}

/**
 * Keeps the word list, and the list of which word follows which, in memory only while the
 * user has the prediction they serve turned on, and reads them off the main thread. Meant
 * to be used from the main thread only.
 *
 * @param scope where the lists are loaded in, it has to run on the main thread.
 * @param openWordList opens the SCOWL word list, [ScowlWordPredictor.load] closes it.
 * @param openBigramList opens the Wikipedia bigram list, [BigramPredictor.load] closes it.
 * @param openHistoryStore where the user's own word history is kept, only called once it is
 * needed, and only ever used off the main thread.
 * @param historyLock held around every use of the store, taken on the main thread; shared with
 * whatever else in the process touches the same store.
 */
class EnglishPrediction(
    private val scope: CoroutineScope,
    private val openWordList: () -> InputStream,
    private val openBigramList: () -> InputStream,
    openHistoryStore: () -> WordHistoryStore,
    private val historyLock: Mutex = WordHistoryVault.lock,
) : EnglishPredictor {
    private val logTag = "EnglishPrediction"

    private var predictor: WordPredictor? = null
    private var loadingJob: Job? = null

    private var bigramPredictor: BigramPredictor? = null
    private var bigramLoadingJob: Job? = null

    // The user's own history, in memory only while it is being learned and used.
    private val historyStore by lazy(openHistoryStore)
    private var history: WordHistory? = null
    private var historyLoadingJob: Job? = null
    private var historySavingJob: Job? = null
    private var historyChanged = false

    override var isEnabled: Boolean = false
        set(value) {
            field = value
            if (value) load() else unload()
            updateBigrams()
            updateHistory()
        }

    /** Next-word prediction, which only works along with [isEnabled]. */
    override var isNextWordEnabled: Boolean = false
        set(value) {
            field = value
            updateBigrams()
        }

    override var isLearningEnabled: Boolean = false
        set(value) {
            field = value
            if (value) updateHistory() else eraseHistory()
        }

    /**
     * Nothing, until the lists have been read.
     *
     * @param previous the word before the one being typed, if only spaces part them.
     * @param prefix the part of the word typed so far, empty right after a space.
     */
    override fun suggest(previous: String, prefix: String): List<String> {
        val followers = previous.takeIf { it.isNotEmpty() }
            ?.let { bigramPredictor?.followers(it) }
            .orEmpty()
        val completions = predictor?.suggest(prefix, MAX_SUGGESTIONS).orEmpty()
        val learned = history ?: return rank(followers, completions, prefix, MAX_SUGGESTIONS)

        // the user's own choices come before the lists', right after a space only along with
        // next-word prediction, like the list of followers
        val learnedFollowers = learned.followers(previous).takeIf { prefix.isNotEmpty() || isNextWordEnabled }.orEmpty()
        val learnedWords = learned.words(prefix).map { EnglishWords.matchCase(it, prefix) }
        return rank(learnedFollowers + followers, learnedWords + completions, prefix, MAX_SUGGESTIONS)
    }

    /**
     * Only words of the built-in list are learned, in its spelling, and the word before them
     * only if the list knows it too: nothing else the user types is ever kept.
     */
    override fun learn(previous: String, word: String) {
        val learned = history ?: return
        val dictionary = predictor ?: return
        val spelling = dictionary.spellingOf(word) ?: return
        val context = previous.takeIf { it.isNotEmpty() && dictionary.spellingOf(it) != null }.orEmpty()
        learned.record(context, spelling)
        historyChanged = true
        scheduleHistorySave()
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

    override fun clearHistory() {
        eraseHistory()
        updateHistory()
    }

    private fun updateHistory() {
        if (isEnabled && isLearningEnabled) loadHistory() else unloadHistory()
    }

    private fun loadHistory() {
        if (history != null) return
        // choices made while it is still being read are merged in afterwards
        val learned = WordHistory().also { history = it }
        historyLoadingJob = scope.launch {
            val stored = try {
                onHistoryStore { read() }
            } catch (exception: IOException) {
                discardUnreadableHistory(exception)
            } catch (exception: GeneralSecurityException) {
                discardUnreadableHistory(exception)
            } catch (exception: IllegalArgumentException) {
                discardUnreadableHistory(exception)
            }
            stored?.let(learned::mergeOlder)
        }
    }

    // The key may be gone, or the file damaged: start afresh rather than keep what is unreadable.
    private suspend fun discardUnreadableHistory(exception: Exception): List<WordHistory.Entry>? {
        // the exception's message never holds any of the history, see WordHistory.decode()
        Log.e(logTag, "The word history cannot be read, erasing it", exception)
        eraseStoredHistory()
        return null
    }

    // Saved first, so that switching prediction off and on again loses nothing.
    private fun unloadHistory() {
        val learned = history ?: return
        historyLoadingJob?.cancel()
        historyLoadingJob = null
        if (historyChanged) saveHistory(learned)
        history = null
    }

    private fun eraseHistory() {
        historyLoadingJob?.cancel()
        historyLoadingJob = null
        historySavingJob?.cancel()
        historySavingJob = null
        history = null
        historyChanged = false
        // not cancellable: the user asked for it to go, it goes even as the input method closes
        scope.launch(NonCancellable) { eraseStoredHistory() }
    }

    private suspend fun eraseStoredHistory() {
        try {
            onHistoryStore { erase() }
        } catch (exception: IOException) {
            Log.e(logTag, "Failed to erase the word history", exception)
        } catch (exception: GeneralSecurityException) {
            Log.e(logTag, "Failed to destroy the word history key", exception)
        }
    }

    // A burst of choices is saved once, a little after the last of them.
    private fun scheduleHistorySave() {
        historySavingJob?.cancel()
        historySavingJob = scope.launch {
            delay(HISTORY_SAVE_DELAY_MILLIS)
            history?.let(::saveHistory)
        }
    }

    private fun saveHistory(learned: WordHistory) {
        historyChanged = false
        // encoded here on the main thread, where the history lives, written off it
        val plain = learned.encode()
        // not cancellable either: what was chosen is kept even as the input method closes
        scope.launch(NonCancellable) {
            try {
                onHistoryStore { write(plain) }
            } catch (exception: IOException) {
                Log.e(logTag, "Failed to save the word history", exception)
            } catch (exception: GeneralSecurityException) {
                Log.e(logTag, "Failed to seal the word history", exception)
            }
        }
    }

    // One store operation at a time, in the order they were asked for: the lock is taken on
    // the main thread, where they are all launched, before going off it, so that a save asked
    // for before an erase can never land after it and bring the history back.
    private suspend fun <T> onHistoryStore(operation: WordHistoryStore.() -> T): T =
        historyLock.withLock { withContext(Dispatchers.IO) { historyStore.operation() } }

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

        // How long after the last choice the history is saved.
        private const val HISTORY_SAVE_DELAY_MILLIS = 2_000L

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
