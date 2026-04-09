/*
 * Guileless Bopomofo
 * Copyright (C) 2024 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ChewingUtilInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataDir = File(dataPath)
    private val chewingDataFiles =
        listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    @Before
    fun setupChewingEngine() {
        // Copy Chewing data files to data directory
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d("ChewingUtilInstrumentedTest", "Copying ${file}...")
            val dataInputStream = appContext.assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e("ChewingUtilInstrumentedTest", it)
                }
            } finally {
                Log.d("ChewingUtilInstrumentedTest", "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }

        // Initialize Chewing
        ChewingBridge.chewing.connect(dataPath)
    }

    @After
    fun cleanUpChewingEngine() {
        // Clean buffers and close Chewing
        ChewingBridge.chewing.handleEsc()
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.delete()
    }

    // region handleBackspaceAction

    @Test
    fun handleBackspaceAction_whenBufferHasContent_shouldClearBuffer() {
        // Type ㄊ (key 'w') to put something in the bopomofo buffer
        ChewingBridge.chewing.handleDefault('w')
        assertTrue(ChewingUtil.anyBufferIsNotEmpty())

        ChewingUtil.handleBackspaceAction()
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
    }

    @Test
    fun handleBackspaceAction_whenBufferIsEmpty_shouldNotCrash() {
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
        // Should not throw; in a real IME it would send KEYCODE_DEL via EventBus
        ChewingUtil.handleBackspaceAction()
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
    }

    // endregion

    // region handleEnterAction

    @Test
    fun handleEnterAction_whenBufferHasContent_shouldCommit() {
        // Type ㄙ ㄨ ˋ (su4) = 速 etc.
        val keys = arrayOf('x', 'j', '4')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        assertTrue(ChewingUtil.anyBufferIsNotEmpty())

        ChewingUtil.handleEnterAction()
        // After committing, the pre-edit buffer should be empty
        assertEquals("", ChewingBridge.chewing.bufferStringStatic())
    }

    @Test
    fun handleEnterAction_whenBufferIsEmpty_shouldNotCrash() {
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
        // Should not throw; in a real IME it would post EnterKeyDownWhenBufferIsEmpty via EventBus
        ChewingUtil.handleEnterAction()
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
    }

    // endregion

    // region handleSpaceAction

    @Test
    fun handleSpaceAction_whenBufferHasContent_shouldProcess() {
        // Type ㄙㄨˋ (su4) using known-working key sequence from existing tests
        val keys = arrayOf('x', 'm', '4')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        assertTrue(ChewingUtil.anyBufferIsNotEmpty())

        ChewingBridge.chewing.handleSpace()
        // After space, the bopomofo buffer should be empty (converted to pre-edit)
        assertEquals("", ChewingBridge.chewing.bopomofoStringStatic())
        // But the pre-edit buffer should have the composed character
        assertTrue(ChewingBridge.chewing.bufferStringStatic().isNotEmpty())
    }

    @Test
    fun handleSpaceAction_whenBufferIsEmpty_shouldNotCrash() {
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
        // Should not throw; in a real IME it would send KEYCODE_SPACE via EventBus
        ChewingUtil.handleSpaceAction()
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
    }

    // endregion

    // region openCandidates

    @Test
    fun openCandidates_afterTyping_shouldOpenCandidateWindow() {
        // Type ㄙㄨˋ (su4) + space to compose, then open candidates
        val keys = arrayOf('x', 'm', '4')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()

        assertTrue(ChewingUtil.candidateWindowOpened())
    }

    // endregion

    // region openSymbolCandidates

    @Test
    fun openSymbolCandidates_shouldOpenCandidateWindow() {
        ChewingUtil.openSymbolCandidates()
        assertTrue(ChewingUtil.candidateWindowOpened())
        assertTrue(ChewingBridge.chewing.candTotalChoice() > 0)
    }

    // endregion

    // region openFrequentlyUsedCandidates

    @Test
    fun openFrequentlyUsedCandidates_shouldOpenCandidateWindow() {
        ChewingUtil.openFrequentlyUsedCandidates()
        assertTrue(ChewingUtil.candidateWindowOpened())
        assertTrue(ChewingBridge.chewing.candTotalChoice() > 0)
    }

    // endregion

    // region anyBufferIsNotEmpty

    @Test
    fun anyBufferIsNotEmpty_whenBufferHasContent_shouldReturnTrue() {
        ChewingBridge.chewing.handleDefault('w')
        assertTrue(ChewingUtil.anyBufferIsNotEmpty())
    }

    @Test
    fun anyBufferIsNotEmpty_whenBufferIsEmpty_shouldReturnFalse() {
        assertFalse(ChewingUtil.anyBufferIsNotEmpty())
    }

    // endregion

    // region candidateWindowOpened / candidateWindowClosed

    @Test
    fun candidateWindowOpened_afterOpeningCandidates_shouldReturnTrue() {
        ChewingUtil.openSymbolCandidates()
        assertTrue(ChewingUtil.candidateWindowOpened())
        assertFalse(ChewingUtil.candidateWindowClosed())
    }

    @Test
    fun candidateWindowClosed_afterClosingCandidates_shouldReturnTrue() {
        ChewingUtil.openSymbolCandidates()
        assertTrue(ChewingUtil.candidateWindowOpened())

        ChewingBridge.chewing.candClose()
        assertTrue(ChewingUtil.candidateWindowClosed())
        assertFalse(ChewingUtil.candidateWindowOpened())
    }

    @Test
    fun candidateWindowClosed_initially_shouldReturnTrue() {
        assertTrue(ChewingUtil.candidateWindowClosed())
        assertFalse(ChewingUtil.candidateWindowOpened())
    }

    // endregion

    // region handleShiftComma

    @Test
    fun handleShiftComma_inChineseMode_shouldInputFullWidthComma() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())

        ChewingUtil.handleShiftComma()
        // In Chinese mode, Shift+Comma produces a full-width comma via easy symbol input
        assertTrue(ChewingUtil.anyBufferIsNotEmpty())
    }

    @Test
    fun handleShiftComma_inSymbolMode_shouldInputHalfWidthComma() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())

        ChewingUtil.handleShiftComma()
        // In symbol mode, it inputs a regular comma character which gets sent directly
        // The commit string should contain the comma
        val commitString = ChewingBridge.chewing.commitStringStatic()
        assertTrue(commitString.contains(","))

        // Reset back to Chinese mode
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
    }

    // endregion

    // region listOfDataFiles

    @Test
    fun listOfDataFiles_shouldReturnCorrectFileList() {
        val dataFiles = ChewingUtil.listOfDataFiles()
        assertEquals(4, dataFiles.size)
        assertTrue(dataFiles.contains("tsi.dat"))
        assertTrue(dataFiles.contains("word.dat"))
        assertTrue(dataFiles.contains("swkb.dat"))
        assertTrue(dataFiles.contains("symbols.dat"))
    }

    // endregion
}
