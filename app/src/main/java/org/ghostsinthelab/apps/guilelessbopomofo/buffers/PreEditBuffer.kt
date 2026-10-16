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
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.greenrobot.eventbus.EventBus

/**
 * The pre-edit buffer: the Han characters converted so far but not yet committed, and the
 * cursor among them. Keeps [uiState] in step with libchewing's buffer.
 */
class PreEditBuffer(private val uiState: ImeUiState) {
    private val logTag = "PreEditBuffer"
    private var offset: Int = 0

    enum class CursorMovedFrom {
        TOUCHSCREEN,
        PHYSICAL_KEYBOARD
    }

    private val cursor = Cursor()

    /** The user touched the buffer at character [touchedOffset]. */
    fun touchedAt(touchedOffset: Int) {
        offset = touchedOffset
        cursorMovedBy(CursorMovedFrom.TOUCHSCREEN)
    }

    fun cursorMovedBy(source: CursorMovedFrom) {
        when (source) {
            CursorMovedFrom.TOUCHSCREEN -> {
                cursor.moveToOffset(offset)

                // 如果使用者點選最後一個字的時候很邊邊角角，
                // 很可能 getOffsetForPosition() 算出來的值會超界，要扣回來
                if (offset >= uiState.preEditText.length) {
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

    fun updateCursorPosition() {
        cursor.syncOffsetWithCursor()
        renderCursor()
    }

    fun updateCursorPositionToBegin() {
        cursor.moveToBegin()
        renderCursor()
    }

    fun updateCursorPositionToEnd() {
        cursor.moveToEnd()
        renderCursor()
    }

    fun update() {
        Log.d(logTag, "offset: ${offset}, cursor: ${ChewingBridge.chewing.cursorCurrent()}")
        // chewing.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingBridge.chewing.commitCheck() == 1) {
            // Take the text along with the event: posted from within another event's handler,
            // it is only delivered after that handler returns, and ack() below empties the
            // commit buffer long before then.
            EventBus.getDefault().post(Events.CommitTextInChewingCommitBuffer(ChewingBridge.chewing.commitString()))
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
                EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            }
        }
    }

    inner class Cursor {
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

        fun moveToEnd() {
            // close if any been opened candidate window first
            ChewingBridge.chewing.candClose()
            // move to end
            ChewingBridge.chewing.handleEnd()
            offset = (ChewingBridge.chewing.cursorCurrent() - 1).coerceAtLeast(0)
        }

        fun moveToBegin() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.handleHome()
            offset = 0
        }

        fun syncOffsetWithCursor() {
            offset = ChewingBridge.chewing.cursorCurrent()
                .coerceIn(0, (ChewingBridge.chewing.bufferLen() - 1).coerceAtLeast(0))
        }
    }
}
