/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CompactLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDachenLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDvorakLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26QwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuDvorakBothLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuDvorakLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeybuttonPopupLayoutBinding
import kotlin.coroutines.CoroutineContext

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs), CoroutineScope {
    private val logTag: String = "KeyboardPanel"

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var currentCandidatesList: Int = 0
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

    // keyButtonPopup
    val keyButtonPopupLayoutBinding: KeybuttonPopupLayoutBinding =
        KeybuttonPopupLayoutBinding.inflate(LayoutInflater.from(context))
    val keyButtonPopup = PopupWindow(keyButtonPopupLayoutBinding.root, 1, 1, false)

    // candidatesRecyclerView
    private val candidatesLayoutBinding: CandidatesLayoutBinding =
        CandidatesLayoutBinding.inflate(LayoutInflater.from(context))
    private val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView

    enum class KeyboardLayout { MAIN, SYMBOLS, CANDIDATES, QWERTY, DVORAK }

    lateinit var currentKeyboardLayout: KeyboardLayout

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)

    init {
        Log.d(logTag, "Building KeyboardLayout.")

        keyButtonPopup.apply {
            elevation = 8F
        }
    }

    fun toggleMainLayoutMode() {
        Log.d(logTag, "toggleMainLayoutMode()")
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
        Log.d(logTag, "switchToCompactLayout")
        this.removeAllViews()
        compactLayoutBinding =
            CompactLayoutBinding.inflate(LayoutInflater.from(context))
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
        Log.d(logTag, "switchToMainLayout")
        currentKeyboardLayout = KeyboardLayout.MAIN

        this.removeAllViews()

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        val userKeyboardLayoutPreference =
            sharedPreferences.getString(
                "user_keyboard_layout",
                GuilelessBopomofoService.defaultKeyboardLayout
            )

        userKeyboardLayoutPreference?.let {
            val newKeyboardType = ChewingBridge.convKBStr2Num(it)
            ChewingBridge.setKBType(newKeyboardType)
        }

        // Toggle to compact layout when physical keyboard is enabled:
        if (GuilelessBopomofoServiceContext.service.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        // Or we will use soft, on-screen keyboard:
        when (userKeyboardLayoutPreference) {
            "KB_HSU" -> {
                if (sharedPreferences.getBoolean(
                        "user_display_hsu_qwerty_layout",
                        false
                    )
                ) {
                    keyboardHsuQwertyLayoutBinding =
                        KeyboardHsuQwertyLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuQwertyLayoutBinding.root)
                } else {
                    keyboardHsuLayoutBinding =
                        KeyboardHsuLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuLayoutBinding.root)
                }
            }

            "KB_DVORAK_HSU" -> {
                if (sharedPreferences.getBoolean(
                        "user_display_dvorak_hsu_both_layout",
                        false
                    )
                ) {
                    keyboardHsuDvorakBothLayoutBinding =
                        KeyboardHsuDvorakBothLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuDvorakBothLayoutBinding.root)
                } else {
                    keyboardHsuDvorakLayoutBinding =
                        KeyboardHsuDvorakLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuDvorakLayoutBinding.root)
                }
            }

            "KB_ET26" -> {
                if (sharedPreferences.getBoolean(
                        "user_display_eten26_qwerty_layout",
                        false
                    )
                ) {
                    keyboardEt26QwertyLayoutBinding =
                        KeyboardEt26QwertyLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardEt26QwertyLayoutBinding.root)
                } else {
                    keyboardEt26LayoutBinding =
                        KeyboardEt26LayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardEt26LayoutBinding.root)
                }
            }

            "KB_DEFAULT" -> {
                keyboardDachenLayoutBinding =
                    KeyboardDachenLayoutBinding.inflate(LayoutInflater.from(context))
                this.addView(keyboardDachenLayoutBinding.root)
            }
        }
    }

    fun switchToAlphabeticalLayout() {
        if (userIsUsingDvorakHsu()) {
            switchToDvorakLayout()
        } else {
            switchToQwertyLayout()
        }
    }

    private fun switchToQwertyLayout() {
        Log.d(logTag, "switchToQwertyLayout")
        currentKeyboardLayout = KeyboardLayout.QWERTY

        if (GuilelessBopomofoServiceContext.service.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        keyboardQwertyLayoutBinding =
            KeyboardQwertyLayoutBinding.inflate(LayoutInflater.from(context))

        this.removeAllViews()
        this.addView(keyboardQwertyLayoutBinding.root)
    }

    private fun switchToDvorakLayout() {
        Log.d(logTag, "switchToDvorakLayout")
        currentKeyboardLayout = KeyboardLayout.DVORAK

        if (GuilelessBopomofoServiceContext.service.physicalKeyboardEnabled) {
            switchToCompactLayout()
            return
        }

        keyboardDvorakLayoutBinding =
            KeyboardDvorakLayoutBinding.inflate(LayoutInflater.from(context))

        this.removeAllViews()
        this.addView(keyboardDvorakLayoutBinding.root)
    }

    private fun userIsUsingDvorakHsu(): Boolean {
        return (sharedPreferences.getString(
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
            ChewingBridge.candClose()
            ChewingBridge.handleEnd()
            updateBuffers()
            currentCandidatesList = 0
            candidatesRecyclerView.adapter = null
            switchToMainLayout()
        } else {
            renderCandidatesLayout()
        }
    }

    fun updateBuffers() {
        GuilelessBopomofoServiceContext.service.viewBinding.apply {
            launch { textViewPreEditBuffer.update() }
            launch { textViewBopomofoBuffer.update() }
        }
    }

    // list current offset's candidates in the candidate window
    fun switchToCandidatesLayout(offset: Int) {
        Log.d(logTag, "switchToCandidatesLayout")

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
        Log.d(logTag, "switchToCandidatesLayout")
        renderCandidatesLayout()
    }

    fun renderCandidatesLayout() {
        Log.d(logTag, "renderCandidatesLayout")
        currentKeyboardLayout = KeyboardLayout.CANDIDATES

        this.removeAllViews()
        this.addView(candidatesLayoutBinding.root)

        if (!GuilelessBopomofoServiceContext.service.physicalKeyboardEnabled) {
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