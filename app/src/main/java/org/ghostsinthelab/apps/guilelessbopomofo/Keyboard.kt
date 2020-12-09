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
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding

class Keyboard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val LOGTAG: String = "Keyboard"
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding

    init {
        this.orientation = VERTICAL
    }

    fun setupImeSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupImeSwitch")
        v = imeService.viewBinding
        val keyImageButtonImeSwitch =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonImeSwitch)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            keyImageButtonImeSwitch.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imeService.switchToNextInputMethod(false)
            }
        }

        keyImageButtonImeSwitch.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val imm =
                imeService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return@setOnLongClickListener true
        }
    }

    fun setupPuncSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupPuncSwitch")
        v = imeService.viewBinding
        val keyImageButtonPunc =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonPunc)
        keyImageButtonPunc.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            imeService.chewingEngine.candClose()
            // 「常用符號」
            imeService.chewingEngine.handleDefault('`')
            imeService.chewingEngine.handleDefault('3')
            imeService.chewingEngine.candOpen()

            candidatesLayoutBinding = CandidatesLayoutBinding.inflate(imeService.layoutInflater)
            v.keyboardPanel.removeAllViews()
            v.keyboardPanel.addView(candidatesLayoutBinding.root)
            v.keyboardPanel.currentKeyboardLayout = KeyboardPanel.KeyboardLayout.CANDIDATES

            val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView
            candidatesRecyclerView.adapter = CandidatesAdapter(imeService)
            val layoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL)
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            candidatesRecyclerView.layoutManager = layoutManager

            return@setOnLongClickListener true
        }
    }

    fun setupSymbolSwitch(imeService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "setupSymbolSwitch")
        v = imeService.viewBinding
        val keyImageButtonSymbol =
            v.keyboardPanel.findViewById<KeyImageButton>(R.id.keyImageButtonSymbol)
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)

        keyImageButtonSymbol.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            imeService.chewingEngine.handleDefault('`')
            imeService.chewingEngine.candOpen()

            v.keyboardPanel.removeAllViews()
            v.keyboardPanel.addView(symbolsPickerLayoutBinding.root)
            v.keyboardPanel.currentKeyboardLayout = KeyboardPanel.KeyboardLayout.SYMBOLS

            val totalCategories = imeService.chewingEngine.candTotalChoice()

            repeat(totalCategories) { category ->
                val button: Button = Button(context)
                button.text = imeService.chewingEngine.candStringByIndexStatic(category)
                button.id = View.generateViewId()

                button.setOnClickListener { listener ->
                    imeService.chewingEngine.candChooseByIndex(category)

                    Log.v(LOGTAG, "'${imeService.chewingEngine.candStringByIndexStatic(0)}'")

                    if (imeService.chewingEngine.candStringByIndexStatic(0).isEmpty()) {
                        imeService.chewingEngine.candClose()
                        imeService.chewingEngine.handleEnd()
                        imeService.viewBinding.keyboardView.syncPreEditBuffers(imeService)
                        imeService.viewBinding.keyboardPanel.switchToMainLayout(imeService)
                    } else { // 如果候選區還有資料，代表目前進入次分類
                        imeService.viewBinding.keyboardPanel.switchToCandidatesLayout(imeService)
                    }

                    imeService.viewBinding.keyboardPanel.currentCandidatesList = 0
                }

                symbolsPickerLayoutBinding.SymbolsConstraintLayout.addView(button)
                symbolsPickerLayoutBinding.SymbolsFlow.addView(button)
            }
        }
    }
}