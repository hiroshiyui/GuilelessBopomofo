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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeDispatcher

/**
 * Helpers around the libchewing context in [ChewingBridge]: installing and resetting its
 * data files, reading its candidates and user phrases, and the key actions that the
 * on-screen and physical keys share. Main thread only, like the context itself.
 */
object ChewingUtil {
    private const val logTag = "ChewingUtil"

    // The dictionaries shipped as assets, which libchewing reads from the app's data directory.
    private val DATA_FILES: List<String> = listOf(
        "static_dict.bin", "rare_dict.bin", "static_words.bin", "static_lm.bin", "swkb.dat", "symbols.dat"
    )

    // The dictionaries libchewing 0.13 and earlier read, which nothing reads any more.
    private val OBSOLETE_DATA_FILES: List<String> = listOf("tsi.dat", "word.dat")

    // Where libchewing 0.14 keeps what it learns from the user, under the data directory.
    private const val USER_DATA_DIR = "v4"

    // What libchewing writes as it starts properly, and only then.
    private const val USER_DICT_FILE = "$USER_DATA_DIR/user_dict.csv"

    // The first start may fall back once, on old user phrases it cannot bring over.
    private const val FIRST_START_ATTEMPTS = 2

    // The user phrases this app kept before libchewing 0.14, and the file name 0.14 looks
    // for to bring them over into USER_DATA_DIR. It also needs LEGACY_DELETED_PHRASES_FILE.
    private const val LEGACY_USER_PHRASES_FILE = "userhash.dat"
    private const val MIGRATABLE_USER_PHRASES_FILE = "chewing.dat"
    private const val LEGACY_DELETED_PHRASES_FILE = "chewing-deleted.dat"

    // What a reset throws away besides USER_DATA_DIR. The older files go too, or libchewing
    // would bring them back over on the next start.
    private val LEGACY_USER_DATA_FILES: List<String> = listOf(
        LEGACY_USER_PHRASES_FILE, MIGRATABLE_USER_PHRASES_FILE, LEGACY_DELETED_PHRASES_FILE
    )

    private const val DATA_VERSION_FILE = "data_appversion.txt"

    // 「常用符號」is the third entry of the symbol picker. The selection keys are not
    // guaranteed to be 0-9, so reaching for it by index is the safer way.
    private const val FREQUENTLY_USED_SYMBOLS_INDEX = 2

    /** The dictionary files shipped as assets, for the settings screens to check on. */
    fun listOfDataFiles(): List<String> = DATA_FILES

    /**
     * Connects libchewing if nothing has yet, installing the dictionaries first if need be.
     * The settings screens call this, as they may run without the input method.
     */
    fun ensureChewingConnected(context: Context) {
        if (ChewingBridge.chewing.context != 0L) return
        val dataPath = context.applicationInfo.dataDir
        setupChewingData(context, dataPath)
        connect(dataPath)
    }

    /**
     * Connects libchewing to the dictionaries and user data under [dataPath].
     *
     * libchewing 0.14 never fails to open a context: whatever stops it starting properly, a
     * missing dictionary or old user phrases it cannot bring over, it falls back to one that
     * has no dictionaries and converts nothing, and says nothing. A proper start writes its
     * user dictionary, so a first start that leaves none fell back. Once more then: it has
     * made [USER_DATA_DIR] by now, so it skips the old phrases, which stay in
     * [LEGACY_USER_PHRASES_FILE]. A later start, with the user dictionary already there,
     * cannot be checked this way.
     *
     * @throws Chewing.ChewingInitException if libchewing cannot start properly.
     */
    fun connect(dataPath: String) {
        val userDict = File(dataPath, USER_DICT_FILE)
        if (userDict.exists()) {
            ChewingBridge.chewing.connect(dataPath)
            return
        }
        repeat(FIRST_START_ATTEMPTS) {
            ChewingBridge.chewing.connect(dataPath)
            if (userDict.exists()) return
            Log.e(logTag, "libchewing fell back to a context without dictionaries.")
        }
        ChewingBridge.chewing.delete()
        throw Chewing.ChewingInitException
    }

