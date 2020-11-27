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
import androidx.appcompat.widget.AppCompatButton

class KeyButton(context: Context, attrs: AttributeSet): AppCompatButton(context, attrs), BehaveLikeKey<KeyButton> {
    override var keyCodeString: String? = null
    override var keyType: Int? = null
    override var keySymbol: Char? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.KeyButton, 0 ,0).apply {
            try {
                keyCodeString = this.getString(R.styleable.KeyButton_keyCodeString)
                keyType = this.getInt(R.styleable.KeyButton_keyTypeEnum, 0)
                keySymbol = this.getString(R.styleable.KeyImageButton_keySymbolString)?.get(0)
            } finally {
                recycle()
            }
        }
    }
}