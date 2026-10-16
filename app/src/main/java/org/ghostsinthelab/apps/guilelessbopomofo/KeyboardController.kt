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

import android.content.SharedPreferences
import android.util.Log
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState.CandidateLayoutStyle
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens
import org.greenrobot.eventbus.EventBus

/**
 * Decides which panel the input view shows (a keyboard, the candidates, the compact bar) and
 * keeps libchewing in step with it. It only writes [uiState]; drawing is left to Compose.
 */
class KeyboardController(
    private val uiState: ImeUiState,
    private val sharedPreferences: SharedPreferences,
) {
    private val logTag: String = "KeyboardController"

    internal var lastChewingCursor: Int = 0
    private var currentCandidatesList: Int = 0

    var currentLayout: Layout
        get() = uiState.layout
        private set(value) {
            if (uiState.layout == value) return
            uiState.layout = value
            // what sits around the panel, the suggestion strip say, depends on it too
            EventBus.getDefault().post(Events.KeyboardLayoutSwitched())
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
            Layout.MAIN -> switchToMainLayout()
            Layout.CANDIDATES -> switchToCandidatesLayout()
            Layout.SYMBOLS -> switchToSymbolPicker()
            Layout.COMPACT -> switchToCompactLayout()
            // QWERTY is never asked for by name: switchToMainLayout() picks it from the
            // current Chinese / alphanumerical mode.
            Layout.QWERTY -> switchToMainLayout()
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

        // get user preferred physical Bopomofo keyboard layout
        val userPhysicalKeyboardLayoutPreference = sharedPreferences.getString(
            USER_PHYSICAL_KEYBOARD_LAYOUT, BopomofoPhysicalKeyboards.KB_DEFAULT.layout
        )

        userPhysicalKeyboardLayoutPreference?.let {
            val newPhysicalKeyboardType = ChewingBridge.chewing.convKBStr2Num(it)
            ChewingBridge.chewing.setKBType(newPhysicalKeyboardType)
        }

        refreshCompactIndicators()
    }

    /** Brings the mode indicators of the compact layout up to date with libchewing. */
    fun refreshCompactIndicators() {
        uiState.compactChineseMode = ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode
        uiState.compactFullWidth = ChewingBridge.chewing.getShapeMode() == ShapeMode.FULL.mode
    }

    private fun switchToBopomofoLayout() {
        Log.d(logTag, "switchToBopomofoLayout()")

        // Toggle to compact layout when physical keyboard is enabled:
        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        currentLayout = Layout.MAIN

        // 不同注音排列螢幕鍵盤的抽換 support different on-screen Bopomofo keyboard layouts
        val userSoftKeyboardLayoutPreference = sharedPreferences.getString(
            USER_SOFT_KEYBOARD_LAYOUT, BopomofoSoftKeyboards.KB_DEFAULT.layout
        )

        // Chewing keeps its own notion of the layout, and the compact one sets it from a
        // different preference, so say it again on every switch.
        val chewingLayout = BopomofoSoftKeyboards.fromLayout(userSoftKeyboardLayoutPreference).chewingLayout
        ChewingBridge.chewing.setKBType(ChewingBridge.chewing.convKBStr2Num(chewingLayout))

        uiState.softKeyboard = softKeyboardFor(userSoftKeyboardLayoutPreference)
        applyKeyPreferences()
    }

    /**
     * The on-screen keyboard the given layout preference asks for. Anything unheard of, a
     * preference left over from an older release say, gets the default Dachen keyboard
     * rather than no keyboard at all.
     */
    private fun softKeyboardFor(softKeyboardLayout: String?): SoftKeyboard = when (softKeyboardLayout) {
        BopomofoSoftKeyboards.KB_HSU.layout ->
            if (sharedPreferences.getBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)) {
                SoftKeyboard.HSU_QWERTY
            } else {
                SoftKeyboard.HSU
            }

        BopomofoSoftKeyboards.KB_ET26.layout ->
            if (sharedPreferences.getBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)) {
                SoftKeyboard.ET26_QWERTY
            } else {
                SoftKeyboard.ET26
            }

        BopomofoSoftKeyboards.KB_ET.layout -> SoftKeyboard.ET41
        BopomofoSoftKeyboards.KB_DACHEN_CP26.layout -> SoftKeyboard.DACHEN_CP26
        BopomofoSoftKeyboards.KB_DACHEN_COMPACT.layout -> SoftKeyboard.DACHEN_COMPACT
        else -> SoftKeyboard.DACHEN
    }

    private fun applyKeyPreferences() {
        uiState.keyHeightDp = sharedPreferences.getInt(
            USER_KEY_BUTTON_HEIGHT, GuilelessDimens.KeyMinHeightDefault.value.toInt()
        )
        uiState.imeSwitchEnabled = sharedPreferences.getBoolean(USER_ENABLE_IME_SWITCH, false)
    }

    private fun switchToAlphanumericalLayout() {
        Log.d(logTag, "switchToQwertyLayout")

        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        currentLayout = Layout.QWERTY
        applyKeyPreferences()
    }

    private fun switchToSymbolPicker() {
        currentLayout = Layout.SYMBOLS
        ChewingUtil.openSymbolCandidates()
        renderCandidatesLayout()
    }

    /** A selection key was pressed while the candidates were on screen. */
    fun candidateKeySelected() {
        if (ChewingUtil.candidateWindowClosed()) {
            leaveCandidatesLayout(moveCursorToEnd = false)
        } else {
            // enter to candidate sublist
            renderCandidatesLayout()
        }
    }

    fun candidateButtonSelected(candidate: Candidate) {
        ChewingBridge.chewing.candChooseByIndex(candidate.index)
        if (ChewingUtil.candidateWindowClosed()) {
            leaveCandidatesLayout(moveCursorToEnd = true)
        } else {
            // enter to candidate sublist
            renderCandidatesLayout()
        }
    }

    /**
     * Nothing left to choose from: put the candidates away and show the keyboard again.
     */
    private fun leaveCandidatesLayout(moveCursorToEnd: Boolean) {
        ChewingBridge.chewing.candClose()
        currentCandidatesList = 0
        uiState.candidates = emptyList()
        EventBus.getDefault().post(Events.UpdateBufferViews())
        if (moveCursorToEnd) {
            EventBus.getDefault().post(Events.UpdateCursorPositionToEnd())
        }
        switchToMainLayout()
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

    fun renderCandidatesLayout() {
        Log.d(logTag, "renderCandidatesLayout")
        currentLayout = Layout.CANDIDATES

        if (physicalKeyboardPresented) {
            uiState.candidateLayoutStyle = CandidateLayoutStyle.LIST
            uiState.candidates = ChewingUtil.getCandidatesByPage(ChewingBridge.chewing.candCurrentPage())
        } else {
            uiState.candidateLayoutStyle = CandidateLayoutStyle.GRID
            uiState.candidates = List(ChewingBridge.chewing.candTotalChoice()) { index ->
                Candidate(index, ChewingBridge.chewing.candStringByIndexStatic(index))
            }
        }
        uiState.candidatesGeneration += 1
    }

    fun releaseShiftKey() {
        Log.d(logTag, "releaseShiftKey()")
        // Only the alphanumerical keyboard has a Shift key to release.
        if (uiState.shiftKeyState != ShiftKey.ShiftKeyState.RELEASED) {
            ShiftKey.switchToState(uiState, ShiftKey.ShiftKeyState.RELEASED)
        }
    }
}
