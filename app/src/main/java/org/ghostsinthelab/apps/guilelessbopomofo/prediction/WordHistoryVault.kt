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

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Where the user's English word history is kept, shared by the input method, which learns it,
 * and the settings, which can clear it: both run in the same process and go through [lock],
 * so that one never writes the file while the other erases it.
 */
object WordHistoryVault {
    private const val LOG_TAG = "WordHistoryVault"

    // private to the app, never backed up (see the manifest's allowBackup too)
    private const val FILE_NAME = "english-word-history.bin"

    /** Every operation on the stored history holds this, taken on the main thread. */
    val lock = Mutex()

    private val clearListeners = CopyOnWriteArraySet<() -> Unit>()

    /** The store in [context]'s no-backup directory, sealed with a Keystore key. */
    fun store(context: Context): WordHistoryStore =
        WordHistoryStore(File(context.noBackupFilesDir, FILE_NAME), KeystoreHistoryCipher())

    /** [listener] is told, on the main thread, to drop what it holds of the history in memory. */
    fun addClearListener(listener: () -> Unit) {
        clearListeners += listener
    }

    fun removeClearListener(listener: () -> Unit) {
        clearListeners -= listener
    }

    /**
     * Erases the history, in memory and on disk, along with its key. Learning, if it is on,
     * goes on afresh. To be called from the main thread.
     *
     * @return whether it is all gone.
     */
    suspend fun clear(context: Context): Boolean {
        // first, so that the input method saves nothing more of what it held
        clearListeners.forEach { it() }
        return try {
            lock.withLock { withContext(Dispatchers.IO) { store(context).erase() } }
            true
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "Failed to erase the word history", exception)
            false
        } catch (exception: GeneralSecurityException) {
            Log.e(LOG_TAG, "Failed to destroy the word history key", exception)
            false
        }
    }
}
