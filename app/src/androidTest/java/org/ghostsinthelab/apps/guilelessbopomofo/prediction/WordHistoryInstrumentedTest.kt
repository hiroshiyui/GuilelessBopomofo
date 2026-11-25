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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.GeneralSecurityException
import java.security.KeyStore

/**
 * The word history against the real Android Keystore and the shipped word list: sealed on
 * disk, kept across restarts, and gone for good once learning is turned off. Uses its own key
 * alias and file, so the user's own history on a dev device is never touched.
 */
@RunWith(AndroidJUnit4::class)
class WordHistoryInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val file = File(context.cacheDir, "word-history-test.bin")
    private val cipher = KeystoreHistoryCipher(TEST_ALIAS)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @After
    fun tearDown() {
        scope.cancel()
        WordHistoryStore(file, cipher).erase()
    }

    private fun newPrediction() = EnglishPrediction(
        scope,
        openWordList = { context.assets.open(ScowlWordPredictor.ASSET_PATH) },
        openBigramList = { context.assets.open(BigramPredictor.ASSET_PATH) },
        openHistoryStore = { WordHistoryStore(file, cipher) },
    )

    private suspend fun <T> onMain(block: () -> T): T = withContext(Dispatchers.Main) { block() }

    private suspend fun waitUntil(condition: suspend () -> Boolean) {
        // the caller's line in the stack trace, rather than a bare timeout from a timer
        val waitedFrom = AssertionError("timed out")
        if (withTimeoutOrNull(TIMEOUT_MILLIS) { while (!condition()) delay(POLL_MILLIS) } == null) throw waitedFrom
    }

    private fun keyExists(): Boolean =
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.containsAlias(TEST_ALIAS)

    @Test
    fun theCipher_opensWhatItSealed_andNothingAltered() {
        val sealed = cipher.encrypt("go\tahead\t1\n".toByteArray())
        assertEquals("go\tahead\t1\n", String(cipher.decrypt(sealed)))
        assertFalse(String(sealed, Charsets.ISO_8859_1).contains("ahead"))

        val altered = sealed.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 1).toByte() }
        assertThrowsSecurity { cipher.decrypt(altered) }
    }

    @Test
    fun theCipher_cannotOpenAnything_onceTheKeyIsDestroyed() {
        val sealed = cipher.encrypt("go\tahead\t1\n".toByteArray())
        cipher.destroyKey()
        assertFalse(keyExists())
        assertThrowsSecurity { cipher.decrypt(sealed) }
    }

    @Test
    fun aChosenWord_comesFirst_isKeptAcrossRestarts_andIsErasedWhenLearningGoesOff() = runBlocking {
        val first = newPrediction()
        onMain {
            first.isEnabled = true
            first.isLearningEnabled = true
        }
        waitUntil { onMain { first.suggest("go", "a").isNotEmpty() } }
        assertFalse(onMain { first.suggest("go", "a") }.contains("ahead"))

        onMain { first.learn("go", "ahead") }
        assertEquals("ahead", onMain { first.suggest("go", "a") }.first())

        // switching prediction off saves it, sealed
        onMain { first.isEnabled = false }
        waitUntil { file.exists() }
        assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("ahead"))

        val second = newPrediction()
        onMain {
            second.isEnabled = true
            second.isLearningEnabled = true
        }
        waitUntil { onMain { second.suggest("go", "a").firstOrNull() == "ahead" } }

        onMain { second.isLearningEnabled = false }
        waitUntil { !file.exists() && !keyExists() }
        assertFalse(onMain { second.suggest("go", "a") }.contains("ahead"))
    }

    @Test
    fun clearing_erasesEverything_whileLearningGoesOn() = runBlocking {
        val prediction = newPrediction()
        onMain {
            prediction.isEnabled = true
            prediction.isLearningEnabled = true
        }
        waitUntil { onMain { prediction.suggest("go", "a").isNotEmpty() } }
        onMain {
            prediction.learn("go", "ahead")
            prediction.isEnabled = false
            prediction.isEnabled = true
        }
        // switched back on, the word list and the history are read again
        waitUntil { file.exists() && keyExists() }
        waitUntil { onMain { prediction.suggest("go", "a").firstOrNull() == "ahead" } }
        // the word list too, which nothing is learned without: no learned word starts with q
        waitUntil { onMain { prediction.suggest("", "q").isNotEmpty() } }

        onMain { prediction.clearHistory() }
        waitUntil { !file.exists() && !keyExists() }
        assertFalse(onMain { prediction.suggest("go", "a") }.contains("ahead"))

        // still learning, afresh, under a new key
        onMain {
            prediction.learn("go", "home")
            prediction.isEnabled = false
        }
        waitUntil { file.exists() }
        assertEquals(listOf(WordHistory.Entry("go", "home", 1)), WordHistoryStore(file, cipher).read())
    }

    @Test
    fun wordsOutsideTheWordList_areNeverKept() = runBlocking {
        val prediction = newPrediction()
        onMain {
            prediction.isEnabled = true
            prediction.isLearningEnabled = true
        }
        waitUntil { onMain { prediction.suggest("", "a").isNotEmpty() } }

        onMain {
            prediction.learn("Hsinchu", "ahead")
            prediction.learn("go", "Zhuyinfuhao")
            prediction.isEnabled = false
        }
        waitUntil { file.exists() }
        val kept = WordHistoryStore(file, cipher).read().orEmpty()
        assertEquals(listOf(WordHistory.Entry("", "ahead", 1)), kept)
        assertTrue(kept.none { it.previous.contains("hsinchu") || it.word.contains("Zhuyin") })
    }

    private fun assertThrowsSecurity(block: () -> Unit) {
        try {
            block()
            throw AssertionError("expected a GeneralSecurityException")
        } catch (_: GeneralSecurityException) {
        }
    }

    private companion object {
        const val TEST_ALIAS = "english_word_history_instrumented_test"
        const val TIMEOUT_MILLIS = 10_000L
        const val POLL_MILLIS = 50L
    }
}
