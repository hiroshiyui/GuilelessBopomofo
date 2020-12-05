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
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.view.children
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.*

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardPanel"
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding
    private lateinit var punctuationPickerLayoutBinding: PunctuationPickerLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToSymbolsPicker(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToSymbolsPicker")
        v = imeService.viewBinding
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)
        symbolsPickerLayoutBinding.keyImageButtonBack.setOnClickListener {
            switchToMainLayout(
                imeService
            )
        }
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(symbolsPickerLayoutBinding.root)
    }

    fun switchPunctuationPicker(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchPunctuationPicker")
        v = imeService.viewBinding
        val ic = imeService.currentInputConnection
        punctuationPickerLayoutBinding =
            PunctuationPickerLayoutBinding.inflate(imeService.layoutInflater)
        punctuationPickerLayoutBinding.keyImageButtonBack.setOnClickListener {
            switchToMainLayout(
                imeService
            )
        }

        punctuationPickerLayoutBinding.root.children.iterator().forEach {
            if (it is KeyboardRow) {
                it.children.iterator().forEach { child ->
                    if (child is KeyButton) {
                        child.setOnClickListener {
                            if (imeService.chewingEngine.commitPreeditBuf() == 0) {
                                ic.commitText(
                                    imeService.chewingEngine.commitStringStatic(),
                                    1
                                )
                            }
                            ic.commitText((child as KeyButton).keySymbol, 1)
                            v.keyboardView.syncPreEditBuffers()
                        }
                    }
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

    fun switchToCandidatesLayout(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        v = imeService.viewBinding
        candidatesLayoutBinding = CandidatesLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(candidatesLayoutBinding.root)

        val candidates = imeService.chewingEngine.candTotalChoice()

        repeat(candidates) { index ->
            Log.v(LOGTAG, imeService.chewingEngine.candStringByIndexStatic(index))
            val candidateButton: Button = Button(imeService.applicationContext)
            candidateButton.id = View.generateViewId()
            candidateButton.text = imeService.chewingEngine.candStringByIndexStatic(index)
            candidateButton.setOnClickListener {
                imeService.chewingEngine.candChooseByIndex(index)
                imeService.chewingEngine.candClose()
                imeService.chewingEngine.handleEnd()
                v.keyboardView.syncPreEditBuffers(imeService)
                v.keyboardPanel.switchToMainLayout(imeService)
            }
            candidatesLayoutBinding.CandidatesConstraintLayout.addView(candidateButton)
            candidatesLayoutBinding.CandidatesFlow.addView(candidateButton)
        }

        if (imeService.chewingEngine.candListHasNext()) {
            val nextCandListButton: Button = Button(imeService.applicationContext)
            nextCandListButton.id = View.generateViewId()
            nextCandListButton.text = resources.getString(R.string.next_cand_list)
            nextCandListButton.setOnClickListener {
                imeService.chewingEngine.candListNext()
                v.keyboardPanel.switchToCandidatesLayout(imeService)
            }
            candidatesLayoutBinding.CandidatesConstraintLayout.addView(nextCandListButton)
            candidatesLayoutBinding.CandidatesFlow.addView(nextCandListButton)
        }

        if (imeService.chewingEngine.candListHasPrev()) {
            val prevCandListButton: Button = Button(imeService.applicationContext)
            prevCandListButton.id = View.generateViewId()
            prevCandListButton.text = resources.getString(R.string.prev_cand_list)
            prevCandListButton.setOnClickListener {
                imeService.chewingEngine.candListPrev()
                v.keyboardPanel.switchToCandidatesLayout(imeService)
            }
            candidatesLayoutBinding.CandidatesConstraintLayout.addView(prevCandListButton)
            candidatesLayoutBinding.CandidatesFlow.addView(prevCandListButton)
        }
    }

}