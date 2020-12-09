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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.SymbolsPickerLayoutBinding

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardPanel"
    var currentCandidatesList: Int = 0
    private var currentOffset: Int = 0
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var symbolsPickerLayoutBinding: SymbolsPickerLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    override lateinit var serviceContext: GuilelessBopomofoService

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES}
    lateinit var currentKeyboardLayout: KeyboardLayout

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToSymbolsPicker(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToSymbolsPicker")
        v = imeService.viewBinding
        symbolsPickerLayoutBinding = SymbolsPickerLayoutBinding.inflate(imeService.layoutInflater)

        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(symbolsPickerLayoutBinding.root)
        currentKeyboardLayout = KeyboardLayout.SYMBOLS
    }

    fun switchToMainLayout(imeService: GuilelessBopomofoService = serviceContext) {
        Log.v(LOGTAG, "switchToMainLayout")
        v = imeService.viewBinding
        keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
        currentKeyboardLayout = KeyboardLayout.MAIN

        v.keyboardView.syncPreEditBuffers(imeService)
        // never forget to pass serviceContext here
        keyboardHsuLayoutBinding.root.setupImeSwitch(serviceContext)
        keyboardHsuLayoutBinding.root.setupPuncSwitch(serviceContext)
    }

    fun switchToCandidatesLayout(
        offset: Int,
        imeService: GuilelessBopomofoService = serviceContext
    ) {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        v = imeService.viewBinding
        candidatesLayoutBinding = CandidatesLayoutBinding.inflate(imeService.layoutInflater)
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(candidatesLayoutBinding.root)
        currentKeyboardLayout = KeyboardLayout.CANDIDATES

        // reset candidates list if offset has been changed
        if (currentOffset != offset) {
            currentOffset = offset
            currentCandidatesList = 0
        }

        // switch to next candidates list
        repeat(currentCandidatesList) {
            imeService.chewingEngine.candListNext()
        }

        // circulate candidates list cursor
        if (imeService.chewingEngine.candListHasNext()) {
            currentCandidatesList += 1
        } else {
            currentCandidatesList = 0
        }

        // Setup & bind RecyclerView
        val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView
        candidatesRecyclerView.adapter = CandidatesAdapter(imeService)
        candidatesRecyclerView.layoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL)
    }

}