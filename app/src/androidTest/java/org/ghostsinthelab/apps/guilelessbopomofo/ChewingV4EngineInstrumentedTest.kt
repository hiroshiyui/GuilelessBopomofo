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

package org.ghostsinthelab.apps.guilelessbopomofo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.HISTORY_DICT
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.SHIFT_PUNCTUATION
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** How libchewing 0.14 behaves with the options this app gives it, typed through the JNI bridge. */
@RunWith(AndroidJUnit4::class)
class ChewingV4EngineInstrumentedTest {
    private lateinit var data: IsolatedChewingData

    @Before
    fun setUp() {
        data = IsolatedChewingData()
        data.installAndConnect()
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
    }

    @After
    fun tearDown() {
        data.close()
    }

    private fun type(keys: String) = keys.forEach { ChewingBridge.chewing.handleDefault(it) }

    private fun candidatesShown(): Boolean = ChewingUtil.candidateWindowOpened()

    // region Space cycling

    @Test
    fun withoutCycling_spaceOpensTheCandidatesAtOnce() {
        ChewingBridge.chewing.setSpaceAsSelection(1)
        type(LYU_CHA)
        ChewingBridge.chewing.handleSpace()
        assertTrue(candidatesShown())
    }

    @Test
    fun withCycling_spaceSwapsCandidatesInPlace_thenOpensTheList() {
        ChewingBridge.chewing.setSpaceAsSelection(1)
        ChewingBridge.chewing.configSetInt("chewing.cycle_first_n_selects", CYCLES)
        type(LYU_CHA)
        val typed = ChewingBridge.chewing.bufferString()

        ChewingBridge.chewing.handleSpace()
        assertFalse("the first Space swaps in place", candidatesShown())
        assertNotEquals(typed, ChewingBridge.chewing.bufferString())
        assertEquals(typed.length, ChewingBridge.chewing.bufferString().length)

        repeat(CYCLES - 1) {
            ChewingBridge.chewing.handleSpace()
            assertFalse("Space ${it + 2} still swaps in place", candidatesShown())
        }

        ChewingBridge.chewing.handleSpace()
        assertTrue("past the cycle, Space opens the list", candidatesShown())
    }

    @Test
    fun cyclingNeedsSpaceAsTheSelectionKey() {
        ChewingBridge.chewing.setSpaceAsSelection(0)
        ChewingBridge.chewing.configSetInt("chewing.cycle_first_n_selects", CYCLES)
        type(LYU_CHA)
        val typed = ChewingBridge.chewing.bufferString()
        ChewingBridge.chewing.handleSpace()
        assertFalse(candidatesShown())
        // Space is typed into the buffer instead.
        assertTrue(ChewingBridge.chewing.bufferString().startsWith(typed))
    }

    // endregion

    // region learning

    // Types 綠茶, commits it, and lets libchewing write out what it learned.
    private fun typeAndCommitThenDisconnect() {
        type(LYU_CHA)
        ChewingBridge.chewing.handleEnter()
        ChewingBridge.chewing.delete()
    }

    private fun history(): ByteArray = data.file(HISTORY_DICT).readBytes()

    @Test
    fun committedText_goesIntoTheTypingHistory() {
        ChewingBridge.chewing.delete()
        val untouched = history()

        ChewingBridge.chewing.connect(data.path)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        typeAndCommitThenDisconnect()
        assertFalse(untouched.contentEquals(history()))
    }

    @Test
    fun withLearningDisabled_committedTextLeavesNoHistory() {
        ChewingBridge.chewing.delete()
        val untouched = history()

        ChewingBridge.chewing.connect(data.path)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.configSetInt("chewing.disable_auto_learn_phrase", 1)
        typeAndCommitThenDisconnect()
        assertArrayEquals(untouched, history())
    }

    @Test
    fun withHistoryLearningOff_committedTextLeavesNoHistory() {
        ChewingBridge.chewing.delete()
        val untouched = history()

        ChewingBridge.chewing.connect(data.path)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.configSetInt("chewing.auto_learn_user_history", 0)
        typeAndCommitThenDisconnect()
        assertArrayEquals(untouched, history())
    }

    // endregion

    // region punctuation

    @Test
    fun theShiftPunctuationTableInSettings_matchesLibchewing() {
        for ((key, symbol, output) in SHIFT_PUNCTUATION) {
            // The app hands libchewing the shifted character, as a physical keyboard types it.
            ChewingBridge.chewing.handleDefault(symbol.single())
            assertEquals("Shift + $key ($symbol)", output, ChewingBridge.chewing.bufferString())
            ChewingBridge.chewing.cleanPreeditBuf()
        }
    }

    // endregion

    private companion object {
        // ㄌㄩˋ ㄔㄚˊ, 綠茶 on the default (Dachen) layout.
        const val LYU_CHA = "xm4t86"
        const val CYCLES = 2
    }
}
