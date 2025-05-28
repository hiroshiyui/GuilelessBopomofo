/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
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
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class BufferLayout(context: Context, attrs: AttributeSet) : FlexboxLayout(context, attrs) {
    var mDetector: GestureDetector
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)

    init {
        mDetector = GestureDetector(context, MyGestureListener())
        mDetector.setOnDoubleTapListener(MyGestureListener())
    }

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(),
        GestureDetector.OnDoubleTapListener {
        // double tap to toggle compact layout
        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.d("BufferLayout", "onDoubleTap")
            if (sharedPreferences.getBoolean(
                    "user_enhanced_compat_physical_keyboard",
                    false
                ) == true
            ) {
                EventBus.getDefault().post(Events.ToggleForceCompactLayout())
            }
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