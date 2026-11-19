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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * A libchewing data directory of a test's own, under the app's cache, so tests about
 * dictionaries, user data and its migration never touch what the app itself has learned.
 * [close] disconnects libchewing and deletes the directory.
 */
class IsolatedChewingData : AutoCloseable {
    val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    val dir: File = File(appContext.cacheDir, "chewing-test-${System.nanoTime()}").apply { mkdirs() }
    val path: String = dir.absolutePath

    /** Installs the dictionaries the way the app does, without connecting. */
    fun install() {
        ChewingUtil.setupChewingData(appContext, path)
    }

    /** Installs the dictionaries and connects libchewing to this directory. */
    fun installAndConnect() {
        install()
        ChewingUtil.connect(path)
    }

    /** Puts libchewing 0.13's user data, from the test assets, in this directory. */
    fun putLegacyUserData(vararg names: String = arrayOf(LEGACY_PHRASES, LEGACY_DELETED)) {
        val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
        for (name in names) {
            testAssets.open("legacy-userdata/$name").use { input ->
                File(dir, name).outputStream().use { input.copyTo(it) }
            }
        }
    }

    fun file(name: String): File = File(dir, name)

    override fun close() {
        ChewingBridge.chewing.delete()
        dir.deleteRecursively()
    }

    companion object {
        const val LEGACY_PHRASES = "userhash.dat"
        const val LEGACY_DELETED = "chewing-deleted.dat"
        const val MIGRATABLE_PHRASES = "chewing.dat"
        const val USER_DICT = "v4/user_dict.csv"
        const val HISTORY_DICT = "v4/history_dict.bin"

        // The one phrase in the legacy fixture.
        const val LEGACY_PHRASE = "策試"
    }
}
