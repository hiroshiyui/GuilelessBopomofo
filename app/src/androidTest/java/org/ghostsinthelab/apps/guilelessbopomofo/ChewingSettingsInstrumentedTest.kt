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

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SOFT_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyboard
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChewingSettingsInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var preferences: SharedPreferences
    private val physicalKeyboard = PhysicalKeyboard()
    private lateinit var settings: ChewingSettings

    @Before
    fun setUp() {
        preferences = appContext.getSharedPreferences(APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        settings = ChewingSettings(preferences, physicalKeyboard)
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
        ChewingBridge.chewing.delete()
    }

    @Test
    fun initialize_connectsAndStartsInBopomofo() {
        settings.initialize(appContext)
        assertTrue(ChewingBridge.chewing.context != 0L)
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())
        assertEquals(10, ChewingBridge.chewing.getCandPerPage())
    }

    @Test
    fun applyAll_putsThePreferencesOnTheContext() {
        preferences.edit()
            .putBoolean(USER_ENABLE_SPACE_AS_SELECTION, false)
            .putBoolean(USER_PHRASE_CHOICE_REARWARD, true)
            .putString(USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.HOME_ROW.set)
            .putInt(USER_CONVERSION_ENGINE, ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode)
            .commit()
        settings.initialize(appContext)
        assertEquals(0, ChewingBridge.chewing.getSpaceAsSelection())
        assertEquals(1, ChewingBridge.chewing.getPhraseChoiceRearward())
        assertArrayEquals(SelectionKeys.HOME_ROW.keys, ChewingBridge.chewing.getSelKey())
        assertEquals(ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode, ChewingBridge.chewing.configGetInt("chewing.conversion_engine"))
    }

    @Test
    fun theDefaults_areSpaceAsSelection_forwardChoice_numberRow_chewingEngine() {
        settings.initialize(appContext)
        assertEquals(1, ChewingBridge.chewing.getSpaceAsSelection())
        assertEquals(0, ChewingBridge.chewing.getPhraseChoiceRearward())
        assertArrayEquals(SelectionKeys.NUMBER_ROW.keys, ChewingBridge.chewing.getSelKey())
        assertEquals(ConversionEngines.CHEWING_CONVERSION_ENGINE.mode, ChewingBridge.chewing.configGetInt("chewing.conversion_engine"))
    }

    @Test
    fun theConversionEngine_followsThePhysicalKeyboard() {
        preferences.edit()
            .putInt(USER_CONVERSION_ENGINE, ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode)
            .putInt(USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD, ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode)
            .commit()
        settings.initialize(appContext)
        assertEquals(ConversionEngines.SIMPLE_CONVERSION_ENGINE.mode, ChewingBridge.chewing.configGetInt("chewing.conversion_engine"))

        physicalKeyboard.isPresent = true
        settings.applyConversionEngine()
        assertEquals(ConversionEngines.FUZZY_CHEWING_CONVERSION_ENGINE.mode, ChewingBridge.chewing.configGetInt("chewing.conversion_engine"))
    }

    @Test
    fun onPreferenceChanged_appliesItsOwnKeys_andDeclinesOthers() {
        settings.initialize(appContext)
        preferences.edit().putBoolean(USER_ENABLE_SPACE_AS_SELECTION, false).commit()
        assertTrue(settings.onPreferenceChanged(USER_ENABLE_SPACE_AS_SELECTION))
        assertEquals(0, ChewingBridge.chewing.getSpaceAsSelection())

        preferences.edit().putString(USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.TAB_ROW.set).commit()
        assertTrue(settings.onPreferenceChanged(USER_CANDIDATE_SELECTION_KEYS_OPTION))
        assertArrayEquals(SelectionKeys.TAB_ROW.keys, ChewingBridge.chewing.getSelKey())

        assertFalse(settings.onPreferenceChanged(USER_SOFT_KEYBOARD_LAYOUT))
        assertFalse(settings.onPreferenceChanged(null))
    }
}