    /**
     * Flushes the chewing context to persist user phrases to disk.
     * This deletes the current context and reconnects, forcing libchewing
     * to write out the user dictionary. Call this after adding or removing user phrases.
     */
    fun flushContext(context: Context) {
        reconnect(context.applicationInfo.dataDir)
    }

    /**
     * Throws away everything libchewing has learned from this user.
     */
    fun resetUserPhraseData(context: Context) {
        resetUserPhraseData(context.applicationInfo.dataDir)
    }

    /** [resetUserPhraseData] on the libchewing data under [dataPath], which tests choose. */
    internal fun resetUserPhraseData(dataPath: String) {
        reconnect(dataPath) { chewingDataDir ->
            File(chewingDataDir, USER_DATA_DIR).deleteRecursively()
            LEGACY_USER_DATA_FILES.forEach { File(chewingDataDir, it).delete() }
        }
    }

    /**
     * Hands the chewing context back to libchewing and asks for a fresh one, which is the
     * only way to make it write out what it has learned. [whileDisconnected] runs in
     * between, when no context is holding the data files open.
     */
    private inline fun reconnect(dataPath: String, whileDisconnected: (File) -> Unit = {}) {
        val wasConnected = ChewingBridge.chewing.context != 0L
        // The input method may be in the middle of typing: keep its modes across the swap.
        val chiEngMode = if (wasConnected) ChewingBridge.chewing.getChiEngMode() else ChiEngMode.CHINESE.mode
        val shapeMode = if (wasConnected) ChewingBridge.chewing.getShapeMode() else ShapeMode.HALF.mode

        ChewingBridge.chewing.delete()
        whileDisconnected(File(dataPath))
        connect(dataPath)

        ChewingBridge.chewing.setChiEngMode(chiEngMode)
        ChewingBridge.chewing.setShapeMode(shapeMode)
        // The rest of its settings the input method, if it is running, puts back itself.
        ChewingBridge.notifyReconnected()
    }

