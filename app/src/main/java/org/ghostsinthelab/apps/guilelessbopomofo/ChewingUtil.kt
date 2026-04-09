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
import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream

object ChewingUtil {
    private const val logTag = "ChewingUtil"

    fun listOfDataFiles(): List<String> {
        return listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")
    }

    fun ensureChewingConnected(context: Context) {
        if (ChewingBridge.chewing.context != 0L) return
        val dataPath = context.applicationInfo.dataDir
        setupChewingData(context, dataPath)
        ChewingBridge.chewing.connect(dataPath)
    }

    /**
     * Flushes the chewing context to persist user phrases to disk.
     * This deletes the current context and reconnects, forcing libchewing
     * to write userhash.dat. Call this after adding or removing user phrases.
     */
    fun flushContext(context: Context) {
        val dataPath = context.applicationInfo.dataDir
        ChewingBridge.chewing.delete()
        ChewingBridge.chewing.context = 0
        ChewingBridge.chewing.connect(dataPath)
    }

    fun setupChewingData(context: Context, dataPath: String) {
        val chewingDataDir = File(dataPath)

        if (!checkChewingData(dataPath)) {
            Log.d(logTag, "Install Chewing data files.")
            installChewingData(context, dataPath)
        }

        val appVersion = BuildConfig.VERSION_NAME.toByteArray()
        val chewingDataAppVersionTxt = File(chewingDataDir, "data_appversion.txt")

        if (!chewingDataAppVersionTxt.exists()) {
            chewingDataAppVersionTxt.appendBytes(appVersion)
        }

        if (!chewingDataAppVersionTxt.readBytes().contentEquals(appVersion)) {
            Log.d(logTag, "Here comes a new version.")
            installChewingData(context, dataPath)
            FileOutputStream(chewingDataAppVersionTxt).use { it.write(appVersion) }
        }
    }

