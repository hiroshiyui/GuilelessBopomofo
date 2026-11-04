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
package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout

/**
 * Everything a key, a panel or libchewing can ask the input method to do. Handed to an
 * [ImeDispatcher], which the input method service implements.
 */
sealed interface ImeAction {
    data class CandidateButtonSelected(val candidate: Candidate) : ImeAction
    data class CommitTextInChewingCommitBuffer(val text: String) : ImeAction
    data class DirectionKeyDown(val direction: DirectionKey) : ImeAction
    data object EnterKeyDownWhenBufferIsEmpty : ImeAction
    data object KeyboardLayoutSwitched : ImeAction
    data object ExitKeyboardSubLayouts : ImeAction
    data class PreEditBufferTouched(val offset: Int) : ImeAction
    data class PrintingKeyDown(val keyCodeString: String) : ImeAction
    data object RequestHideIme : ImeAction
    data class SendDownUpKeyEvents(val keycode: Int) : ImeAction
    data class SuggestionSelected(val word: String) : ImeAction
    data class SwitchToLayout(val layout: Layout) : ImeAction
    data object SwitchToNextInputMethod : ImeAction
    data object ToggleFullOrHalfWidthMode : ImeAction
    data object ToggleKeyboardMainLayoutMode : ImeAction
    data object UpdateBufferViews : ImeAction
    data object UpdateCursorPosition : ImeAction
    data object UpdateCursorPositionToBegin : ImeAction
    data object UpdateCursorPositionToEnd : ImeAction
}

/** libchewing was given a fresh context, without the settings the input method put on the old one. */
class ChewingReconnected
