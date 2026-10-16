/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
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

import android.content.SharedPreferences
import android.util.Log
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class KeyboardControllerInstrumentedTest {
    private val logTag = "KeyboardControllerTest"
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataFiles = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    private lateinit var sharedPreferences: SharedPreferences
    private val uiState = ImeUiState()
    private lateinit var keyboardController: KeyboardController

    @Before
    fun setUp() {
        // Copy Chewing data files to data directory
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", dataPath, file))
            Log.d(logTag, "Copying ${file}...")
            val dataInputStream = appContext.assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)
            try {
                dataInputStream.copyTo(dataOutputStream)
            } finally {
                dataInputStream.close()
                dataOutputStream.close()
            }
        }

        // Initialize Chewing
        ChewingBridge.chewing.connect(dataPath)
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(10)

        sharedPreferences = appContext.getSharedPreferences(
            APP_SHARED_PREFERENCES, Context.MODE_PRIVATE
        )

        keyboardController = KeyboardController(uiState, sharedPreferences)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()
    }

    // --- Layout switching: each Bopomofo keyboard layout ---

    @Test
    fun switchToBopomofoLayout_defaultLayout() {
        setLayoutPreference("KB_DEFAULT")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.DACHEN, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_hsuLayout() {
        setLayoutPreference("KB_HSU")
        setQwertyVariantPreference(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.HSU, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_hsuQwertyLayout() {
        setLayoutPreference("KB_HSU")
        setQwertyVariantPreference(USER_DISPLAY_HSU_QWERTY_LAYOUT, true)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.HSU_QWERTY, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_et26Layout() {
        setLayoutPreference("KB_ET26")
        setQwertyVariantPreference(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.ET26, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_et26QwertyLayout() {
        setLayoutPreference("KB_ET26")
        setQwertyVariantPreference(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, true)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.ET26_QWERTY, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_et41Layout() {
        setLayoutPreference("KB_ET")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.ET41, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_dachenCp26Layout() {
        setLayoutPreference("KB_DACHEN_CP26")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.DACHEN_CP26, uiState.softKeyboard)
    }

    @Test
    fun switchToBopomofoLayout_dachenCompactKeypad() {
        setLayoutPreference("KB_DACHEN_COMPACT")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.DACHEN_COMPACT, uiState.softKeyboard)
        // the keypad types Dachen key codes, so libchewing has to read them as Dachen
        assertEquals("KB_DEFAULT", ChewingBridge.chewing.getKBString())
    }

    // --- Alphanumerical (QWERTY) layout ---

    @Test
    fun switchToAlphanumericalLayout() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.QWERTY, keyboardController.currentLayout)
        assertEquals(Layout.QWERTY, uiState.layout)
        // restore
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
    }

    // --- Compact layout ---

    @Test
    fun switchToCompactLayout() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.COMPACT)
        }
        assertEquals(Layout.COMPACT, keyboardController.currentLayout)
        assertTrue("Compact layout should report Chinese mode", uiState.compactChineseMode)
    }

    @Test
    fun refreshCompactIndicators_followsShapeMode() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToCompactLayout()
            ChewingBridge.chewing.setShapeMode(ShapeMode.FULL.mode)
            keyboardController.refreshCompactIndicators()
        }
        assertEquals(Layout.COMPACT, keyboardController.currentLayout)
        assertTrue("Compact layout should report full width", uiState.compactFullWidth)
        // restore
        ChewingBridge.chewing.setShapeMode(ShapeMode.HALF.mode)
    }

    // --- Candidates layout ---

    @Test
    fun switchToCandidatesLayout() {
        // Type something to generate candidates
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.CANDIDATES)
        }
        assertEquals(Layout.CANDIDATES, keyboardController.currentLayout)
        assertTrue("Candidates should be listed", uiState.candidates.isNotEmpty())

        // cleanup
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.cleanBopomofoBuf()
    }

    // --- Layout round-trips ---

    @Test
    fun layoutRoundTrip_mainToCandidatesToMain() {
        setLayoutPreference("KB_DEFAULT")

        // Start at main
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)

        // Type something and open candidates
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.CANDIDATES)
        }
        assertEquals(Layout.CANDIDATES, keyboardController.currentLayout)

        // Back to main
        ChewingBridge.chewing.candClose()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.DACHEN, uiState.softKeyboard)

        // cleanup
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.cleanBopomofoBuf()
    }

    @Test
    fun layoutRoundTrip_mainToCompactToMain() {
        setLayoutPreference("KB_DEFAULT")

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.COMPACT)
        }
        assertEquals(Layout.COMPACT, keyboardController.currentLayout)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
        assertEquals(SoftKeyboard.DACHEN, uiState.softKeyboard)
    }

    // --- releaseShiftKey on different layouts ---

    @Test
    fun releaseShiftKey_onDefaultLayout_noCrash() {
        setLayoutPreference("KB_DEFAULT")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
            keyboardController.releaseShiftKey()
        }
        // No crash means success
    }

    @Test
    fun releaseShiftKey_onCandidatesLayout_noCrash() {
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.CANDIDATES)
            keyboardController.releaseShiftKey()
        }
        assertEquals(ShiftKey.ShiftKeyState.RELEASED, uiState.shiftKeyState)

        // cleanup
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.cleanBopomofoBuf()
    }

    @Test
    fun releaseShiftKey_onCompactLayout_noCrash() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToCompactLayout()
            keyboardController.releaseShiftKey()
        }
        // No crash means success
    }

    @Test
    fun releaseShiftKey_releasesHeldShift() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ShiftKey.switchToState(uiState, ShiftKey.ShiftKeyState.HOLD)
            keyboardController.releaseShiftKey()
        }
        assertEquals(ShiftKey.ShiftKeyState.RELEASED, uiState.shiftKeyState)
    }

    // --- Toggle main layout mode ---

    @Test
    fun toggleMainLayoutMode_chineseToSymbolAndBack() {
        setLayoutPreference("KB_DEFAULT")
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardController.currentLayout)

        // Toggle to alphanumeric
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.toggleMainLayoutMode()
        }
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(Layout.QWERTY, keyboardController.currentLayout)

        // Toggle back to Chinese
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardController.toggleMainLayoutMode()
        }
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(Layout.MAIN, keyboardController.currentLayout)
    }

    // --- Helpers ---

    private fun setLayoutPreference(layout: String) {
        sharedPreferences.edit()
            .putString(USER_SOFT_KEYBOARD_LAYOUT, layout)
            .apply()
    }

    private fun setQwertyVariantPreference(key: String, enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, enabled)
            .apply()
    }
}
