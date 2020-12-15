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
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton

class KeyImageButton(context: Context, attrs: AttributeSet) :
    AppCompatImageButton(context, attrs, R.attr.imageButtonStyle),
    BehaveLikeKey<KeyImageButton>, DisplayMetricsComputable {
    private val LOGTAG: String = "KeyImageButton"
    private val sharedPreferences =
        context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)
    override var keyCodeString: String? = null
    override var keyType: Int? = null
    override var keySymbol: String? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyImageButton, 0, 0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyImageButton_keyCodeString)
                keyType = this.getInt(R.styleable.KeyImageButton_keyTypeEnum, -1)
                keySymbol = this.getString(R.styleable.KeyImageButton_keySymbolString)
            } finally {
                recycle()
            }
        }

        this.apply {
            minimumHeight = convertDpToPx(48F).toInt()
            minimumWidth = convertDpToPx(48F).toInt()

            if (sharedPreferences.getBoolean("user_enable_button_elevation", false)) {
                elevation = convertDpToPx(2F)
            }
        }
    }
}