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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.GeneralSecurityException

class WordHistoryStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    /** Flips every bit and marks the result, so a test can tell sealed bytes from plain ones. */
    private class FakeCipher : HistoryCipher {
        var keyDestroyed = false
        override fun encrypt(plain: ByteArray) = byteArrayOf(MARK) + plain.map { it.toInt().inv().toByte() }
        override fun decrypt(sealed: ByteArray): ByteArray {
            if (keyDestroyed || sealed.firstOrNull() != MARK) throw GeneralSecurityException("cannot open")
            return sealed.drop(1).map { it.toInt().inv().toByte() }.toByteArray()
        }
        override fun destroyKey() {
            keyDestroyed = true
        }

        companion object {
            const val MARK: Byte = 0x5A
        }
    }

    private val cipher = FakeCipher()
    private val file: File by lazy { File(folder.root, "history.bin") }
    private val store by lazy { WordHistoryStore(file, cipher) }

    private fun historyOf(vararg pairs: Pair<String, String>) =
        WordHistory().apply { pairs.forEach { (previous, word) -> record(previous, word) } }

    @Test
    fun nothingKept_readsAsNull() {
        assertNull(store.read())
    }

    @Test
    fun whatIsWritten_isReadBack() {
        store.write(historyOf("go" to "ahead", "" to "I").encode())
        assertEquals(
            listOf(WordHistory.Entry("go", "ahead", 1), WordHistory.Entry("", "I", 1)),
            store.read()
        )
    }

    @Test
    fun theFileIsSealed_neverPlainText() {
        store.write(historyOf("go" to "ahead").encode())
        val onDisk = file.readBytes()
        assertEquals(FakeCipher.MARK, onDisk.first())
        assertFalse(String(onDisk, Charsets.ISO_8859_1).contains("ahead"))
    }

    @Test
    fun thePlainBytes_areWipedOnceWritten() {
        val plain = historyOf("go" to "ahead").encode()
        store.write(plain)
        assertTrue(plain.all { it == 0.toByte() })
    }

    @Test
    fun noPendingFileIsLeftBehind() {
        store.write(historyOf("go" to "ahead").encode())
        assertEquals(listOf("history.bin"), folder.root.list()!!.toList())
    }

    @Test
    fun erase_deletesTheFile_andDestroysTheKey() {
        store.write(historyOf("go" to "ahead").encode())
        store.erase()
        assertFalse(file.exists())
        assertTrue(cipher.keyDestroyed)
        assertNull(store.read())
    }

    @Test(expected = GeneralSecurityException::class)
    fun aCopyLeftAfterTheKeyIsGone_cannotBeRead() {
        store.write(historyOf("go" to "ahead").encode())
        val copy = file.readBytes()
        store.erase()
        file.writeBytes(copy)
        store.read()
    }
}
