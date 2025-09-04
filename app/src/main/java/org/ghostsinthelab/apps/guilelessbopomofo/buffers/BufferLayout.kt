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
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.android.flexbox.FlexboxLayout

class BufferLayout(context: Context, attrs: AttributeSet) : FlexboxLayout(context, attrs) {
    var mDetector: GestureDetector

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(MyGestureListener())
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(),
        GestureDetector.OnDoubleTapListener {
        // double tap to toggle compact layout
        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d("BufferLayout", "onDoubleTap")
            return super.onDoubleTap(e)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // forward event to detector
        event?.apply {
            mDetector.onTouchEvent(this)
        }
        return true
    }
}