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
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.buffers.PreEditBuffer
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * When libchewing fails to start, the service sets the context to 0 and the keyboard has to
 * keep going without it: every getter Kotlin declares non-null must still return a value.
 */
@RunWith(AndroidJUnit4::class)
class ChewingWithoutContextInstrumentedTest {
    private var savedContext: Long = 0

    @Before
    fun setUp() {
        savedContext = ChewingBridge.chewing.context
        ChewingBridge.chewing.context = 0
    }

    @After
    fun tearDown() {
        ChewingBridge.chewing.context = savedContext
    }

    @Test
    fun stringGetters_returnEmptyStrings() {
        with(ChewingBridge.chewing) {
            assertEquals("", bufferStringStatic())
            assertEquals("", bopomofoStringStatic())
            assertEquals("", bufferString())
            assertEquals("", bopomofoString())
            assertEquals("", commitString())
            assertEquals("", commitStringStatic())
            assertEquals("", candString())
            assertEquals("", candStringStatic())
            assertEquals("", candStringByIndexStatic(0))
            assertEquals("", getKBString())
            assertEquals("", configGetStr("chewing.keyboard_type"))
        }
    }

    @Test
    fun selectionKeys_areAnEmptyArray() {
        assertEquals(0, ChewingBridge.chewing.getSelKey().size)
    }

    @Test
    fun updatingTheBuffers_doesNotCrash() {
        val uiState = ImeUiState()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            PreEditBuffer(uiState).update()
            uiState.bopomofoText = ChewingBridge.chewing.bopomofoStringStatic()
        }
        assertEquals("", uiState.preEditText)
        assertEquals("", uiState.bopomofoText)
        assertTrue(ChewingUtil.anyBufferIsNotEmpty().not())
    }
}
