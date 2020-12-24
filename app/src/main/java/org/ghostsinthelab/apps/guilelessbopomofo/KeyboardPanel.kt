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
import android.widget.RelativeLayout
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.*

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs),
    GuilelessBopomofoServiceContext {
    private val LOGTAG: String = "KeyboardPanel"
    var currentCandidatesList: Int = 0
    private var currentOffset: Int = 0
    private lateinit var v: KeyboardLayoutBinding
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    private lateinit var keyboardEt26LayoutBinding: KeyboardEt26LayoutBinding
    private lateinit var keyboardDachenLayoutBinding: KeyboardDachenLayoutBinding
    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    private lateinit var keyboardQwertyLayoutBinding: KeyboardQwertyLayoutBinding
    override lateinit var guilelessBopomofoService: GuilelessBopomofoService

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES, QWERTY }

    lateinit var currentKeyboardLayout: KeyboardLayout

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        Log.v(LOGTAG, "onViewAdded - ${child}")
        v = guilelessBopomofoService.viewBinding

        when (currentKeyboardLayout) {
            KeyboardLayout.MAIN -> {
                v.keyboardView.updateBuffers(guilelessBopomofoService)

                child as Keyboard
                guilelessBopomofoService.apply {
                    child.setupImeSwitch(this)
                    child.setupPuncSwitch(this)
                    child.setupSymbolSwitch(this)
                    child.setupModeSwitch(this)
                    child.setupBackspace(this)
                }
          }
            KeyboardLayout.QWERTY -> {
                v.keyboardView.updateBuffers(guilelessBopomofoService)
                child as Keyboard
                guilelessBopomofoService.apply {
                    child.setupModeSwitch(this)
                    child.setupBackspace(this)
                }
            }
            KeyboardLayout.CANDIDATES -> {
                val keyButtonBackToMain = candidatesLayoutBinding.keyButtonBackToMain
                keyButtonBackToMain.setBackMainLayoutOnClickListener(guilelessBopomofoService)
            }
            else -> {
            }
        }
    }

    fun switchToMainLayout(guilelessBopomofoService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "switchToMainLayout")
        currentKeyboardLayout = KeyboardLayout.MAIN

        v = guilelessBopomofoService.viewBinding
        v.keyboardPanel.removeAllViews()

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        val userKeyboardLayoutPreference = guilelessBopomofoService.sharedPreferences.getString(
            "user_keyboard_layout",
            GuilelessBopomofoService.defaultKeyboardLayout
        )

        when (userKeyboardLayoutPreference) {
            "KB_HSU" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_HSU")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
                v.keyboardPanel.addView(keyboardHsuLayoutBinding.root)
            }
            "KB_ET26" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_ET26")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardEt26LayoutBinding = KeyboardEt26LayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
                v.keyboardPanel.addView(keyboardEt26LayoutBinding.root)
            }
            "KB_DEFAULT" -> {
                val newKeyboardType = ChewingEngine.convKBStr2Num("KB_DEFAULT")
                ChewingEngine.setKBType(newKeyboardType)
                keyboardDachenLayoutBinding = KeyboardDachenLayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
                v.keyboardPanel.addView(keyboardDachenLayoutBinding.root)
            }
        }
    }

    fun switchToQwertyLayout(guilelessBopomofoService: GuilelessBopomofoService) {
        Log.v(LOGTAG, "switchToQwertyLayout")
        currentKeyboardLayout = KeyboardLayout.QWERTY

        keyboardQwertyLayoutBinding =
            KeyboardQwertyLayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
        v = guilelessBopomofoService.viewBinding
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(keyboardQwertyLayoutBinding.root)
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
            ChewingEngine.candListNext()
        }

        // circulate candidates list cursor
        if (ChewingEngine.candListHasNext()) {
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
        currentKeyboardLayout = KeyboardLayout.CANDIDATES

        candidatesLayoutBinding =
            CandidatesLayoutBinding.inflate(guilelessBopomofoService.layoutInflater)
        v = guilelessBopomofoService.viewBinding
        v.keyboardPanel.removeAllViews()
        v.keyboardPanel.addView(candidatesLayoutBinding.root)

        val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView
        candidatesRecyclerView.adapter = CandidatesAdapter(guilelessBopomofoService)
        candidatesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL)
    }
}