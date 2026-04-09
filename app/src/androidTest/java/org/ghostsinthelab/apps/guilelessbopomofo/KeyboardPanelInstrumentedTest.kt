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
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_ETEN26_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DISPLAY_HSU_QWERTY_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ImeLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class KeyboardPanelInstrumentedTest {
    private val logTag = "KeyboardPanelTest"
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataFiles = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewBinding: ImeLayoutBinding
    private val keyboardPanel get() = viewBinding.keyboardPanel

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
            APP_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE
        )

        // Inflate the IME layout on the main thread
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            viewBinding = ImeLayoutBinding.inflate(LayoutInflater.from(appContext))
        }
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
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_hsuLayout() {
        setLayoutPreference("KB_HSU")
        setQwertyVariantPreference(USER_DISPLAY_HSU_QWERTY_LAYOUT, false)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_hsuQwertyLayout() {
        setLayoutPreference("KB_HSU")
        setQwertyVariantPreference(USER_DISPLAY_HSU_QWERTY_LAYOUT, true)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_et26Layout() {
        setLayoutPreference("KB_ET26")
        setQwertyVariantPreference(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, false)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_et26QwertyLayout() {
        setLayoutPreference("KB_ET26")
        setQwertyVariantPreference(USER_DISPLAY_ETEN26_QWERTY_LAYOUT, true)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_et41Layout() {
        setLayoutPreference("KB_ET")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun switchToBopomofoLayout_dachenCp26Layout() {
        setLayoutPreference("KB_DACHEN_CP26")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    // --- Alphanumerical (QWERTY) layout ---

    @Test
    fun switchToAlphanumericalLayout() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.QWERTY, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
        // restore
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
    }

    // --- Compact layout ---

    @Test
    fun switchToCompactLayout() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.COMPACT)
        }
        assertEquals(Layout.COMPACT, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)
    }

    @Test
    fun setShapeMode_afterCompactInit() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToCompactLayout()
            keyboardPanel.setShapeMode("Test Mode")
        }
        // No crash means success
        assertEquals(Layout.COMPACT, keyboardPanel.currentLayout)
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
            keyboardPanel.switchToLayout(Layout.CANDIDATES)
        }
        assertEquals(Layout.CANDIDATES, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views", keyboardPanel.childCount > 0)

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
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)

        // Type something and open candidates
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.CANDIDATES)
        }
        assertEquals(Layout.CANDIDATES, keyboardPanel.currentLayout)

        // Back to main
        ChewingBridge.chewing.candClose()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views after round-trip", keyboardPanel.childCount > 0)

        // cleanup
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.cleanBopomofoBuf()
    }

    @Test
    fun layoutRoundTrip_mainToCompactToMain() {
        setLayoutPreference("KB_DEFAULT")

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.COMPACT)
        }
        assertEquals(Layout.COMPACT, keyboardPanel.currentLayout)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
        assertTrue("KeyboardPanel should have child views after round-trip", keyboardPanel.childCount > 0)
    }

    // --- releaseShiftKey on different layouts ---

    @Test
    fun releaseShiftKey_onDefaultLayout_noCrash() {
        setLayoutPreference("KB_DEFAULT")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
            keyboardPanel.releaseShiftKey()
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
            keyboardPanel.switchToLayout(Layout.CANDIDATES)
            keyboardPanel.releaseShiftKey()
        }
        // No crash means success — ShiftKey not present in candidates layout

        // cleanup
        ChewingBridge.chewing.candClose()
        ChewingBridge.chewing.cleanPreeditBuf()
        ChewingBridge.chewing.cleanBopomofoBuf()
    }

    @Test
    fun releaseShiftKey_onCompactLayout_noCrash() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToCompactLayout()
            keyboardPanel.releaseShiftKey()
        }
        // No crash means success
    }

    // --- Toggle main layout mode ---

    @Test
    fun toggleMainLayoutMode_chineseToSymbolAndBack() {
        setLayoutPreference("KB_DEFAULT")
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.switchToLayout(Layout.MAIN)
        }
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)

        // Toggle to alphanumeric
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.toggleMainLayoutMode()
        }
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(Layout.QWERTY, keyboardPanel.currentLayout)

        // Toggle back to Chinese
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            keyboardPanel.toggleMainLayoutMode()
        }
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(Layout.MAIN, keyboardPanel.currentLayout)
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
