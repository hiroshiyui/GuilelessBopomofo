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
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.*

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs) {
    private val LOGTAG: String = "KeyboardPanel"
    var currentCandidatesList: Int = 0
    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    private lateinit var keyboardHsuQwertyLayoutBinding: KeyboardHsuQwertyLayoutBinding
    private lateinit var keyboardEt26LayoutBinding: KeyboardEt26LayoutBinding
    private lateinit var keyboardEt26QwertyLayoutBinding: KeyboardEt26QwertyLayoutBinding
    private lateinit var keyboardDachenLayoutBinding: KeyboardDachenLayoutBinding
    private lateinit var keyboardQwertyLayoutBinding: KeyboardQwertyLayoutBinding
    private lateinit var keyboardHsuDvorakLayoutBinding: KeyboardHsuDvorakLayoutBinding
    private lateinit var keyboardHsuDvorakBothLayoutBinding: KeyboardHsuDvorakBothLayoutBinding
    private lateinit var keyboardDvorakLayoutBinding: KeyboardDvorakLayoutBinding
    private lateinit var compactLayoutBinding: CompactLayoutBinding

    private lateinit var candidatesLayoutBinding: CandidatesLayoutBinding
    val keyButtonPopupLayoutBinding: KeybuttonPopupLayoutBinding =
        KeybuttonPopupLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
    val keyButtonPopup = PopupWindow(keyButtonPopupLayoutBinding.root, 1, 1, false)

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES, QWERTY, DVORAK }

    lateinit var currentKeyboardLayout: KeyboardLayout

    init {
        Log.v(LOGTAG, "Building KeyboardLayout.")

        keyButtonPopup.apply {
            animationStyle = R.style.KeyButtonPopupAnimation
            elevation = 8F
        }
    }

    fun toggleMainLayoutMode() {
        when (ChewingBridge.getChiEngMode()) {
            SYMBOL_MODE -> {
                ChewingBridge.setChiEngMode(CHINESE_MODE)
                switchToBopomofoLayout()
            }
            CHINESE_MODE -> {
                ChewingBridge.setChiEngMode(SYMBOL_MODE)
                switchToAlphabeticalLayout()
            }
        }
    }

    fun switchToMainLayout() {
        if (ChewingBridge.getChiEngMode() == CHINESE_MODE) {
            switchToBopomofoLayout()
        } else {
            switchToAlphabeticalLayout()
        }
    }

    private fun switchToCompactLayout() {
        Log.v(LOGTAG, "switchToCompactLayout")
        this.removeAllViews()
        compactLayoutBinding =
            CompactLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
        if (ChewingBridge.getChiEngMode() == CHINESE_MODE) {
            compactLayoutBinding.textViewCurrentModeValue.text =
                resources.getString(R.string.mode_bopomofo)
        } else {
            compactLayoutBinding.textViewCurrentModeValue.text =
                resources.getString(R.string.mode_english)
        }
        this.addView(compactLayoutBinding.root)
    }

    private fun switchToBopomofoLayout() {
        Log.v(LOGTAG, "switchToMainLayout")
        currentKeyboardLayout = KeyboardLayout.MAIN

        this.removeAllViews()

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        val userKeyboardLayoutPreference =
            GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getString(
                "user_keyboard_layout",
                GuilelessBopomofoService.defaultKeyboardLayout
            )

        if (GuilelessBopomofoServiceContext.serviceInstance.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        userKeyboardLayoutPreference?.let {
            val newKeyboardType = ChewingBridge.convKBStr2Num(it)
            ChewingBridge.setKBType(newKeyboardType)
        }

        when (userKeyboardLayoutPreference) {
            "KB_HSU" -> {
                if (GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getBoolean(
                        "user_display_hsu_qwerty_layout",
                        false
                    )
                ) {
                    keyboardHsuQwertyLayoutBinding =
                        KeyboardHsuQwertyLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardHsuQwertyLayoutBinding.root)
                } else {
                    keyboardHsuLayoutBinding =
                        KeyboardHsuLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardHsuLayoutBinding.root)
                }
            }
            "KB_DVORAK_HSU" -> {
                if (GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getBoolean(
                        "user_display_dvorak_hsu_both_layout",
                        false
                    )
                ) {
                    keyboardHsuDvorakBothLayoutBinding =
                        KeyboardHsuDvorakBothLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardHsuDvorakBothLayoutBinding.root)
                } else {
                    keyboardHsuDvorakLayoutBinding =
                        KeyboardHsuDvorakLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardHsuDvorakLayoutBinding.root)
                }
            }
            "KB_ET26" -> {
                if (GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getBoolean(
                        "user_display_eten26_qwerty_layout",
                        false
                    )
                ) {
                    keyboardEt26QwertyLayoutBinding =
                        KeyboardEt26QwertyLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardEt26QwertyLayoutBinding.root)
                } else {
                    keyboardEt26LayoutBinding =
                        KeyboardEt26LayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                    this.addView(keyboardEt26LayoutBinding.root)
                }
            }
            "KB_DEFAULT" -> {
                keyboardDachenLayoutBinding =
                    KeyboardDachenLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
                this.addView(keyboardDachenLayoutBinding.root)
            }
        }
    }

    private fun switchToAlphabeticalLayout() {
        if (userIsUsingDvorakHsu()) {
            switchToDvorakLayout()
        } else {
            switchToQwertyLayout()
        }
    }

    private fun switchToQwertyLayout() {
        Log.v(LOGTAG, "switchToQwertyLayout")
        currentKeyboardLayout = KeyboardLayout.QWERTY

        if (GuilelessBopomofoServiceContext.serviceInstance.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        keyboardQwertyLayoutBinding =
            KeyboardQwertyLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)

        this.removeAllViews()
        this.addView(keyboardQwertyLayoutBinding.root)
    }

    private fun switchToDvorakLayout() {
        Log.v(LOGTAG, "switchToDvorakLayout")
        currentKeyboardLayout = KeyboardLayout.DVORAK

        if (GuilelessBopomofoServiceContext.serviceInstance.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        keyboardDvorakLayoutBinding =
            KeyboardDvorakLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)

        this.removeAllViews()
        this.addView(keyboardDvorakLayoutBinding.root)
    }

    private fun userIsUsingDvorakHsu(): Boolean {
        return (GuilelessBopomofoServiceContext.serviceInstance.sharedPreferences.getString(
            "user_keyboard_layout",
            GuilelessBopomofoService.defaultKeyboardLayout
        ) == "KB_DVORAK_HSU")
    }

    fun backToMainLayout() {
        // force back to main layout whatever a candidate has been chosen or not
        if (currentKeyboardLayout == KeyboardLayout.CANDIDATES) {
            ChewingBridge.candClose()
            ChewingBridge.handleEnd()
            switchToMainLayout()
        }
    }

    fun switchToSymbolPicker() {
        currentKeyboardLayout = KeyboardLayout.SYMBOLS
        ChewingUtil.openSymbolCandidates()
        renderCandidatesLayout()
    }

    fun candidateSelectionDone() {
        finishCandidateSelection()
    }

    fun candidateSelectionDone(index: Int) {
        ChewingBridge.candChooseByIndex(index)
        finishCandidateSelection()
    }

    private fun finishCandidateSelection() {
        if (ChewingUtil.candWindowClosed()) {
            ChewingBridge.handleEnd()
            updateBuffers()
            currentCandidatesList = 0
            switchToMainLayout()
        } else {
            renderCandidatesLayout()
        }
    }

    fun updateBuffers() {
        GuilelessBopomofoServiceContext.serviceInstance.viewBinding.apply {
            textViewPreEditBuffer.update()
            textViewBopomofoBuffer.update()
        }
    }

    // list current offset's candidates in the candidate window
    fun switchToCandidatesLayout(offset: Int) {
        Log.v(LOGTAG, "switchToCandidatesLayout")

        // switch to the target candidates list
        repeat(currentCandidatesList) {
            ChewingBridge.candListNext()
        }

        // circulate candidates list cursor
        if (ChewingBridge.candListHasNext()) {
            currentCandidatesList += 1
        } else {
            currentCandidatesList = 0
        }

        renderCandidatesLayout()
    }

    // just list current candidate window
    fun switchToCandidatesLayout() {
        Log.v(LOGTAG, "switchToCandidatesLayout")
        renderCandidatesLayout()
    }

    fun renderCandidatesLayout() {
        Log.v(LOGTAG, "renderCandidatesLayout")
        currentKeyboardLayout = KeyboardLayout.CANDIDATES

        candidatesLayoutBinding =
            CandidatesLayoutBinding.inflate(GuilelessBopomofoServiceContext.serviceInstance.layoutInflater)
        this.removeAllViews()
        this.addView(candidatesLayoutBinding.root)

        val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView

        if (!GuilelessBopomofoServiceContext.serviceInstance.physicalKeyboardEnabled) {
            candidatesRecyclerView.adapter = CandidatesAdapter()
            candidatesRecyclerView.layoutManager =
                GridLayoutManager(context, 4, LinearLayoutManager.HORIZONTAL, false)
        } else {
            val layoutManager = FlexboxLayoutManager(context)
            candidatesRecyclerView.adapter = PagedCandidatesAdapter(ChewingBridge.candCurrentPage())
            candidatesRecyclerView.layoutManager = layoutManager
        }
    }
}