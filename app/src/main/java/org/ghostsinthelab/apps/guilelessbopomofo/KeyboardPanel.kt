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
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext

class KeyboardPanel(context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardLayout"
    private lateinit var v: KeyboardLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToSymbolsPicker(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        v.includeSymbolsPicker.linearLayoutKeyboardSymbols.visibility = View.VISIBLE
        v.linearLayoutKeyboard.visibility = View.GONE
    }

    fun switchToMainLayout(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        v.includeSymbolsPicker.linearLayoutKeyboardSymbols.visibility = View.GONE
        v.linearLayoutKeyboard.visibility = View.VISIBLE
    }

    fun setupImeSwitch(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        val keyImageButtonImeSwitch = v.keyImageButtonImeSwitch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            keyImageButtonImeSwitch.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imeService.switchToNextInputMethod(false)
            }
        }

        keyImageButtonImeSwitch.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val imm = imeService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return@setOnLongClickListener true
        }
    }
}