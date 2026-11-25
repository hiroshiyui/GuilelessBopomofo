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

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException

/** Seals the word history before it touches the disk, and opens it again. */
interface HistoryCipher {
    /** @throws GeneralSecurityException if it cannot be sealed. */
    fun encrypt(plain: ByteArray): ByteArray

    /** @throws GeneralSecurityException if [sealed] was not sealed by this cipher, or was altered. */
    fun decrypt(sealed: ByteArray): ByteArray

    /** Throws the key away for good, so that no copy of anything it sealed can be read again. */
    fun destroyKey()
}

/**
 * Keeps the word history, sealed by [cipher], in a single file. Meant to be run off the main
 * thread, one call at a time.
 *
 * @param file where it is kept: the app's no-backup directory, out of reach of other apps, of
 * Android's backups, and of anyone before the device is first unlocked.
 */
class WordHistoryStore(private val file: File, private val cipher: HistoryCipher) {
    private val pending = File(file.path + PENDING_SUFFIX)

    /**
     * @return the history, or null if none has been kept.
     * @throws IOException, GeneralSecurityException, IllegalArgumentException when it cannot
     * be read, it should then be [erase]d.
     */
    fun read(): List<WordHistory.Entry>? {
        if (!file.exists()) return null
        val plain = cipher.decrypt(file.readBytes())
        return try {
            WordHistory.decode(plain)
        } finally {
            plain.fill(0)
        }
    }

    /**
     * Replaces what is kept with [plain], as [WordHistory.encode] gave it, and wipes [plain].
     * The file is swapped in whole, so a crash halfway leaves the previous one.
     *
     * @throws IOException, GeneralSecurityException
     */
    fun write(plain: ByteArray) {
        val sealed = try {
            cipher.encrypt(plain)
        } finally {
            plain.fill(0)
        }
        FileOutputStream(pending).use { output ->
            output.write(sealed)
            output.fd.sync()
        }
        if (!pending.renameTo(file)) {
            pending.delete()
            throw IOException("Failed to replace the word history")
        }
    }

    /**
     * Deletes the history and its key. Flash storage may keep the deleted bytes around for a
     * while, but without the key they can no longer be read.
     *
     * @throws IOException if the file could not be deleted, its key is destroyed anyway.
     * @throws GeneralSecurityException if the key could not be destroyed, the files are gone anyway.
     */
    fun erase() {
        file.delete()
        pending.delete()
        cipher.destroyKey()
        if (file.exists() || pending.exists()) throw IOException("Failed to delete the word history")
    }

    private companion object {
        const val PENDING_SUFFIX = ".new"
    }
}
