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
import android.util.Log
import android.widget.RelativeLayout
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding

class KeyboardPanel(context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardLayout"
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToSymbolsPicker(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(symbolsPickerLayoutBinding.root)
    }

    fun switchToMainLayout(imeService: GuilelessBopomofoService = serviceContext) {
        v = imeService.viewBinding
        keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
        keyboardHsuLayoutBinding.root.setupImeSwitch(serviceContext)
    }
}