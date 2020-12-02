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

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.text.toSpannable

class PreEditBufferTextView(context: Context, attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private val LOGTAG = "PreEditBufferTextView"
    private lateinit var span: SpannableString
    private val MAGIC_SHIFT_X = 0.75F

    init {
        setOnClickListener {
            Log.v(LOGTAG, "setOnClickListener")
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            return@setOnClickListener
        }

        setOnTouchListener { v, event ->
            Log.v(LOGTAG, "setOnTouchListener: ${event.action}")
            span = (v as TextView).text.toSpannable() as SpannableString
            val underlineSpans = span.getSpans(0, span.length, UnderlineSpan::class.java)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x * MAGIC_SHIFT_X
                    val y = event.y
                    var offset = (v as TextView).getOffsetForPosition(x, y)
                    if (offset >= this.text.length) {
                        offset -= 1
                    }
                    Log.v(LOGTAG, offset.toString())
                    underlineSpans?.forEach {
                        span.removeSpan(it)
                    }
                    try {
                        Log.v(LOGTAG, this.text[offset].toString())
                        span.setSpan(
                            UnderlineSpan(),
                            offset,
                            offset + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } catch (e: StringIndexOutOfBoundsException) {
                        Log.e(LOGTAG, "StringIndexOutOfBoundsException")
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_UP -> {
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_MOVE -> {
                    return@setOnTouchListener  false
                }
                MotionEvent.ACTION_MASK -> {
                    performClick()
                    return@setOnTouchListener false
                }
                else -> {
                    return@setOnTouchListener true
                }
            }
        }
    }

}