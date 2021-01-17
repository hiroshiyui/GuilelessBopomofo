/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
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

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.core.view.setPadding
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.events.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("ClickableViewAccessibility")
class PreEditBufferTextView(context: Context, attrs: AttributeSet) :
    BufferTextView(context, attrs) {
    private val LOGTAG = "PreEditBufferTextView"
    private lateinit var span: SpannableString

    // which character did I touched? (index value)
    var offset: Int = ChewingBridge.cursorCurrent()

    init {
        this.setOnTouchListener { v, event ->
            v as TextView

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    val y = event.y
                    offset = v.getOffsetForPosition(x, y)


                    return@setOnTouchListener false
                }
                else -> {
                    return@setOnTouchListener false
                }
            }
        }

        this.setOnClickListener {
            Log.v(LOGTAG, "offset: $offset")
            performHapticFeedback(GuilelessBopomofoServiceContext.serviceInstance.userHapticFeedbackStrength)
            EventBus.getDefault().post(PreEditBufferCursorChangedEvent.OnTouch())
            EventBus.getDefault().post(CandidatesWindowOpendEvent.Offset(offset))
        }
    }

    override fun onAttachedToWindow() {
        EventBus.getDefault().register(this)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreEditBufferCursorChangedEvent(event: PreEditBufferCursorChangedEvent.OnTouch) {
        Log.v(LOGTAG, "Offset: ${offset}, Cursor: ${ChewingBridge.cursorCurrent()}")
        ChewingUtil.moveToPreEditBufferOffset(offset)

        // 如果使用者點選最後一個字的時候很邊邊角角，
        // 很可能 getOffsetForPosition() 算出來的值會超界，要扣回來
        if (offset >= this.text.length) {
            offset -= 1
        }

        renderUnderlineSpan()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPreEditBufferCursorChangedEvent(event: PreEditBufferCursorChangedEvent.OnKeyboard) {
        Log.v(LOGTAG, "Offset: ${offset}, Cursor: ${ChewingBridge.cursorCurrent()}")

        offset = ChewingBridge.cursorCurrent()
        if (offset >= ChewingBridge.bufferLen()) {
            offset -= 1
        }

        renderUnderlineSpan()
    }

    private fun renderUnderlineSpan() {
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
            Log.e(LOGTAG, "StringIndexOutOfBoundsException")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBufferUpdatedEvent(event: BufferUpdatedEvent) {
        this.text = ChewingBridge.bufferStringStatic()
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
}