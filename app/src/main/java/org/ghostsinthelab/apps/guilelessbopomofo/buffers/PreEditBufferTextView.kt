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

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.text.toSpannable
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.ConversionEngines
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

class PreEditBufferTextView(context: Context, attrs: AttributeSet) :
    BufferTextView(context, attrs), Vibratable {
    private val logTag = "PreEditBufferTextView"
    private lateinit var span: SpannableString
    override var mDetector: GestureDetector
    var offset: Int = 0
    val paddingPx = convertDpToPx(12F).toInt()

    enum class CursorMovedFrom {
        TOUCHSCREEN,
        PHYSICAL_KEYBOARD
    }

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    fun cursorMovedBy(source: CursorMovedFrom) {
        when (source) {
            CursorMovedFrom.TOUCHSCREEN -> {
                Cursor().moveToOffset(offset)

                // 如果使用者點選最後一個字的時候很邊邊角角，
                // 很可能 getOffsetForPosition() 算出來的值會超界，要扣回來
                if (offset >= this.text.length) {
                    offset = ChewingBridge.chewing.bufferLen() - 1
                }
            }

            CursorMovedFrom.PHYSICAL_KEYBOARD -> {
                offset = ChewingBridge.chewing.cursorCurrent()
                if (offset >= ChewingBridge.chewing.bufferLen()) {
                    offset = ChewingBridge.chewing.bufferLen() - 1
                }
            }
        }

        renderCursorUnderlineSpan()
        renderPaddingEnd()
    }

    // It just renders, presents underline for current cursor
    private fun renderCursorUnderlineSpan() {
        span = this.text.toSpannable() as SpannableString
        val underlineSpans = span.getSpans(0, span.length, UnderlineSpan::class.java)

        // clear the existent underlines first
        underlineSpans?.forEach {
            span.removeSpan(it)
        }

        // Avoids IndexOutOfBoundsException early, just skip the rendering:
        if (offset < 0) {
            return
        }
        if (offset + 1 > ChewingBridge.chewing.bufferLen()) {
            return
        }

        try {
            span.setSpan(
                UnderlineSpan(),
                offset,
                offset + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: StringIndexOutOfBoundsException) {
            Log.e(logTag, "StringIndexOutOfBoundsException")
        } catch (e: IndexOutOfBoundsException) {
            // For physical keyboard users, it's a very common case for them to change offset by keys,
            // and sometimes make it out of bounds.
            Log.e(logTag, "IndexOutOfBoundsException")
        }
    }

    private fun renderPaddingEnd() {
        // if the cursor is larger or equal to the buffer length, make the padding end a bit wider,
        // in order to make user aware of the cursor is at the end of the buffer
        if (ChewingBridge.chewing.bufferLen() > 0 && ChewingBridge.chewing.cursorCurrent() >= ChewingBridge.chewing.bufferLen()) {
            Log.d(logTag, "Expand paddingEnd")
            this@PreEditBufferTextView.updatePadding(right = paddingPx + convertDpToPx(12F).toInt())
        } else {
            Log.d(logTag, "Shrink paddingEnd")
            this@PreEditBufferTextView.updatePadding(right = paddingPx)
        }
    }

    fun updateCursorPosition() {
        Cursor().syncOffsetWithCursor()
        renderCursorUnderlineSpan()
    }

    fun updateCursorPositionToBegin() {
        Cursor().moveToBegin()
        renderCursorUnderlineSpan()
    }

    fun updateCursorPositionToEnd() {
        Cursor().moveToEnd()
        renderCursorUnderlineSpan()
    }

    override fun update() {
        Log.d(logTag, "offset: ${offset}, cursor: ${ChewingBridge.chewing.cursorCurrent()}")
        // chewing.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingBridge.chewing.commitCheck() == 1) {
            EventBus.getDefault().post(Events.CommitTextInChewingCommitBuffer())
            // 讓 chewing.commitCheck() 歸 0 (also cleans the commit buffer)
            ChewingBridge.chewing.ack()
        }

        this@PreEditBufferTextView.text = ChewingBridge.chewing.bufferStringStatic()
        renderCursorUnderlineSpan()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int,
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (lengthAfter != 0) {
            // improve character click accuracy, leave text far from edges
            this.setPadding(paddingPx, 0, paddingPx, 0)
        } else {
            this.setPadding(0)
        }

        // open candidate window if conversion engine is simple and candidate window is opening
        // (which means a new Han character is converted from Bopomofo)
        if (ChewingBridge.chewing.configGetInt("chewing.conversion_engine") == ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode) {
            if (ChewingUtil.candidateWindowOpened()) {
                EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            }
        }
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            offset = getOffsetForPosition(x, y)
            Log.d(logTag, "offset: $offset")
            performVibration(context, Vibratable.VibrationStrength.LIGHT)

            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            this@PreEditBufferTextView.cursorMovedBy(CursorMovedFrom.TOUCHSCREEN)
            EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            return true
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
            offset = ChewingBridge.chewing.cursorCurrent() - 1
        }

        fun moveToBegin() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.handleHome()
            offset = 0
        }

        fun syncOffsetWithCursor() {
            offset = ChewingBridge.chewing.cursorCurrent()
            if (offset < 0) {
                offset = 0
            }
            if (offset >= ChewingBridge.chewing.bufferLen()) {
                offset = ChewingBridge.chewing.bufferLen() - 1
            }
        }
    }
}