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
package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingTestSetup
import org.ghostsinthelab.apps.guilelessbopomofo.ChiEngMode
import org.ghostsinthelab.apps.guilelessbopomofo.RecordingImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.BackToMainFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.BackspaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CandidateButton
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.EnterKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ImeSwitchFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ModeSwitchFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.PunctuationFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SpaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SuggestionButton
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SymbolFunctionKey
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Every on-screen key asks the input method for the right thing. */
@RunWith(AndroidJUnit4::class)
class VirtualKeysInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val host = RecordingImeHost(appContext)

    @Before
    fun setUp() {
        ChewingTestSetup.connect(appContext)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
    }

    @After
    fun tearDown() {
        ChewingBridge.chewing.delete()
    }

    private fun onMain(block: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(block)

    @Test
    fun characterKey_typesOnTheWayDown() {
        onMain { CharacterKey("KEYCODE_J").onDown(host) }
        assertEquals(listOf<ImeAction>(ImeAction.PrintingKeyDown("KEYCODE_J")), host.actions)
    }

    @Test
    fun candidateButton_offersItsCandidate() {
        val candidate = Candidate(2, "測")
        onMain { CandidateButton(candidate).onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.CandidateButtonSelected(candidate)), host.actions)
    }

    @Test
    fun suggestionButton_offersItsWord_butNotAnEmptyOne() {
        onMain {
            SuggestionButton("hello").onTap(host)
            SuggestionButton("").onTap(host)
        }
        assertEquals(listOf<ImeAction>(ImeAction.SuggestionSelected("hello")), host.actions)
    }

    @Test
    fun modeSwitch_togglesTheMainLayoutMode() {
        onMain { ModeSwitchFunctionKey.onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.ToggleKeyboardMainLayoutMode), host.actions)
    }

    @Test
    fun symbolKey_opensTheSymbols() {
        onMain { SymbolFunctionKey.onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.SwitchToLayout(Layout.SYMBOLS)), host.actions)
    }

    @Test
    fun backToMain_leavesTheSubLayout_withTheCursorAtTheEnd() {
        onMain { BackToMainFunctionKey.onTap(host) }
        assertEquals(
            listOf<ImeAction>(ImeAction.ExitKeyboardSubLayouts, ImeAction.UpdateCursorPositionToEnd),
            host.actions,
        )
    }

    @Test
    fun imeSwitch_switchesToTheNextInputMethod() {
        onMain { ImeSwitchFunctionKey.onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.SwitchToNextInputMethod), host.actions)
    }

    @Test
    fun punctuation_typesTheMarkAndShowsTheBuffers() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        onMain { PunctuationFunctionKey.onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.UpdateBufferViews), host.actions)
        // in English the comma goes straight to the commit buffer
        assertEquals(1, ChewingBridge.chewing.commitCheck())
        assertEquals(",", ChewingBridge.chewing.commitString())
    }

    @Test
    fun withEmptyBuffers_backspaceSpaceAndEnter_goToTheTextField() {
        onMain {
            BackspaceKey.onTap(host)
            SpaceKey.onTap(host)
            EnterKey.onTap(host)
        }
        assertEquals(
            listOf<ImeAction>(
                ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_DEL),
                ImeAction.SendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE),
                ImeAction.EnterKeyDownWhenBufferIsEmpty,
            ),
            host.actions,
        )
    }

    @Test
    fun withSomethingInTheBuffer_backspaceEditsItInstead() {
        ChewingBridge.chewing.handleDefault('j')
        onMain { BackspaceKey.onTap(host) }
        assertEquals(listOf<ImeAction>(ImeAction.UpdateBufferViews), host.actions)
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
    }
}