    /**
     * Makes sure the dictionaries under [dataPath] are present and belong to this build of
     * the app and libchewing, reinstalling them from the assets otherwise.
     */
    fun setupChewingData(context: Context, dataPath: String) {
        val chewingDataDir = File(dataPath)

        OBSOLETE_DATA_FILES.forEach { File(chewingDataDir, it).delete() }
        prepareLegacyUserPhrasesForMigration(chewingDataDir)

        if (!chewingDataFilesInstalled(dataPath)) {
            Log.d(logTag, "Install Chewing data files.")
            installChewingData(context, dataPath)
        }

        // The libchewing revision changes the dictionaries without a new version name.
        val appVersion = "${BuildConfig.VERSION_NAME}+${BuildConfig.CHEWING_REVISION}".toByteArray()
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

    /**
     * Puts a copy of the user phrases kept before libchewing 0.14 where 0.14 looks for them,
     * so it brings them over into [USER_DATA_DIR] on its first start. Only once: after that
     * [USER_DATA_DIR] exists and libchewing leaves the old files alone. The original stays,
     * for a build with an older libchewing to go on reading.
     */
    private fun prepareLegacyUserPhrasesForMigration(chewingDataDir: File) {
        if (File(chewingDataDir, USER_DATA_DIR).exists()) return
        val legacyPhrases = File(chewingDataDir, LEGACY_USER_PHRASES_FILE)
        val migratablePhrases = File(chewingDataDir, MIGRATABLE_USER_PHRASES_FILE)
        if (!legacyPhrases.exists() || migratablePhrases.exists()) return
        // Without this file libchewing cannot bring the phrases over, and falls back to a
        // context that has no dictionaries for the first start.
        if (!File(chewingDataDir, LEGACY_DELETED_PHRASES_FILE).exists()) {
            Log.w(logTag, "No $LEGACY_DELETED_PHRASES_FILE, so the old user phrases are not migrated.")
            return
        }
        try {
            legacyPhrases.copyTo(migratablePhrases)
        } catch (exception: IOException) {
            Log.e(logTag, "Failed to hand $LEGACY_USER_PHRASES_FILE over for migration.", exception)
            migratablePhrases.delete()
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

    /**
     * The phrases the user added, for the user phrase manager. Single characters are left
     * out: libchewing learns those on its own as text is committed.
     */
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

    /** Whether libchewing has candidates on offer right now. */
    fun candidateWindowOpened(): Boolean {
        return ChewingBridge.chewing.candTotalChoice() > 0
    }

    /** The opposite of [candidateWindowOpened]. */
    fun candidateWindowClosed(): Boolean {
        return ChewingBridge.chewing.candTotalChoice() <= 0
    }

    /** Whether anything, converted or still Bopomofo, is waiting to be committed. */
    fun anyBufferIsNotEmpty(): Boolean {
        return ChewingBridge.chewing.bufferStringStatic().isNotEmpty() || ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty()
    }

    /** Opens the symbol picker at its top level, as the `‵` key does. */
    fun openSymbolCandidates() {
        openSymbolPicker()
    }

    /** Opens the symbol picker straight at its「常用符號」(frequently used symbols) page. */
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

    /**
     * One page of the current candidates, each paired with the selection key that picks it,
     * for the paged list shown alongside a physical keyboard.
     */
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

    /**
     * Backspace: edits libchewing's buffers while they hold anything, and goes to the text
     * field otherwise.
     */
    fun handleBackspaceAction(ime: ImeDispatcher) {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleBackspace()
            ime.dispatch(ImeAction.UpdateBufferViews)
        } else {
            ime.dispatch(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL))
        }
    }

    /**
     * @param ime Who carries out what the key asks for.
     * @param intent What the user meant by the key press. Committing what is still in the
     * buffer always comes first, whichever intent is given.
     */
    fun handleEnterAction(ime: ImeDispatcher, intent: EnterKeyIntent) {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.commitPreeditBuf(ChewingBridge.chewing.context)
            ime.dispatch(ImeAction.UpdateBufferViews)
            return
        }

        when (intent) {
            EnterKeyIntent.LINE_BREAK ->
                ime.dispatch(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER))

            EnterKeyIntent.EDITOR_ACTION ->
                ime.dispatch(ImeAction.EnterKeyDownWhenBufferIsEmpty(lineBreakInMultiLineFields = false))

            EnterKeyIntent.DEFAULT ->
                ime.dispatch(ImeAction.EnterKeyDownWhenBufferIsEmpty(lineBreakInMultiLineFields = true))
        }
    }

    /**
     * Space: converts or opens the candidates while the buffers hold anything, as the user's
     * preferences say, and types a space into the text field otherwise.
     */
    fun handleSpaceAction(ime: ImeDispatcher) {
        if (anyBufferIsNotEmpty()) {
            ChewingBridge.chewing.handleSpace()
            ime.dispatch(ImeAction.UpdateBufferViews)
            if (ChewingBridge.chewing.getSpaceAsSelection() == 1 && candidateWindowOpened()) {
                openCandidates(ime)
            }
        } else {
            ime.dispatch(ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE))
        }
    }

    /**
     * Moves the chewing cursor one character to either side, and lets whoever is showing the
     * buffer or the candidates know about it.
     */
    fun moveCursorHorizontally(ime: ImeDispatcher, direction: DirectionKey) {
        when (direction) {
            DirectionKey.LEFT -> ChewingBridge.chewing.handleLeft()
            DirectionKey.RIGHT -> ChewingBridge.chewing.handleRight()
        }
        ime.dispatch(ImeAction.DirectionKeyDown(direction))
    }

    /** Opens the candidates for the character under libchewing's cursor and shows them. */
    fun openCandidates(ime: ImeDispatcher) {
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.candOpen()
        ime.dispatch(ImeAction.SwitchToLayout(Layout.CANDIDATES))
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

    /** The QWERTY key at the position [key] has on a Dvorak keyboard, or [key] itself. */
    fun dvorakToQwertyKeyMapping(key: Char): Char {
        return dvorakToQwertyKeyMappingMap[key] ?: key
    }

    private val qwertyToDvorakKeyMappingMap: Map<Char, Char> =
        qwertyKeysList.zip(dvorakKeysList).toMap()

    /** The Dvorak key at the position [key] has on a QWERTY keyboard, or [key] itself. */
    fun qwertyToDvorakKeyMapping(key: Char): Char {
        return qwertyToDvorakKeyMappingMap[key] ?: key
    }
}
