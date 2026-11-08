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
    /** A candidate was tapped on screen. */
    data class CandidateButtonSelected(val candidate: Candidate) : ImeAction
    /** libchewing has [text] ready to go into the text field. */
    data class CommitTextInChewingCommitBuffer(val text: String) : ImeAction
    /** An arrow or volume key moved libchewing's cursor; the panels follow. */
    data class DirectionKeyDown(val direction: DirectionKey) : ImeAction
    /**
     * Enter with nothing to commit: do what the text field wants Enter to do.
     * @param lineBreakInMultiLineFields whether a multiple line field gets a line break
     * rather than the action it declares, see [org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent].
     */
    data class EnterKeyDownWhenBufferIsEmpty(val lineBreakInMultiLineFields: Boolean) : ImeAction
    /** The panel changed; whatever sits around it may need to change too. */
    data object KeyboardLayoutSwitched : ImeAction
    /** Back to the main keyboard from the candidates or the symbols. */
    data object ExitKeyboardSubLayouts : ImeAction
    /** The user touched character [offset] of the pre-edit buffer. */
    data class PreEditBufferTouched(val offset: Int) : ImeAction
    /** An on-screen character key went down; [keyCodeString] names the [android.view.KeyEvent] key code. */
    data class PrintingKeyDown(val keyCodeString: String) : ImeAction
    /** Put the keyboard away, if the current panel allows it. */
    data object RequestHideIme : ImeAction
    /** Send a key press straight to the text field, past libchewing. */
    data class SendDownUpKeyEvents(val keycode: Int) : ImeAction
    /** An English suggestion was tapped. */
    data class SuggestionSelected(val word: String) : ImeAction
    /** Show [layout]. */
    data class SwitchToLayout(val layout: Layout) : ImeAction
    /** Hand over to the next input method the user has enabled. */
    data object SwitchToNextInputMethod : ImeAction
    /** Switch the alphanumerical keyboard between half-width and full-width characters. */
    data object ToggleFullOrHalfWidthMode : ImeAction
    /** Switch between Bopomofo and alphanumerical input. */
    data object ToggleKeyboardMainLayoutMode : ImeAction
    /** libchewing's buffers changed: read them back and redraw. */
    data object UpdateBufferViews : ImeAction
    /** libchewing's cursor moved: redraw it. */
    data object UpdateCursorPosition : ImeAction
    /** Move the cursor to the start of the pre-edit buffer. */
    data object UpdateCursorPositionToBegin : ImeAction
    /** Move the cursor to the end of the pre-edit buffer. */
    data object UpdateCursorPositionToEnd : ImeAction
}
