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

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.APP_SHARED_PREFERENCES
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_LEARN_FROM_TYPING
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_LEARN_NEW_WORDS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SPACE_CYCLES_CANDIDATES
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The settings for what libchewing 0.14 brought: learning, and Space going through candidates. */
@RunWith(AndroidJUnit4::class)
class ChewingV4SettingsInstrumentedTest {
    private lateinit var data: IsolatedChewingData
    private lateinit var preferences: SharedPreferences
    private lateinit var settings: ChewingSettings

    @Before
    fun setUp() {
        data = IsolatedChewingData()
        preferences = data.appContext.getSharedPreferences(APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        settings = ChewingSettings(preferences, PhysicalKeyboard())
        data.installAndConnect()
        settings.applyAll()
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
        data.close()
    }

    private fun option(name: String): Int = ChewingBridge.chewing.configGetInt(name)

    private fun field(inputType: Int = InputType.TYPE_CLASS_TEXT, imeOptions: Int = 0) = EditorInfo().apply {
        this.inputType = inputType
        this.imeOptions = imeOptions
    }

    // region learning

    @Test
    fun byDefault_itLearnsFromTyping_butNotNewWords() {
        assertEquals(0, option(DISABLE_LEARNING))
        assertEquals(1, option(LEARN_FROM_TYPING))
        assertEquals(0, option(LEARN_NEW_WORDS))
    }

    @Test
    fun theLearningPreferences_reachLibchewing() {
        preferences.edit().putBoolean(USER_LEARN_FROM_TYPING, false).putBoolean(USER_LEARN_NEW_WORDS, true).commit()
        assertTrue(settings.onPreferenceChanged(USER_LEARN_FROM_TYPING))
        assertEquals(0, option(LEARN_FROM_TYPING))
        assertEquals(1, option(LEARN_NEW_WORDS))

        preferences.edit().putBoolean(USER_LEARN_NEW_WORDS, false).commit()
        assertTrue(settings.onPreferenceChanged(USER_LEARN_NEW_WORDS))
        assertEquals(0, option(LEARN_NEW_WORDS))
    }

    @Test
    fun aPasswordField_isNotLearnedFrom() {
        for (password in listOf(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        )) {
            settings.applyLearningFor(field(inputType = password))
            assertEquals("inputType $password", 1, option(DISABLE_LEARNING))
        }
    }

    @Test
    fun aFieldAskingNotToBeLearnedFrom_isNotLearnedFrom() {
        settings.applyLearningFor(field(imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING))
        assertEquals(1, option(DISABLE_LEARNING))
    }

    @Test
    fun anOrdinaryField_isLearnedFromAgain() {
        settings.applyLearningFor(field(imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING))
        settings.applyLearningFor(field())
        assertEquals(0, option(DISABLE_LEARNING))
    }

    @Test
    fun noFieldAtAll_isLearnedFrom() {
        settings.applyLearningFor(field(imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING))
        settings.applyLearningFor(null)
        assertEquals(0, option(DISABLE_LEARNING))
    }

    @Test
    fun theFieldsWish_outlastsAReconnect() {
        settings.applyLearningFor(field(imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING))
        // A fresh context learns again until the settings are put back on it.
        ChewingBridge.chewing.connect(data.path)
        assertEquals(0, option(DISABLE_LEARNING))
        settings.applyAll()
        assertEquals(1, option(DISABLE_LEARNING))
    }

    @Test
    fun theFieldsWish_winsOverThePreferences() {
        settings.applyLearningFor(field(imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING))
        preferences.edit().putBoolean(USER_LEARN_FROM_TYPING, true).commit()
        settings.onPreferenceChanged(USER_LEARN_FROM_TYPING)
        assertEquals(1, option(DISABLE_LEARNING))
    }

    // endregion

    // region Space cycling

    @Test
    fun byDefault_spaceOpensTheListAtOnce() {
        assertEquals(0, option(CYCLE_FIRST_N_SELECTS))
    }

    @Test
    fun theSpaceCyclingPreference_reachesLibchewing() {
        preferences.edit().putInt(USER_SPACE_CYCLES_CANDIDATES, 3).commit()
        assertTrue(settings.onPreferenceChanged(USER_SPACE_CYCLES_CANDIDATES))
        assertEquals(3, option(CYCLE_FIRST_N_SELECTS))
    }

    @Test
    fun theSpaceCyclingPreference_outlastsAReconnect() {
        preferences.edit().putInt(USER_SPACE_CYCLES_CANDIDATES, 2).commit()
        ChewingBridge.chewing.connect(data.path)
        settings.applyAll()
        assertEquals(2, option(CYCLE_FIRST_N_SELECTS))
    }

    // endregion

    private companion object {
        const val DISABLE_LEARNING = "chewing.disable_auto_learn_phrase"
        const val LEARN_FROM_TYPING = "chewing.auto_learn_user_history"
        const val LEARN_NEW_WORDS = "chewing.auto_learn_new_words"
        const val CYCLE_FIRST_N_SELECTS = "chewing.cycle_first_n_selects"
    }
}
