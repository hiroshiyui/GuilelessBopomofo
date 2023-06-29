/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo.buffers

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.text.toSpannable
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.setPadding
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.events.CommitTextEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.CursorMovedByPhysicalKeyboardEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.SwitchToCandidatesLayoutEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.UpdateBuffersEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.UpdateCursorEvent
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PreEditBufferTextView(context: Context, attrs: AttributeSet) :
    BufferTextView(context, attrs), Vibratable {
    private val logTag = "PreEditBufferTextView"
    private lateinit var span: SpannableString
    override lateinit var mDetector: GestureDetectorCompat

    enum class CursorMovedBy {
        TOUCH,
        PHYSICAL_KEYBOARD
    }

    init {
        mDetector = GestureDetectorCompat(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun onUpdateBuffersEvent(event: UpdateBuffersEvent) {
        this.update()
    }

    @Subscribe
    fun onUpdateCursorEvent(event: UpdateCursorEvent) {
        this.offset = ChewingBridge.cursorCurrent()
        this.renderUnderlineSpan()
    }

    @Subscribe
    fun onCursorMovedByPhysicalKeyboardEvent(event: CursorMovedByPhysicalKeyboardEvent) {
        Log.d(logTag, "onCursorMovedByPhysicalKeyboardEvent")
        this.cursorMovedBy(CursorMovedBy.PHYSICAL_KEYBOARD)
    }

    // which character did I touched? (index value)
    var offset: Int = ChewingBridge.cursorCurrent()

    fun cursorMovedBy(source: CursorMovedBy) {
        when (source) {
            CursorMovedBy.TOUCH -> {
                ChewingUtil.moveToPreEditBufferOffset(offset)

                // 如果使用者點選最後一個字的時候很邊邊角角，
                // 很可能 getOffsetForPosition() 算出來的值會超界，要扣回來
                if (offset >= this.text.length) {
                    Log.d(logTag, "Offset is over the buffer length!")
                    offset = this.text.length - 1
                }
            }
            CursorMovedBy.PHYSICAL_KEYBOARD -> {
                offset = ChewingBridge.cursorCurrent()
                if (offset >= ChewingBridge.bufferLen()) {
                    Log.d(logTag, "Offset is over the buffer length!")
                    offset = this.text.length - 1
                }
            }
        }
        renderUnderlineSpan()
    }

    fun renderUnderlineSpan() {
        span = this.text.toSpannable() as SpannableString
        val underlineSpans = span.getSpans(0, span.length, UnderlineSpan::class.java)

        // clear the existent underlines first
        underlineSpans?.forEach {
            span.removeSpan(it)
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
        }
    }

    override fun update() {
        // chewingEngine.setMaxChiSymbolLen() 到達閾值時，
        // 會把 pre-edit buffer 開頭送到 commit buffer，
        // 所以要先丟出來：
        if (ChewingBridge.commitCheck() == 1) {
            EventBus.getDefault().post(CommitTextEvent())
            // dirty hack (?) - 讓 chewingEngine.commitCheck() 歸 0
            // 研究 chewing_commit_Check() 之後想到的，並不是亂碰運氣
            ChewingBridge.handleEnd()
        }

        this@PreEditBufferTextView.text = ChewingBridge.bufferStringStatic()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (lengthAfter != 0) {
            // improve character click accuracy, leave text far from edges
            val px = convertDpToPx(12F).toInt()
            this.setPadding(px, 0, px, 0)
        } else {
            this.setPadding(0)
        }
    }

    inner class MyGestureListener : GestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            offset = getOffsetForPosition(x, y)
            Log.d(logTag, "offset: $offset")
            performVibrate(context, Vibratable.VibrationStrength.LIGHT)

            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            this@PreEditBufferTextView.cursorMovedBy(CursorMovedBy.TOUCH)
            EventBus.getDefault().post(SwitchToCandidatesLayoutEvent())
            return true
        }
    }
}