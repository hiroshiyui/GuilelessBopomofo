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
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardPanel"
    var currentCandidatesList: Int = 0
    private var currentOffset: Int = 0
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    override lateinit var guilelessBopomofoService: GuilelessBopomofoService

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES }

    lateinit var currentKeyboardLayout: KeyboardLayout

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    fun switchToMainLayout(guilelessBopomofoService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "switchToMainLayout")
        v = guilelessBopomofoService.viewBinding
        guilelessBopomofoService.setMainLayout()
        currentKeyboardLayout = KeyboardLayout.MAIN

        v.keyboardView.updateBuffers(guilelessBopomofoService)
    }

    // list current offset's candidates in the candidate window
    fun switchToCandidatesLayout(
        offset: Int,
        guilelessBopomofoService: GuilelessBopomofoService
    ) {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        // reset candidates list if offset has been changed
        if (currentOffset != offset) {
            currentOffset = offset
            currentCandidatesList = 0
        }

        // switch to next candidates list
        repeat(currentCandidatesList) {
            guilelessBopomofoService.chewingEngine.candListNext()
        }

        // circulate candidates list cursor
        if (guilelessBopomofoService.chewingEngine.candListHasNext()) {
            currentCandidatesList += 1
        } else {
            currentCandidatesList = 0
        }

        renderCandidatesLayout(guilelessBopomofoService)
    }

    // just list current candidate window
    fun switchToCandidatesLayout(guilelessBopomofoService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        renderCandidatesLayout(guilelessBopomofoService)
    }

    private fun renderCandidatesLayout(guilelessBopomofoService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "renderCandidatesLayout")
        candidatesLayoutBinding = CandidatesLayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
        v = guilelessBopomofoService.viewBinding
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(candidatesLayoutBinding.root)
        currentKeyboardLayout = KeyboardLayout.SYMBOLS

        val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView
        candidatesRecyclerView.adapter = CandidatesAdapter(guilelessBopomofoService)
        candidatesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL)

        val keyButtonBackToMain = candidatesLayoutBinding.keyButtonBackToMain
        keyButtonBackToMain.setBackMainLayoutOnClickListener(guilelessBopomofoService)
    }
}