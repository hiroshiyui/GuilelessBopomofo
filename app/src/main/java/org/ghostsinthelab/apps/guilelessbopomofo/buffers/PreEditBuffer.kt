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

package org.ghostsinthelab.apps.guilelessbopomofo.buffers

import android.util.Log
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.ConversionEngines
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeDispatcher
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState

/**
 * The pre-edit buffer: the Han characters converted so far but not yet committed, and the
 * cursor among them. Keeps [uiState] in step with libchewing's buffer.
 */
class PreEditBuffer(private val uiState: ImeUiState, private val ime: ImeDispatcher) {
    private val logTag = "PreEditBuffer"
    private var offset: Int = 0

    /** How the user moved the cursor, which decides what the move leads to. */
    enum class CursorMovedFrom {
        TOUCHSCREEN,
        PHYSICAL_KEYBOARD
    }

    private val cursor = Cursor()

    /** The user touched the buffer at character (code point, not UTF-16 unit) [touchedOffset]. */
    fun touchedAt(touchedOffset: Int) {
        offset = touchedOffset
        cursorMovedBy(CursorMovedFrom.TOUCHSCREEN)
    }

    /**
     * Moves libchewing's cursor to where the user asked: a touch opens the candidates for
     * that character, arrow keys only move and redraw.
     */
    fun cursorMovedBy(source: CursorMovedFrom) {
        when (source) {
            CursorMovedFrom.TOUCHSCREEN -> {
                cursor.moveToOffset(offset)

                // 如果使用者點選最後一個字的時候很邊邊角角，
                // 很可能 getOffsetForPosition() 算出來的值會超界，要扣回來
                if (offset >= uiState.preEditText.let { it.codePointCount(0, it.length) }) {
                    offset = (ChewingBridge.chewing.bufferLen() - 1).coerceAtLeast(0)
                }
            }

            CursorMovedFrom.PHYSICAL_KEYBOARD -> {
                offset = ChewingBridge.chewing.cursorCurrent()
                if (offset >= ChewingBridge.chewing.bufferLen()) {
                    offset = (ChewingBridge.chewing.bufferLen() - 1).coerceAtLeast(0)
                }
            }
        }

        renderCursor()
        // if the cursor is larger or equal to the buffer length, make the end a bit wider,
        // in order to make user aware of the cursor is at the end of the buffer
        uiState.preEditCursorAtEnd =
            ChewingBridge.chewing.bufferLen() > 0 && ChewingBridge.chewing.cursorCurrent() >= ChewingBridge.chewing.bufferLen()
    }

    // It just renders, presents underline for current cursor
    private fun renderCursor() {
        uiState.preEditCursorIndex = offset.takeIf { it >= 0 && it + 1 <= ChewingBridge.chewing.bufferLen() } ?: -1
    }

    /** Redraws the cursor where libchewing has it. */
    fun updateCursorPosition() {
        cursor.syncOffsetWithCursor()
        renderCursor()
    }

    /** Moves the cursor to the start of the buffer and redraws it. */
    fun updateCursorPositionToBegin() {
        cursor.moveToBegin()
        renderCursor()
    }

    /** Moves the cursor to the end of the buffer and redraws it. */
    fun updateCursorPositionToEnd() {
        cursor.moveToEnd()
        renderCursor()
    }

    /**
     * Reads the buffer back from libchewing after a key: commits what it has already pushed
     * out of the pre-edit buffer, redraws the text and cursor, and with the simple conversion
     * engine shows the candidates for a character just converted.
     */
    fun update() {
        Log.d(logTag, "offset: ${offset}, cursor: ${ChewingBridge.chewing.cursorCurrent()}")
        // chewing.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingBridge.chewing.commitCheck() == 1) {
            // Take the text along with the event: posted from within another event's handler,
            // it is only delivered after that handler returns, and ack() below empties the
            // commit buffer long before then.
            ime.dispatch(ImeAction.CommitTextInChewingCommitBuffer(ChewingBridge.chewing.commitString()))
            // 讓 chewing.commitCheck() 歸 0 (also cleans the commit buffer)
            ChewingBridge.chewing.ack()
        }

        uiState.preEditText = ChewingBridge.chewing.bufferStringStatic()
        // new text, the usual padding again
        uiState.preEditCursorAtEnd = false
        openCandidatesForSimpleConversionEngine()
        renderCursor()
    }

    /**
     * With the simple conversion engine, an open candidate window means a new Han character
     * has just been converted from Bopomofo, so show the candidates for it.
     */
    private fun openCandidatesForSimpleConversionEngine() {
        if (ChewingBridge.chewing.configGetInt("chewing.conversion_engine") == ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode) {
            if (ChewingUtil.candidateWindowOpened()) {
                ime.dispatch(ImeAction.SwitchToLayout(Layout.CANDIDATES))
            }
        }
    }

    /** Moves libchewing's cursor and keeps [offset] in step with it. */
    inner class Cursor {
        /** Walks from the start to character [offset] and opens the candidates there. */
        fun moveToOffset(offset: Int) {
            // close if any been opened candidate window first
            ChewingBridge.chewing.candClose()
            // move to first character
            ChewingBridge.chewing.handleHome()
            // move to clicked character
            repeat(offset) { ChewingBridge.chewing.handleRight() }
            // open candidates window
            ChewingBridge.chewing.candOpen()
        }

        /** To the end of the buffer, candidates closed. */
        fun moveToEnd() {
            // close if any been opened candidate window first
            ChewingBridge.chewing.candClose()
            // move to end
            ChewingBridge.chewing.handleEnd()
            offset = (ChewingBridge.chewing.cursorCurrent() - 1).coerceAtLeast(0)
        }

        /** To the start of the buffer, candidates closed. */
        fun moveToBegin() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.handleHome()
            offset = 0
        }

        /** Reads the cursor back from libchewing, kept within the buffer. */
        fun syncOffsetWithCursor() {
            offset = ChewingBridge.chewing.cursorCurrent()
                .coerceIn(0, (ChewingBridge.chewing.bufferLen() - 1).coerceAtLeast(0))
        }
    }
}
