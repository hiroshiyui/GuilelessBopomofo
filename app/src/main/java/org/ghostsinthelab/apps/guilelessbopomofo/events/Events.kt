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
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey

class Events {
    class CandidateButtonSelected(val candidate: Candidate)
    class CommitTextInChewingCommitBuffer
    class DirectionKeyDown(val direction: DirectionKey)
    class EnterKeyDownWhenBufferIsEmpty
    class ExitKeyboardSubLayouts
    class PrintingKeyDown(val characterKey: CharacterKey)
    class RequestHideIme
    class SendDownUpKeyEvents(val keycode: Int)
    class SwitchToLayout(val layout: Layout)
    class SwitchToNextInputMethod
    class ToggleFullOrHalfWidthMode
    class ToggleKeyboardMainLayoutMode
    class UpdateBufferViews
    class UpdateCursorPosition
    class UpdateCursorPositionToBegin
    class UpdateCursorPositionToEnd
    class UpdateShiftKeyState(val isActive: Boolean, val isLocked: Boolean)
}