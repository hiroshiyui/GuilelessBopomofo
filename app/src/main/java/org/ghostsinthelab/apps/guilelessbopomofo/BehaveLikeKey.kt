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

import android.view.KeyEvent
import android.view.View

interface BehaveLikeKey<T : View> {
    var keyCodeString: String?
    var keyType: Int?
    var keySymbol: String?

    // NOTICE: Should be in sync with attrs.xml
    enum class KeyType(val value: Int) {
        KEYTYPE_CHARACTER(0),
        KEYTYPE_CONTROL(1),
        KEYTYPE_MODIFIER(2),
        KEYTYPE_FUNCTION(3)
    }

    fun keyCode(): Int? {
        keyCodeString?.let {
            return KeyEvent.keyCodeFromString(it)
        }
        return null
    }

    fun isCharacterKey(): Boolean {
        return (keyType == KeyType.KEYTYPE_CHARACTER.value)
    }

    fun isControlKey(): Boolean {
        return (keyType == KeyType.KEYTYPE_CONTROL.value)
    }

    fun isModifierKey(): Boolean {
        return (keyType == KeyType.KEYTYPE_MODIFIER.value)
    }

    fun isFuntionKey(): Boolean {
        return (keyType == KeyType.KEYTYPE_FUNCTION.value)
    }
}
