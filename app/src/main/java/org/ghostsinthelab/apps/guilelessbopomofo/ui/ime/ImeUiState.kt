/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens

/**
 * Everything the input view shows, in one place. The service and [KeyboardController] write
 * it, on the main thread; [ImeRoot] only reads it, so the view can be thrown away and built
 * again at any time without losing anything.
 */
class ImeUiState {
    enum class CandidateLayoutStyle { LIST, GRID }

    /** Which panel is showing below the buffers. */
    var layout: Layout by mutableStateOf(Layout.MAIN)

    /** The Bopomofo keyboard [Layout.MAIN] shows. */
    var softKeyboard: SoftKeyboard by mutableStateOf(SoftKeyboard.DACHEN)

    // The user's key preferences, read again whenever the main keyboard is switched to.
    var keyHeightDp: Int by mutableIntStateOf(GuilelessDimens.KeyMinHeightDefault.value.toInt())
    var imeSwitchEnabled: Boolean by mutableStateOf(false)

    var shiftKeyState: ShiftKey.ShiftKeyState by mutableStateOf(ShiftKey.ShiftKeyState.RELEASED)

    var preEditText: String by mutableStateOf("")

    /** The character of [preEditText] the cursor is under, or -1 for none. */
    var preEditCursorIndex: Int by mutableIntStateOf(-1)

    /** Widens the end of the pre-edit buffer, so one can see the cursor sits past its last character. */
    var preEditCursorAtEnd: Boolean by mutableStateOf(false)

    var bopomofoText: String by mutableStateOf("")

    /** The buffers and the suggestion strip take turns above the keyboard. */
    var showBuffers: Boolean by mutableStateOf(true)
    var suggestions: List<String> by mutableStateOf(emptyList())

    var candidates: List<Candidate> by mutableStateOf(emptyList())
    var candidateLayoutStyle: CandidateLayoutStyle by mutableStateOf(CandidateLayoutStyle.GRID)

    /** Bumped on every render of the candidates, so that a fresh list starts from its beginning. */
    var candidatesGeneration: Int by mutableIntStateOf(0)

    // What the compact layout (a physical keyboard is in use) reports.
    var compactChineseMode: Boolean by mutableStateOf(true)
    var compactFullWidth: Boolean by mutableStateOf(false)
}
