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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingEngine
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardPanel
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.events.CandidateSelectionDoneEvent
import org.ghostsinthelab.apps.guilelessbopomofo.events.CandidatesWindowOpendEvent
import org.greenrobot.eventbus.EventBus

class SymbolKeyImageButton(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    private var v: KeyboardLayoutBinding =
        GuilelessBopomofoServiceContext.serviceInstance.viewBinding
    private var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding =
        SymbolsPickerLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)

    init {
        this.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            ChewingEngine.openSymbolCandidates()

            v.keyboardPanel.apply {
                currentKeyboardLayout = KeyboardPanel.KeyboardLayout.SYMBOLS
                removeAllViews()
                addView(symbolsPickerLayoutBinding.root)
            }

            val totalCategories = ChewingEngine.candTotalChoice()

            repeat(totalCategories) { category ->
                val button: Button = Button(context)
                button.text =
                    ChewingEngine.candStringByIndexStatic(category)
                button.id = View.generateViewId()

                button.setOnClickListener {
                    ChewingEngine.candChooseByIndex(category)

                    if (ChewingEngine.hasCandidates()) {
                        // 如果候選區還有資料，代表目前進入次分類
                        EventBus.getDefault().post(CandidatesWindowOpendEvent())
                    } else {
                        EventBus.getDefault().post(CandidateSelectionDoneEvent())
                    }
                }

                symbolsPickerLayoutBinding.SymbolsConstraintLayout.addView(button)
                symbolsPickerLayoutBinding.SymbolsFlow.addView(button)
            }
        }
    }
}