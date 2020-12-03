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
import android.view.HapticFeedbackConstants
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding

class Keyboard(context: Context, attrs: AttributeSet): LinearLayout(context, attrs), GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "Keyboard"
    private lateinit var v: KeyboardLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        this.orientation = VERTICAL
        serviceContext = GuilelessBopomofoService()
    }

    fun setupImeSwitch(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        val keyImageButtonImeSwitch = v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonImeSwitch)
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