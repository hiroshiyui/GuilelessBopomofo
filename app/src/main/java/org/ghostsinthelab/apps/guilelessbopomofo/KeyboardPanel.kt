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
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.PunctuationPickerLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding

class KeyboardPanel(context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardLayout"
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding
    private lateinit var punctuationPickerLayoutBinding: PunctuationPickerLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToSymbolsPicker(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToSymbolsPicker")
        v = imeService.viewBinding
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(symbolsPickerLayoutBinding.root)
    }

    fun switchPunctuationPicker(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchPunctuationPicker")
        v = imeService.viewBinding
        // TODO: 可以動，但是不應該放在這裡，要切開來，試試看放到 KeyButton 那邊
        val ic = imeService.currentInputConnection
        punctuationPickerLayoutBinding = PunctuationPickerLayoutBinding.inflate(imeService.layoutInflater)
        punctuationPickerLayoutBinding.let { it ->
            listOf(
                it.keyButtonPeriod,
                it.keyButtonIdeographicComma,
                it.keyButtonQuestionMark
            ).forEach { keyButton ->
                keyButton.setOnClickListener {
                    if (imeService.chewingEngine.commitPreeditBuf() == 0) {
                        ic.commitText(imeService.chewingEngine.commitStringStatic(), 1)
                    }
                    ic.commitText((it as KeyButton).keySymbol, 1)
                    v.keyboardView.syncPreEditString()
                }
            }
        }

        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(punctuationPickerLayoutBinding.root)
    }

    fun switchToMainLayout(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToMainLayout")
        v = imeService.viewBinding
        keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
        // never forget to pass serviceContext here
        keyboardHsuLayoutBinding.root.setupImeSwitch(serviceContext)
        keyboardHsuLayoutBinding.root.setupPuncSwitch(serviceContext)
    }
}