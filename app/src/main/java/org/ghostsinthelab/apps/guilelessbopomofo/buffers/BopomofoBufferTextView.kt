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
import android.util.AttributeSet
import androidx.core.view.GestureDetectorCompat
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge

class BopomofoBufferTextView(context: Context, attrs: AttributeSet) :
    BufferTextView(context, attrs) {

    override lateinit var mDetector: GestureDetectorCompat

    init {
        // Purposes to this padding setting:
        //   1. Acts as, and looks like a cursor
        //   2. Set its height early
        val px = convertDpToPx(2F).toInt()
        this.setPadding(px, 0, px, 0)

        mDetector = GestureDetectorCompat(context, MyGestureListener())
    }

    override fun update() {
        this@BopomofoBufferTextView.text = ChewingBridge.chewing.bopomofoStringStatic()
    }

    inner class MyGestureListener: GestureListener()
}