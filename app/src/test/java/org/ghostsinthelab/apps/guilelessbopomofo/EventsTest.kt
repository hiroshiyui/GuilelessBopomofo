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
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class EventsTest {

    // --- Events with data fields ---

    @Test
    fun candidateButtonSelected_holdsCandidate() {
        val candidate = Candidate(0, "test", 'a')
        val event = Events.CandidateButtonSelected(candidate)
        assertEquals(candidate, event.candidate)
        assertEquals(0, event.candidate.index)
        assertEquals("test", event.candidate.candidateString)
        assertEquals('a', event.candidate.selectionKey)
    }

    @Test
    fun directionKeyDown_holdsRight() {
        val event = Events.DirectionKeyDown(DirectionKey.RIGHT)
        assertEquals(DirectionKey.RIGHT, event.direction)
    }

    @Test
    fun directionKeyDown_holdsLeft() {
        val event = Events.DirectionKeyDown(DirectionKey.LEFT)
        assertEquals(DirectionKey.LEFT, event.direction)
    }

    @Test
    fun printingKeyDown_holdsCharacterKey() {
        val mockKey = mock(CharacterKey::class.java)
        val event = Events.PrintingKeyDown(mockKey)
        assertEquals(mockKey, event.characterKey)
    }

    @Test
    fun sendDownUpKeyEvents_holdsKeycode() {
        val event = Events.SendDownUpKeyEvents(42)
        assertEquals(42, event.keycode)
    }

    @Test
    fun sendDownUpKeyEvents_holdsZeroKeycode() {
        val event = Events.SendDownUpKeyEvents(0)
        assertEquals(0, event.keycode)
    }

    @Test
    fun switchToLayout_holdsLayout() {
        for (layout in Layout.values()) {
            val event = Events.SwitchToLayout(layout)
            assertEquals(layout, event.layout)
        }
    }

    @Test
    fun updateShiftKeyState_holdsActiveAndLocked() {
        val event = Events.UpdateShiftKeyState(isActive = true, isLocked = false)
        assertTrue(event.isActive)
        assertFalse(event.isLocked)
    }

    @Test
    fun updateShiftKeyState_bothTrue() {
        val event = Events.UpdateShiftKeyState(isActive = true, isLocked = true)
        assertTrue(event.isActive)
        assertTrue(event.isLocked)
    }

    @Test
    fun updateShiftKeyState_bothFalse() {
        val event = Events.UpdateShiftKeyState(isActive = false, isLocked = false)
        assertFalse(event.isActive)
        assertFalse(event.isLocked)
    }

    // --- Events with no fields ---

    @Test
    fun commitTextInChewingCommitBuffer_canBeInstantiated() {
        val event = Events.CommitTextInChewingCommitBuffer()
        assertNotNull(event)
    }

    @Test
    fun enterKeyDownWhenBufferIsEmpty_canBeInstantiated() {
        val event = Events.EnterKeyDownWhenBufferIsEmpty()
        assertNotNull(event)
    }

    @Test
    fun exitKeyboardSubLayouts_canBeInstantiated() {
        val event = Events.ExitKeyboardSubLayouts()
        assertNotNull(event)
    }

    @Test
    fun requestHideIme_canBeInstantiated() {
        val event = Events.RequestHideIme()
        assertNotNull(event)
    }

    @Test
    fun switchToNextInputMethod_canBeInstantiated() {
        val event = Events.SwitchToNextInputMethod()
        assertNotNull(event)
    }

    @Test
    fun toggleFullOrHalfWidthMode_canBeInstantiated() {
        val event = Events.ToggleFullOrHalfWidthMode()
        assertNotNull(event)
    }

    @Test
    fun toggleKeyboardMainLayoutMode_canBeInstantiated() {
        val event = Events.ToggleKeyboardMainLayoutMode()
        assertNotNull(event)
    }

    @Test
    fun updateBufferViews_canBeInstantiated() {
        val event = Events.UpdateBufferViews()
        assertNotNull(event)
    }

    @Test
    fun updateCursorPosition_canBeInstantiated() {
        val event = Events.UpdateCursorPosition()
        assertNotNull(event)
    }

    @Test
    fun updateCursorPositionToBegin_canBeInstantiated() {
        val event = Events.UpdateCursorPositionToBegin()
        assertNotNull(event)
    }

    @Test
    fun updateCursorPositionToEnd_canBeInstantiated() {
        val event = Events.UpdateCursorPositionToEnd()
        assertNotNull(event)
    }

    // --- Instance identity (plain classes, not data classes) ---

    @Test
    fun noFieldEvents_eachInstantiationCreatesDistinctObject() {
        val a = Events.UpdateBufferViews()
        val b = Events.UpdateBufferViews()
        assertNotSame(a, b)
    }

    @Test
    fun fieldEvents_eachInstantiationCreatesDistinctObject() {
        val a = Events.SendDownUpKeyEvents(1)
        val b = Events.SendDownUpKeyEvents(1)
        assertNotSame(a, b)
    }

    @Test
    fun candidateButtonSelected_distinctInstancesAreNotEqual() {
        val candidate = Candidate(0, "test")
        val a = Events.CandidateButtonSelected(candidate)
        val b = Events.CandidateButtonSelected(candidate)
        // Plain classes use reference equality by default
        assertNotSame(a, b)
    }
}
