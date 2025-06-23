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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.DisplayMetricsComputable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

abstract class KeyButton(context: Context, attrs: AttributeSet) :
    AppCompatButton(context, attrs, R.attr.buttonStyle), BehaveLikeKey<KeyButton>,
    DisplayMetricsComputable {
    private val sharedPreferences =
        context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)
    override var keyCodeString: String? = null

    abstract var mDetector: GestureDetector

    abstract class GestureListener : GestureDetector.SimpleOnGestureListener(), Vibratable

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            mDetector.onTouchEvent(event)
        }
        return true
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyButton, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyButton_keyCodeString)
            } finally {
                recycle()
            }
        }

        this.apply {
            if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                elevation = convertDpToPx(2F)
            }
        }
    }
}