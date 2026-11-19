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
import android.view.inputmethod.EditorInfo
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.keys.physical.PhysicalKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.prediction.EnglishWords

/**
 * What this input method sets on a libchewing context, from the user's preferences and its
 * own constants. A fresh context starts without any of it, so [applyAll] goes with every
 * connection, and [onPreferenceChanged] keeps the context in step afterwards.
 */
class ChewingSettings(
    private val sharedPreferences: SharedPreferences,
    private val physicalKeyboard: PhysicalKeyboard,
) {
    // Whether the text field being typed in lets libchewing learn from it. Kept here, as a
    // reconnected context starts out learning again and applyAll() has to put this back.
    private var learningAllowed = true

    /**
     * Installs the dictionaries if need be, connects libchewing and sets it up for Bopomofo.
     *
     * @throws java.io.IOException if the dictionaries cannot be installed.
     * @throws Chewing.ChewingInitException if libchewing cannot open a context.
     * @throws UnsatisfiedLinkError if the native library cannot be loaded.
     */
    fun initialize(context: Context) {
        val dataPath = context.applicationInfo.dataDir
        ChewingUtil.setupChewingData(context, dataPath)
        ChewingUtil.connect(dataPath)

        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        applyAll()
    }

    /** Puts every setting on the current context, as after connecting or reconnecting. */
    fun applyAll() {
        applySpaceAsSelection()
        applyPhraseChoiceRearward()
        applyConversionEngine()
        ChewingBridge.chewing.setCandPerPage(CANDIDATES_PER_PAGE)
        ChewingBridge.chewing.configSetInt("chewing.sort_candidates_by_frequency", 1)
        applySelectionKeys()
        applyLearning()
    }

    /**
     * Lets libchewing learn phrases and typing history from the text field [info] describes,
     * unless it asks not to be learned from, as incognito tabs do, or holds a password.
     */
    fun applyLearningFor(info: EditorInfo?) {
        learningAllowed = info?.let { !it.forbidsLearning() } ?: true
        applyLearning()
    }

    // Also stops libchewing 0.14 recording the typing history in v4/history_dict.bin.
    private fun applyLearning() {
        ChewingBridge.chewing.configSetInt(OPTION_DISABLE_AUTO_LEARN, if (learningAllowed) 0 else 1)
    }

    // IME_FLAG_NO_PERSONALIZED_LEARNING is from API 26; older text fields never set it.
    private fun EditorInfo.forbidsLearning(): Boolean =
        imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0 ||
            EnglishWords.isPasswordField(inputType)

    /** The engine depends on which keyboard is in use, so this goes with every change of that. */
    fun applyConversionEngine() {
        val key = if (physicalKeyboard.isPresent)
            USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
        else
            USER_CONVERSION_ENGINE
        val mode = sharedPreferences.getInt(key, ConversionEngines.CHEWING_CONVERSION_ENGINE.mode)
        ChewingBridge.chewing.configSetInt("chewing.conversion_engine", mode)
    }

    private fun applySpaceAsSelection() {
        val enabled = sharedPreferences.getBoolean(USER_ENABLE_SPACE_AS_SELECTION, true)
        ChewingBridge.chewing.setSpaceAsSelection(if (enabled) 1 else 0)
    }

    private fun applyPhraseChoiceRearward() {
        val enabled = sharedPreferences.getBoolean(USER_PHRASE_CHOICE_REARWARD, false)
        ChewingBridge.chewing.setPhraseChoiceRearward(if (enabled) 1 else 0)
    }

    private fun applySelectionKeys() {
        sharedPreferences.getString(USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set)?.let {
            ChewingBridge.chewing.setSelKey(SelectionKeys.valueOf(it).keys, CANDIDATES_PER_PAGE)
        }
    }

    /**
     * Applies the preference under [key] if it is one of libchewing's.
     *
     * @return whether it was.
     */
    fun onPreferenceChanged(key: String?): Boolean {
        when (key) {
            USER_ENABLE_SPACE_AS_SELECTION -> applySpaceAsSelection()
            USER_PHRASE_CHOICE_REARWARD -> applyPhraseChoiceRearward()
            USER_CANDIDATE_SELECTION_KEYS_OPTION -> applySelectionKeys()
            USER_CONVERSION_ENGINE, USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD -> applyConversionEngine()
            else -> return false
        }
        return true
    }

    companion object {
        private const val CANDIDATES_PER_PAGE = 10
        private const val OPTION_DISABLE_AUTO_LEARN = "chewing.disable_auto_learn_phrase"
    }
}
