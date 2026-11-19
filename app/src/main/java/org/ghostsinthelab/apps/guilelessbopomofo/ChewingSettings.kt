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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.EditorInfo
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_LEARN_FROM_TYPING
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_LEARN_NEW_WORDS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_SPACE_CYCLES_CANDIDATES
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
        applySpaceCycling()
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

    // The text field decides whether to learn at all, and in fields that allow it the user's
    // preferences decide what. libchewing 0.14 learns everything, the typing history in
    // v4/history_dict.bin included, through the history option.
    private fun applyLearning() {
        val fromTyping = sharedPreferences.getBoolean(USER_LEARN_FROM_TYPING, true)
        val newWords = sharedPreferences.getBoolean(USER_LEARN_NEW_WORDS, false)
        ChewingBridge.chewing.configSetInt(OPTION_DISABLE_AUTO_LEARN, if (learningAllowed) 0 else 1)
        ChewingBridge.chewing.configSetInt(OPTION_LEARN_FROM_TYPING, if (fromTyping) 1 else 0)
        ChewingBridge.chewing.configSetInt(OPTION_LEARN_NEW_WORDS, if (newWords) 1 else 0)
    }

    // IME_FLAG_NO_PERSONALIZED_LEARNING is from API 26, but only a constant: older text
    // fields never set it, so reading it there is safe.
    @SuppressLint("InlinedApi")
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

    // libchewing only cycles while Space is the selection key; 0 opens the list at once.
    private fun applySpaceCycling() {
        val candidates = sharedPreferences.getInt(USER_SPACE_CYCLES_CANDIDATES, 0)
        ChewingBridge.chewing.configSetInt(OPTION_CYCLE_FIRST_N_SELECTS, candidates)
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
            USER_SPACE_CYCLES_CANDIDATES -> applySpaceCycling()
            USER_LEARN_FROM_TYPING, USER_LEARN_NEW_WORDS -> applyLearning()
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
        private const val OPTION_LEARN_FROM_TYPING = "chewing.auto_learn_user_history"
        private const val OPTION_LEARN_NEW_WORDS = "chewing.auto_learn_new_words"
        private const val OPTION_CYCLE_FIRST_N_SELECTS = "chewing.cycle_first_n_selects"
    }
}
