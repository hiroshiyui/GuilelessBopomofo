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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager

class LeftAlt : PhysicalKeyHandler {
    override fun onKeyDown(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        event?.startTracking()
        return true
    }

    override fun onKeyLongPress(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        return super.onKeyLongPress(context, keyCode, event)
    }
}