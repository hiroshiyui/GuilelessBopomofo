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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.emoji.widget.EmojiAppCompatTextView
import org.ghostsinthelab.apps.guilelessbopomofo.utils.DisplayMetricsComputable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Touchable

abstract class BufferTextView(context: Context, attrs: AttributeSet) :
    EmojiAppCompatTextView(context, attrs), DisplayMetricsComputable, Touchable {
    var mDetector: GestureDetectorCompat = GestureDetectorCompat(context, this)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return true
    }

    abstract fun update()
}