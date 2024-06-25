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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.utils.DisplayMetricsComputable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibrable

abstract class KeyImageButton(context: Context, attrs: AttributeSet) :
    AppCompatImageButton(context, attrs, R.attr.imageButtonStyle),
    BehaveLikeKey<KeyImageButton>, DisplayMetricsComputable {
    open val logTag: String = "KeyImageButton"
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)
    override var keyCodeString: String? = null

    abstract var mDetector: GestureDetector
    abstract class GestureListener : GestureDetector.SimpleOnGestureListener(), Vibrable

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            mDetector.onTouchEvent(event)
        }
        return true
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyImageButton, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyImageButton_keyCodeString)
            } finally {
                recycle()
            }
        }

        this.apply {
            if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                elevation = convertDpToPx(2F)
            }

            val keyButtonPreferenceHeight = sharedPreferences.getInt("user_key_button_height", 52)
            minimumHeight = convertDpToPx(keyButtonPreferenceHeight.toFloat()).toInt()
        }
    }
}