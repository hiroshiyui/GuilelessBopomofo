/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CompactLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDachenLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDvorakLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26QwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt41LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuDvorakBothLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuDvorakLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.greenrobot.eventbus.EventBus

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs) {
    private val logTag: String = "KeyboardPanel"

    internal var lastChewingCursor: Int = 0
    private var currentCandidatesList: Int = 0

    private lateinit var keyboardHsuLayoutBinding: KeyboardHsuLayoutBinding
    private lateinit var keyboardHsuQwertyLayoutBinding: KeyboardHsuQwertyLayoutBinding
    private lateinit var keyboardEt26LayoutBinding: KeyboardEt26LayoutBinding
    private lateinit var keyboardEt26QwertyLayoutBinding: KeyboardEt26QwertyLayoutBinding
    private lateinit var keyboardEt41LayoutBinding: KeyboardEt41LayoutBinding
    private lateinit var keyboardDachenLayoutBinding: KeyboardDachenLayoutBinding
    private lateinit var keyboardQwertyLayoutBinding: KeyboardQwertyLayoutBinding
    private lateinit var keyboardHsuDvorakLayoutBinding: KeyboardHsuDvorakLayoutBinding
    private lateinit var keyboardHsuDvorakBothLayoutBinding: KeyboardHsuDvorakBothLayoutBinding
    private lateinit var keyboardDvorakLayoutBinding: KeyboardDvorakLayoutBinding
    private lateinit var compactLayoutBinding: CompactLayoutBinding

    // candidatesRecyclerView
    private val candidatesLayoutBinding: CandidatesLayoutBinding = CandidatesLayoutBinding.inflate(LayoutInflater.from(context))
    private val candidatesRecyclerView = candidatesLayoutBinding.CandidatesRecyclerView

    var currentLayout: Layout = Layout.MAIN

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(APP_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)

    init {
        Log.d(logTag, "Building KeyboardLayout.")
    }

    fun toggleMainLayoutMode() {
        Log.d(logTag, "toggleMainLayoutMode()")

        when (ChewingBridge.chewing.getChiEngMode()) {
            ChiEngMode.SYMBOL.mode -> {
                ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
                switchToBopomofoLayout()
            }

            ChiEngMode.CHINESE.mode -> {
                ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
                switchToAlphanumericalLayout()
            }
        }
    }

    fun switchToLayout(layout: Layout) {
        currentLayout = layout
        when (layout) {
            Layout.MAIN -> {
                switchToMainLayout()
            }

            Layout.CANDIDATES -> {
                switchToCandidatesLayout()
            }

            Layout.SYMBOLS -> {
                switchToSymbolPicker()
            }

            Layout.COMPACT -> {
                switchToCompactLayout()
            }

            else -> {}
        }
    }

    private fun switchToMainLayout() {
        Log.d(logTag, "switchToMainLayout()")

        if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
            switchToBopomofoLayout()
        } else {
            switchToAlphanumericalLayout()
        }
    }

    fun switchToCompactLayout() {
        Log.d(logTag, "switchToCompactLayout")
        currentLayout = Layout.COMPACT
        this.removeAllViews()
        compactLayoutBinding = CompactLayoutBinding.inflate(LayoutInflater.from(context))
        if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
            compactLayoutBinding.textViewCurrentModeValue.text = resources.getString(R.string.mode_bopomofo)
        } else {
            compactLayoutBinding.textViewCurrentModeValue.text = resources.getString(R.string.mode_alphanumerical)
        }

        when (ChewingBridge.chewing.getShapeMode()) {
            ShapeMode.HALF.mode -> {
                compactLayoutBinding.textViewCurrentWidthModeValue.text = resources.getString(R.string.half_width_mode)
            }

            ShapeMode.FULL.mode -> {
                compactLayoutBinding.textViewCurrentWidthModeValue.text = resources.getString(R.string.full_width_mode)
            }
        }

        this.addView(compactLayoutBinding.root)
    }

    private fun switchToBopomofoLayout() {
        Log.d(logTag, "switchToBopomofoLayout()")
        currentLayout = Layout.MAIN

        this.removeAllViews()

        // 不同注音鍵盤排列的抽換 support different Bopomofo keyboard layouts
        val userKeyboardLayoutPreference = sharedPreferences.getString(
            USER_KEYBOARD_LAYOUT, BopomofoKeyboards.KB_DEFAULT.layout
        )

        userKeyboardLayoutPreference?.let {
            val newKeyboardType = ChewingBridge.chewing.convKBStr2Num(it)
            ChewingBridge.chewing.setKBType(newKeyboardType)
        }

        // Toggle to compact layout when physical keyboard is enabled:
        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        // Or we will use soft, on-screen keyboard:
        when (userKeyboardLayoutPreference) {
            "KB_HSU" -> {
                if (sharedPreferences.getBoolean(
                        USER_DISPLAY_HSU_QWERTY_LAYOUT, false
                    )
                ) {
                    keyboardHsuQwertyLayoutBinding = KeyboardHsuQwertyLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuQwertyLayoutBinding.root)
                } else {
                    keyboardHsuLayoutBinding = KeyboardHsuLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuLayoutBinding.root)
                }
                return
            }

            "KB_DVORAK_HSU" -> {
                if (sharedPreferences.getBoolean(
                        USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT, false
                    )
                ) {
                    keyboardHsuDvorakBothLayoutBinding = KeyboardHsuDvorakBothLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuDvorakBothLayoutBinding.root)
                } else {
                    keyboardHsuDvorakLayoutBinding = KeyboardHsuDvorakLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardHsuDvorakLayoutBinding.root)
                }
                return
            }

            "KB_ET26" -> {
                if (sharedPreferences.getBoolean(
                        USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false
                    )
                ) {
                    keyboardEt26QwertyLayoutBinding = KeyboardEt26QwertyLayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardEt26QwertyLayoutBinding.root)
                } else {
                    keyboardEt26LayoutBinding = KeyboardEt26LayoutBinding.inflate(LayoutInflater.from(context))
                    this.addView(keyboardEt26LayoutBinding.root)
                }
                return
            }

            "KB_ET" -> {
                keyboardEt41LayoutBinding = KeyboardEt41LayoutBinding.inflate(LayoutInflater.from(context)).also {
                    this.addView(it.root)
                }
                return
            }

            "KB_DEFAULT" -> {
                keyboardDachenLayoutBinding = KeyboardDachenLayoutBinding.inflate(LayoutInflater.from(context))
                this.addView(keyboardDachenLayoutBinding.root)
                return
            }
        }
    }

    private fun switchToAlphanumericalLayout() {
        Log.d(logTag, "switchToAlphanumericalLayout()")

        if (userIsUsingDvorakHsu()) {
            switchToDvorakLayout()
        } else {
            switchToQwertyLayout()
        }
    }

    private fun switchToQwertyLayout() {
        Log.d(logTag, "switchToQwertyLayout")
        currentLayout = Layout.QWERTY

        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        keyboardQwertyLayoutBinding = KeyboardQwertyLayoutBinding.inflate(LayoutInflater.from(context))

        this.removeAllViews()
        this.addView(keyboardQwertyLayoutBinding.root)
    }

    private fun switchToDvorakLayout() {
        Log.d(logTag, "switchToDvorakLayout")
        currentLayout = Layout.DVORAK

        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        keyboardDvorakLayoutBinding = KeyboardDvorakLayoutBinding.inflate(LayoutInflater.from(context))

        this.removeAllViews()
        this.addView(keyboardDvorakLayoutBinding.root)
    }

    private fun userIsUsingDvorakHsu(): Boolean {
        return (sharedPreferences.getString(
            USER_KEYBOARD_LAYOUT, BopomofoKeyboards.KB_DEFAULT.layout
        ) == "KB_DVORAK_HSU")
    }

    private fun switchToSymbolPicker() {
        currentLayout = Layout.SYMBOLS
        ChewingUtil.openSymbolCandidates()
        renderCandidatesLayout()
    }

    fun candidateKeySelected(keyEvent: KeyEvent) {
        if (ChewingUtil.candidateWindowClosed()) {
            ChewingBridge.chewing.candClose()
            currentCandidatesList = 0
            candidatesRecyclerView.adapter = null
            EventBus.getDefault().post(Events.UpdateBufferViews())
            switchToMainLayout()
        } else {
            // enter to candidate sublist
            renderCandidatesLayout()
        }
    }

    fun candidateButtonSelected(candidate: Candidate) {
        ChewingBridge.chewing.candChooseByIndex(candidate.index)
        if (ChewingUtil.candidateWindowClosed()) {
            ChewingBridge.chewing.candClose()
            currentCandidatesList = 0
            candidatesRecyclerView.adapter = null
            EventBus.getDefault().post(Events.UpdateBufferViews())
            EventBus.getDefault().post(Events.UpdateCursorPositionToEnd())
            switchToMainLayout()
        } else {
            // enter to candidate sublist
            renderCandidatesLayout()
        }
    }

    // list current offset's candidates in the candidate window
    private fun switchToCandidatesLayout() {
        Log.d(logTag, "switchToCandidatesLayout")

        // reset candidates list to 0 (longest possible phrase) if cursor has been changed
        if (ChewingBridge.chewing.cursorCurrent() != lastChewingCursor) {
            currentCandidatesList = 0
            lastChewingCursor = ChewingBridge.chewing.cursorCurrent()
        }

        // switch to the target candidates list
        repeat(currentCandidatesList) {
            ChewingBridge.chewing.candListNext()
        }

        // circulate candidates list cursor
        if (ChewingBridge.chewing.candListHasNext()) {
            currentCandidatesList += 1
        } else {
            currentCandidatesList = 0
        }

        renderCandidatesLayout()
    }

    enum class CandidateLayoutStyle {
        LIST, GRID
    }

    fun renderCandidatesLayout() {
        Log.d(logTag, "renderCandidatesLayout")
        currentLayout = Layout.CANDIDATES

        this.removeAllViews()
        this.addView(candidatesLayoutBinding.root)

        if (physicalKeyboardPresented) {
            renderCandidatesLayout(CandidateLayoutStyle.LIST)
        } else {
            renderCandidatesLayout(CandidateLayoutStyle.GRID)
        }
        return
    }

    private fun renderCandidatesLayout(candidateLayoutStyle: CandidateLayoutStyle) {
        when (candidateLayoutStyle) {
            CandidateLayoutStyle.LIST -> {
                val layoutManager = FlexboxLayoutManager(context)
                candidatesRecyclerView.adapter = PagedCandidatesAdapter(ChewingBridge.chewing.candCurrentPage())
                candidatesRecyclerView.layoutManager = layoutManager
                return
            }

            CandidateLayoutStyle.GRID -> {
                candidatesRecyclerView.adapter = CandidatesAdapter()
                candidatesRecyclerView.layoutManager = GridLayoutManager(context, 4, LinearLayoutManager.HORIZONTAL, false)
                return
            }
        }
    }

    fun releaseShiftKey() {
        Log.d(logTag, "releaseShiftKey()")
        this.findViewById<ShiftKey>(R.id.keyImageButtonShift)?.switchToState(ShiftKey.ShiftKeyState.RELEASED)
        return
    }

    fun setShapeMode(mode: String) {
        compactLayoutBinding.textViewCurrentWidthModeValue.text = mode
        return
    }
}