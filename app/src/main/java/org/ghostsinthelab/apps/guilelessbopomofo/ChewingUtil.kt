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
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream

object ChewingUtil {
    private const val logTag = "ChewingUtil"

    // The dictionaries shipped as assets, which libchewing reads from the app's data directory.
    private val DATA_FILES: List<String> = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    // What libchewing writes as the user types, and what a reset throws away.
    private val USER_DATA_FILES: List<String> = listOf("chewing-deleted.dat", "userhash.dat")

    private const val DATA_VERSION_FILE = "data_appversion.txt"

    // 「常用符號」is the third entry of the symbol picker. The selection keys are not
    // guaranteed to be 0-9, so reaching for it by index is the safer way.
    private const val FREQUENTLY_USED_SYMBOLS_INDEX = 2

    fun listOfDataFiles(): List<String> = DATA_FILES

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
        reconnect(context)
    }

    /**
     * Throws away everything libchewing has learned from this user.
     */
    fun resetUserPhraseData(context: Context) {
        reconnect(context) { chewingDataDir ->
            USER_DATA_FILES.forEach { File(chewingDataDir, it).delete() }
        }
    }

    /**
     * Hands the chewing context back to libchewing and asks for a fresh one, which is the
     * only way to make it write out what it has learned. [whileDisconnected] runs in
     * between, when no context is holding the data files open.
     */
    private inline fun reconnect(context: Context, whileDisconnected: (File) -> Unit = {}) {
        val dataPath = context.applicationInfo.dataDir
        val wasConnected = ChewingBridge.chewing.context != 0L
        // The input method may be in the middle of typing: keep its modes across the swap.
        val chiEngMode = if (wasConnected) ChewingBridge.chewing.getChiEngMode() else ChiEngMode.CHINESE.mode
        val shapeMode = if (wasConnected) ChewingBridge.chewing.getShapeMode() else ShapeMode.HALF.mode

        ChewingBridge.chewing.delete()
        whileDisconnected(File(dataPath))
        ChewingBridge.chewing.connect(dataPath)

        ChewingBridge.chewing.setChiEngMode(chiEngMode)
        ChewingBridge.chewing.setShapeMode(shapeMode)
        // The rest of its settings the input method, if it is running, puts back itself.
        EventBus.getDefault().post(Events.ChewingReconnected())
    }

    fun setupChewingData(context: Context, dataPath: String) {
        val chewingDataDir = File(dataPath)

        if (!chewingDataFilesInstalled(dataPath)) {
            Log.d(logTag, "Install Chewing data files.")
            installChewingData(context, dataPath)
        }

        val appVersion = BuildConfig.VERSION_NAME.toByteArray()
        val chewingDataAppVersionTxt = File(chewingDataDir, DATA_VERSION_FILE)

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
        for (file in DATA_FILES) {
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

    /**
     * Whether every dictionary libchewing needs is where it expects to find it.
     */
    fun chewingDataFilesInstalled(dataPath: String): Boolean {
        Log.d(logTag, "Checking Chewing data files...")
        val chewingDataDir = File(dataPath)
        return DATA_FILES.all { File(chewingDataDir, it).exists() }
    }

    fun enumerateUserPhrases(): List<UserPhrase> {
        val results = ChewingBridge.chewing.userphraseGetAll() ?: return emptyList()
        return results.mapNotNull { pair ->
            if (pair.size >= 2) UserPhrase(pair[0], pair[1]) else null
        }.filter {
            // Hide single-character entries: libchewing auto-learns these during
            // composition commit, so they are not phrases the user intentionally
            // added. Showing them clutters the manager with noise.
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
        openSymbolPicker()
    }

    fun openFrequentlyUsedCandidates() {
        openSymbolPicker { ChewingBridge.chewing.candChooseByIndex(FREQUENTLY_USED_SYMBOLS_INDEX) }
    }

    /**
     * Brings up the symbol picker, which the `‵` key opens. [descend] may walk into one of
     * its sub-menus before the candidates are shown.
     */
    private inline fun openSymbolPicker(descend: () -> Unit = {}) {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.handleDefault('`')
        descend()
        ChewingBridge.chewing.candOpen()
    }

    fun getCandidatesByPage(page: Int = 0): List<Candidate> {
        val candidatesPerPage = ChewingBridge.chewing.candChoicePerPage()
        val selectionKeys = ChewingBridge.chewing.getSelKey()
        val fromOffset = page * candidatesPerPage

        return (fromOffset until fromOffset + candidatesPerPage)
            .map { index -> index to ChewingBridge.chewing.candStringByIndexStatic(index) }
            .filter { (_, candidateString) -> candidateString.isNotBlank() }
            .mapIndexed { positionInPage, (index, candidateString) ->
                Candidate(
                    index = index,
                    candidateString = candidateString,
                    // binding selection key
                    selectionKey = selectionKeys.getOrNull(positionInPage)?.toChar() ?: '\u0000',
                )
            }
    }

    fun handleBackspaceAction() {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleBackspace()
            EventBus.getDefault().post(Events.UpdateBufferViews())
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL))
        }
    }

    /**
     * @param intent What the user meant by the key press. Committing what is still in the
     * buffer always comes first, whichever intent is given.
     */
    fun handleEnterAction(intent: EnterKeyIntent = EnterKeyIntent.EDITOR_ACTION) {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.commitPreeditBuf(ChewingBridge.chewing.context)
            EventBus.getDefault().post(Events.UpdateBufferViews())
            return
        }

        when (intent) {
            EnterKeyIntent.LINE_BREAK ->
                EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER))

            EnterKeyIntent.EDITOR_ACTION ->
                EventBus.getDefault().post(Events.EnterKeyDownWhenBufferIsEmpty())
        }
    }

    fun handleSpaceAction() {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleSpace()
            EventBus.getDefault().post(Events.UpdateBufferViews())
            if (ChewingBridge.chewing.getSpaceAsSelection() == 1 && candidateWindowOpened()) {
                openCandidates()
            }
        } else {
            EventBus.getDefault().post(Events.SendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE))
        }
    }

    /**
     * Moves the chewing cursor one character to either side, and lets whoever is showing the
     * buffer or the candidates know about it.
     */
    fun moveCursorHorizontally(direction: DirectionKey) {
        when (direction) {
            DirectionKey.LEFT -> ChewingBridge.chewing.handleLeft()
            DirectionKey.RIGHT -> ChewingBridge.chewing.handleRight()
        }
        EventBus.getDefault().post(Events.DirectionKeyDown(direction))
    }

    fun openCandidates() {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.candOpen()
        EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
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