    private fun installChewingData(context: Context, dataPath: String) {
        val chewingDataDir = File(dataPath)
        for (file in listOfDataFiles()) {
            val destinationFile = File(chewingDataDir, file)
            Log.d(logTag, "Copying ${file}...")
            try {
                context.assets.open(file).use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "Failed to copy $file", e)
            }
        }
    }

    private fun checkChewingData(dataPath: String): Boolean {
        Log.d(logTag, "Checking Chewing data files...")
        val chewingDataDir = File(dataPath)
        for (file in listOfDataFiles()) {
            val destinationFile = File(chewingDataDir, file)
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }

    fun enumerateUserPhrases(): List<UserPhrase> {
        val results = ChewingBridge.chewing.userphraseGetAll() ?: return emptyList()
        return results.mapNotNull { pair ->
            if (pair.size >= 2) UserPhrase(pair[0], pair[1]) else null
        }.filter {
            // Skip single-character entries: these are auto-learned by libchewing
            // during composition commit, not intentionally added by the user.
            // Showing them in the manager would be confusing, and deleting them
            // triggers a libchewing bug where the character gets added to the
            // exclusion dictionary (chewing-deleted.dat), hiding it from system
            // dictionary candidates entirely.
            it.phrase.length > 1
        }
    }

    fun candidateWindowOpened(): Boolean {
        return ChewingBridge.chewing.candTotalChoice() > 0
    }

    fun candidateWindowClosed(): Boolean {
        return ChewingBridge.chewing.candTotalChoice() <= 0
    }

    fun anyBufferIsNotEmpty(): Boolean {
        return ChewingBridge.chewing.bufferStringStatic().isNotEmpty() || ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty()
    }

    fun openSymbolCandidates() {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.handleDefault('`')
        ChewingBridge.chewing.candOpen()
    }

    fun openFrequentlyUsedCandidates() {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.handleDefault('`')
        // 「常用符號」
        // 選字鍵不能保證一定是 0-9，用 candChooseByIndex() 相對妥當
        ChewingBridge.chewing.candChooseByIndex(2)
        ChewingBridge.chewing.candOpen()
    }

    fun getCandidatesByPage(page: Int = 0): List<Candidate> {
        val fromOffset = page * ChewingBridge.chewing.candChoicePerPage()
        val toOffset = fromOffset + ChewingBridge.chewing.candChoicePerPage() - 1
        val candidatesInThisPage: MutableList<Candidate> = mutableListOf()
        val selKeys = ChewingBridge.chewing.getSelKey()

        for (i in fromOffset..toOffset) {
            if (ChewingBridge.chewing.candStringByIndexStatic(i).isNotBlank()) {
                candidatesInThisPage.add(getCandidate(index = i))
            }
        }

        // binding selection key
        candidatesInThisPage.mapIndexed { index, candidate ->
            if (index < selKeys.size) {
                candidate.selectionKey = selKeys[index].toChar()
            }
        }

        return candidatesInThisPage.toList()
    }

    fun handleBackspaceAction() {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleBackspace()
            EventBus.getDefault().post(Events.UpdateBufferViews())
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL))
        }
    }

    fun handleEnterAction() {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.commitPreeditBuf(ChewingBridge.chewing.context)
            EventBus.getDefault().post(Events.UpdateBufferViews())
        } else {
            EventBus.getDefault().post(Events.EnterKeyDownWhenBufferIsEmpty())
        }
    }

    fun handleSpaceAction() {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleSpace()
            EventBus.getDefault().post(Events.UpdateBufferViews())
            if (ChewingBridge.chewing.getSpaceAsSelection() == 1 && ChewingBridge.chewing.candTotalChoice() > 0) {
                openCandidates()
            }
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE))
        }
    }

    fun openCandidates() {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.candOpen()
        EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
    }

    private fun getCandidate(index: Int): Candidate {
        return Candidate(index, ChewingBridge.chewing.candStringByIndexStatic(index))
    }

    // simulates [Shift] + [,]
    fun handleShiftComma() {
        if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
            ChewingBridge.chewing.setEasySymbolInput(1)
            ChewingBridge.chewing.handleDefault('<')
            ChewingBridge.chewing.setEasySymbolInput(0)
        } else {
            ChewingBridge.chewing.handleDefault(',')
        }
    }

    private val dvorakKeysList: List<Char> = listOf(
        '\'', '\"', ',', '<', '.', '>', 'p', 'P', 'y', 'Y', 'f', 'F', 'g', 'G',
        'c', 'C', 'r', 'R', 'l', 'L', '/', '?', '=', '+', '\\', '|',
        'a', 'A', 'o', 'O', 'e', 'E', 'u', 'U', 'i', 'I', 'd', 'D', 'h', 'H',
        't', 'T', 'n', 'N', 's', 'S', '-', '_',
        ';', ':', 'q', 'Q', 'j', 'J', 'k', 'K', 'x', 'X', 'b', 'B', 'm', 'M',
        'w', 'W', 'v', 'V', 'z', 'Z'
    )

    private val qwertyKeysList: List<Char> = listOf(
        'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U',
        'i', 'I', 'o', 'O', 'p', 'P', '[', '{', ']', '}', '\\', '|',
        'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J',
        'k', 'K', 'l', 'L', ';', ':', '\'', '\"',
        'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M',
        ',', '<', '.', '>', '/', '?'
    )

    private val dvorakToQwertyKeyMappingMap: Map<Char, Char> =
        dvorakKeysList.zip(qwertyKeysList).toMap()

    fun dvorakToQwertyKeyMapping(key: Char): Char {
        return dvorakToQwertyKeyMappingMap[key] ?: key
    }

    private val qwertyToDvorakKeyMappingMap: Map<Char, Char> =
        qwertyKeysList.zip(dvorakKeysList).toMap()

    fun qwertyToDvorakKeyMapping(key: Char): Char {
        return qwertyToDvorakKeyMappingMap[key] ?: key
    }
}
