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
import java.io.File
import java.io.FileOutputStream

/** Puts the dictionaries in place and connects libchewing, the way the tests here need it. */
object ChewingTestSetup {
    private val dataFiles = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    fun connect(context: Context): String {
        val dataPath = context.dataDir.absolutePath
        for (file in dataFiles) {
            context.assets.open(file).use { input ->
                FileOutputStream(File(dataPath, file)).use { output -> input.copyTo(output) }
            }
        }
        ChewingBridge.chewing.connect(dataPath)
        Log.d("ChewingTestSetup", "connected")
        return dataPath
    }
}
