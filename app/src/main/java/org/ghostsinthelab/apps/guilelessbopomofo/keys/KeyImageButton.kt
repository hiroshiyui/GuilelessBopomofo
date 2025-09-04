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
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.RegisteredSharedPreferences
import org.ghostsinthelab.apps.guilelessbopomofo.utils.DisplayMetricsComputable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

abstract class KeyImageButton(context: Context, attrs: AttributeSet) : MaterialButton(context, attrs, R.attr.imageButtonStyle),
    BehaveLikeKey<KeyImageButton>, DisplayMetricsComputable, Vibratable {
    open val logTag: String = "KeyImageButton"
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)
    override var keyCodeString: String? = null

    abstract var mDetector: GestureDetector

    abstract class GestureListener : GestureDetector.SimpleOnGestureListener(), Vibratable

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            mDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
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
            val keyButtonPreferenceHeight = sharedPreferences.getInt(RegisteredSharedPreferences.USER_KEY_BUTTON_HEIGHT.key, 52)
            minimumHeight = convertDpToPx(keyButtonPreferenceHeight.toFloat()).toInt()
        }
    }
}