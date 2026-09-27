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
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CandidatesLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.CompactLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDachenCp26LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardDachenLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt26QwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardEt41LayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardHsuQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardQwertyLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.greenrobot.eventbus.EventBus

class KeyboardPanel(
    context: Context, attrs: AttributeSet,
) : RelativeLayout(context, attrs) {
    private val logTag: String = "KeyboardPanel"

    companion object {
        // How many candidates a column of the grid holds.
        private const val CANDIDATE_GRID_ROWS = 4
    }

    internal var lastChewingCursor: Int = 0
    private var currentCandidatesList: Int = 0

    private var compactLayoutBinding: CompactLayoutBinding? = null

    // Which keyboard is on screen at this very moment, so that asking again for the one
    // already shown costs nothing. onStartInputView() fires on every hop between text
    // fields, and tearing the keyboard down only to inflate the same thing again is what
    // makes it visibly reload while filling in, say, the six boxes of a one time password.
    private enum class RenderedLayout { BOPOMOFO, ALPHANUMERICAL, COMPACT }

    private var renderedLayout: RenderedLayout? = null

    // candidatesRecyclerView
    private val candidatesLayoutBinding: CandidatesLayoutBinding by lazy {
        CandidatesLayoutBinding.inflate(LayoutInflater.from(context))
    }
    private val candidatesRecyclerView get() = candidatesLayoutBinding.CandidatesRecyclerView

    var currentLayout: Layout = Layout.MAIN
        set(value) {
            if (field == value) return
            field = value
            // what sits around the panel, the suggestion strip say, depends on it too
            EventBus.getDefault().post(Events.KeyboardLayoutSwitched())
        }

    val sharedPreferences: SharedPreferences = context.appSharedPreferences

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

    /**
     * Throw away what is on screen, so that the next switch inflates it afresh. Call this
     * whenever something the layouts are built from, a preference say, has changed.
     */
    fun invalidateRenderedLayout() {
        renderedLayout = null
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

        // Keep the one already on screen, its mode indicators are refreshed below anyway.
        val binding = compactLayoutBinding.takeIf { renderedLayout == RenderedLayout.COMPACT }
            ?: CompactLayoutBinding.inflate(LayoutInflater.from(context)).also {
                compactLayoutBinding = it
                this.removeAllViews()
                this.addView(it.root)
                renderedLayout = RenderedLayout.COMPACT
            }

        binding.textViewCurrentModeValue.text =
            if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
                resources.getString(R.string.mode_bopomofo)
            } else {
                resources.getString(R.string.mode_alphanumerical)
            }

        binding.textViewCurrentWidthModeValue.text =
            if (ChewingBridge.chewing.getShapeMode() == ShapeMode.FULL.mode) {
                resources.getString(R.string.full_width_mode)
            } else {
                resources.getString(R.string.half_width_mode)
            }
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
        // different preference, so say it again even when nothing is inflated below.
        userSoftKeyboardLayoutPreference?.let {
            val newSoftKeyboardType = ChewingBridge.chewing.convKBStr2Num(it)
            ChewingBridge.chewing.setKBType(newSoftKeyboardType)
        }

        if (renderedLayout == RenderedLayout.BOPOMOFO) return

        this.removeAllViews()
        renderedLayout = RenderedLayout.BOPOMOFO
        this.addView(inflateBopomofoKeyboard(userSoftKeyboardLayoutPreference))
    }

    /**
     * The on-screen keyboard the given layout preference asks for. Anything unheard of, a
     * preference left over from an older release say, gets the default Dachen keyboard
     * rather than no keyboard at all.
     */
    private fun inflateBopomofoKeyboard(softKeyboardLayout: String?): View {
        val inflater = LayoutInflater.from(context)
        return when (softKeyboardLayout) {
            BopomofoSoftKeyboards.KB_HSU.layout ->
                if (sharedPreferences.getBoolean(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)) {
                    KeyboardHsuQwertyLayoutBinding.inflate(inflater).root
                } else {
                    KeyboardHsuLayoutBinding.inflate(inflater).root
                }

            BopomofoSoftKeyboards.KB_ET26.layout ->
                if (sharedPreferences.getBoolean(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)) {
                    KeyboardEt26QwertyLayoutBinding.inflate(inflater).root
                } else {
                    KeyboardEt26LayoutBinding.inflate(inflater).root
                }

            BopomofoSoftKeyboards.KB_ET.layout -> KeyboardEt41LayoutBinding.inflate(inflater).root

            BopomofoSoftKeyboards.KB_DACHEN_CP26.layout ->
                KeyboardDachenCp26LayoutBinding.inflate(inflater).root

            else -> KeyboardDachenLayoutBinding.inflate(inflater).root
        }
    }

    private fun switchToAlphanumericalLayout() {
        Log.d(logTag, "switchToQwertyLayout")

        if (physicalKeyboardPresented) {
            switchToCompactLayout()
            return
        }

        currentLayout = Layout.QWERTY

        if (renderedLayout == RenderedLayout.ALPHANUMERICAL) return

        this.removeAllViews()
        renderedLayout = RenderedLayout.ALPHANUMERICAL
        this.addView(KeyboardQwertyLayoutBinding.inflate(LayoutInflater.from(context)).root)
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
        candidatesRecyclerView.adapter = null
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

    enum class CandidateLayoutStyle {
        LIST, GRID
    }

    fun renderCandidatesLayout() {
        Log.d(logTag, "renderCandidatesLayout")
        currentLayout = Layout.CANDIDATES

        this.removeAllViews()
        // The keyboard is gone from the screen now, whatever it was.
        renderedLayout = null
        this.addView(candidatesLayoutBinding.root)

        renderCandidatesLayout(
            if (physicalKeyboardPresented) CandidateLayoutStyle.LIST else CandidateLayoutStyle.GRID
        )
    }

    private fun renderCandidatesLayout(candidateLayoutStyle: CandidateLayoutStyle) {
        when (candidateLayoutStyle) {
            CandidateLayoutStyle.LIST -> {
                candidatesRecyclerView.adapter = PagedCandidatesAdapter(ChewingBridge.chewing.candCurrentPage())
                candidatesRecyclerView.layoutManager = FlexboxLayoutManager(context)
            }

            CandidateLayoutStyle.GRID -> {
                candidatesRecyclerView.adapter = CandidatesAdapter()
                candidatesRecyclerView.layoutManager = GridLayoutManager(
                    context, CANDIDATE_GRID_ROWS, LinearLayoutManager.HORIZONTAL, false
                )
            }
        }
    }

    fun releaseShiftKey() {
        Log.d(logTag, "releaseShiftKey()")
        this.findViewById<ShiftKey>(R.id.keyImageButtonShift)?.switchToState(ShiftKey.ShiftKeyState.RELEASED)
    }

    fun setShapeMode(mode: String) {
        compactLayoutBinding?.textViewCurrentWidthModeValue?.text = mode
    }
}