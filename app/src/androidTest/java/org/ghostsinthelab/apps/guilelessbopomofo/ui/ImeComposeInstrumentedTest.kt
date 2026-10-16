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

package org.ghostsinthelab.apps.guilelessbopomofo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.CANDIDATE_TEST_TAG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.CandidatesPanel
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.ImeUiState
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.KEY_TEST_TAG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.Keyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.PhoneKeypad
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeySpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardSpecs
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardVariant
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.BooleanPreferenceSwitch
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImeComposeInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val selectedCandidates = mutableListOf<Candidate>()

    @Subscribe
    fun onCandidateButtonSelected(event: Events.CandidateButtonSelected) {
        selectedCandidates += event.candidate
    }

    @Before
    fun setUp() {
        EventBus.getDefault().register(this)
    }

    @After
    fun tearDown() {
        EventBus.getDefault().unregister(this)
        appContext.appSharedPreferences.edit().remove(TEST_PREFERENCE_KEY).commit()
    }

    @Test
    fun keyboard_drawsOneKeyPerSpecKey() {
        val uiState = ImeUiState().apply { imeSwitchEnabled = true }
        var keyboard by mutableStateOf(SoftKeyboard.DACHEN)
        composeRule.setContent {
            GuilelessTheme { Keyboard(KeyboardSpecs.of(keyboard, KeyboardVariant.DEFAULT), uiState) }
        }
        SoftKeyboard.entries.filterNot { it.isKeypad }.forEach { softKeyboard ->
            keyboard = softKeyboard
            composeRule.waitForIdle()
            val expected = KeyboardSpecs.of(softKeyboard, KeyboardVariant.DEFAULT).rows.sumOf { it.size }
            assertEquals("$softKeyboard", expected, composeRule.onAllNodesWithTag(KEY_TEST_TAG).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun phoneKeypad_drawsTheNumberPadAndItsFunctionKeys() {
        val uiState = ImeUiState().apply { imeSwitchEnabled = true }
        composeRule.setContent { GuilelessTheme { PhoneKeypad(uiState) } }
        // 11 symbol keys and # (punctuation), then Aa, symbols, IME switch, Backspace, Space, Enter
        assertEquals(12 + 6, composeRule.onAllNodesWithTag(KEY_TEST_TAG).fetchSemanticsNodes().size)
    }

    @Test
    fun keyboard_leavesOutTheImeSwitchKeyUnlessEnabled() {
        val uiState = ImeUiState().apply { imeSwitchEnabled = false }
        val spec = KeyboardSpecs.of(SoftKeyboard.DACHEN, KeyboardVariant.DEFAULT)
        composeRule.setContent { GuilelessTheme { Keyboard(spec, uiState) } }
        val imeSwitchKeys = spec.rows.flatten().count { it is KeySpec.Function && it.key == FunctionKey.IME_SWITCH }
        assertEquals(1, imeSwitchKeys)
        assertEquals(
            spec.rows.sumOf { it.size } - imeSwitchKeys,
            composeRule.onAllNodesWithTag(KEY_TEST_TAG).fetchSemanticsNodes().size,
        )
    }

    @Test
    fun candidates_tapPostsTheChosenCandidate() {
        val uiState = ImeUiState().apply {
            candidates = listOf(Candidate(0, "測"), Candidate(1, "試"))
            candidateLayoutStyle = ImeUiState.CandidateLayoutStyle.GRID
        }
        composeRule.setContent { GuilelessTheme { CandidatesPanel(uiState) } }
        composeRule.onAllNodesWithTag(CANDIDATE_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText("試").performTouchInput { down(center); up() }
        composeRule.waitForIdle()
        assertEquals(listOf(Candidate(1, "試")), selectedCandidates)
    }

    @Test
    fun settingsSwitch_writesThePreference() {
        val preferences = appContext.appSharedPreferences
        preferences.edit().putBoolean(TEST_PREFERENCE_KEY, false).commit()
        composeRule.setContent {
            GuilelessTheme { BooleanPreferenceSwitch(R.string.english_prediction_switch, TEST_PREFERENCE_KEY, false) }
        }
        composeRule.onNodeWithText(appContext.getString(R.string.english_prediction_switch)).performClick()
        composeRule.waitForIdle()
        assertTrue(preferences.getBoolean(TEST_PREFERENCE_KEY, false))

        composeRule.onAllNodesWithText(appContext.getString(R.string.english_prediction_switch)).onFirst().performClick()
        composeRule.waitForIdle()
        assertFalse(preferences.getBoolean(TEST_PREFERENCE_KEY, true))
    }

    private companion object {
        const val TEST_PREFERENCE_KEY = "compose_test_switch"
    }
}
