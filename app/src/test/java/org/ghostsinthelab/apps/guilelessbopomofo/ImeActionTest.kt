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

import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ImeActionTest {

    // --- Events with data fields ---

    @Test
    fun candidateButtonSelected_holdsCandidate() {
        val candidate = Candidate(0, "test", 'a')
        val event = ImeAction.CandidateButtonSelected(candidate)
        assertEquals(candidate, event.candidate)
        assertEquals(0, event.candidate.index)
        assertEquals("test", event.candidate.candidateString)
        assertEquals('a', event.candidate.selectionKey)
    }

    @Test
    fun directionKeyDown_holdsRight() {
        val event = ImeAction.DirectionKeyDown(DirectionKey.RIGHT)
        assertEquals(DirectionKey.RIGHT, event.direction)
    }

    @Test
    fun directionKeyDown_holdsLeft() {
        val event = ImeAction.DirectionKeyDown(DirectionKey.LEFT)
        assertEquals(DirectionKey.LEFT, event.direction)
    }

    @Test
    fun printingKeyDown_holdsKeyCodeString() {
        val event = ImeAction.PrintingKeyDown("KEYCODE_A")
        assertEquals("KEYCODE_A", event.keyCodeString)
    }

    @Test
    fun sendDownUpKeyEvents_holdsKeycode() {
        val event = ImeAction.SendDownUpKeyEvents(42)
        assertEquals(42, event.keycode)
    }

    @Test
    fun sendDownUpKeyEvents_holdsZeroKeycode() {
        val event = ImeAction.SendDownUpKeyEvents(0)
        assertEquals(0, event.keycode)
    }

    @Test
    fun suggestionSelected_holdsWord() {
        val event = ImeAction.SuggestionSelected("hello")
        assertEquals("hello", event.word)
    }

    @Test
    fun switchToLayout_holdsLayout() {
        for (layout in Layout.values()) {
            val event = ImeAction.SwitchToLayout(layout)
            assertEquals(layout, event.layout)
        }
    }

    // --- Events with no fields ---

    @Test
    fun commitTextInChewingCommitBuffer_holdsText() {
        val event = ImeAction.CommitTextInChewingCommitBuffer("測試")
        assertEquals("測試", event.text)
    }

    @Test
    fun enterKeyDownWhenBufferIsEmpty_canBeInstantiated() {
        val event = ImeAction.EnterKeyDownWhenBufferIsEmpty(lineBreakInMultiLineFields = true)
        assertNotNull(event)
    }

    @Test
    fun keyboardLayoutSwitched_canBeInstantiated() {
        val event = ImeAction.KeyboardLayoutSwitched
        assertNotNull(event)
    }

    @Test
    fun exitKeyboardSubLayouts_canBeInstantiated() {
        val event = ImeAction.ExitKeyboardSubLayouts
        assertNotNull(event)
    }

    @Test
    fun requestHideIme_canBeInstantiated() {
        val event = ImeAction.RequestHideIme
        assertNotNull(event)
    }

    @Test
    fun switchToNextInputMethod_canBeInstantiated() {
        val event = ImeAction.SwitchToNextInputMethod
        assertNotNull(event)
    }

    @Test
    fun toggleFullOrHalfWidthMode_canBeInstantiated() {
        val event = ImeAction.ToggleFullOrHalfWidthMode
        assertNotNull(event)
    }

    @Test
    fun toggleKeyboardMainLayoutMode_canBeInstantiated() {
        val event = ImeAction.ToggleKeyboardMainLayoutMode
        assertNotNull(event)
    }

    @Test
    fun updateBufferViews_canBeInstantiated() {
        val event = ImeAction.UpdateBufferViews
        assertNotNull(event)
    }

    @Test
    fun updateCursorPosition_canBeInstantiated() {
        val event = ImeAction.UpdateCursorPosition
        assertNotNull(event)
    }

    @Test
    fun updateCursorPositionToBegin_canBeInstantiated() {
        val event = ImeAction.UpdateCursorPositionToBegin
        assertNotNull(event)
    }

    @Test
    fun updateCursorPositionToEnd_canBeInstantiated() {
        val event = ImeAction.UpdateCursorPositionToEnd
        assertNotNull(event)
    }

    // --- Value semantics ---

    @Test
    fun noFieldActions_areOneObject() {
        assertSame(ImeAction.UpdateBufferViews, ImeAction.UpdateBufferViews)
    }

    @Test
    fun fieldActions_withTheSameFieldsAreEqual() {
        assertEquals(ImeAction.SendDownUpKeyEvents(1), ImeAction.SendDownUpKeyEvents(1))
        assertNotEquals(ImeAction.SendDownUpKeyEvents(1), ImeAction.SendDownUpKeyEvents(2))
    }

    @Test
    fun candidateButtonSelected_comparesByCandidate() {
        val candidate = Candidate(0, "test")
        assertEquals(ImeAction.CandidateButtonSelected(candidate), ImeAction.CandidateButtonSelected(candidate))
    }
}
