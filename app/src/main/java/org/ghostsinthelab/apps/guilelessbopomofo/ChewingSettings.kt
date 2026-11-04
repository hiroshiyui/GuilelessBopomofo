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
import android.util.Log
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_SPACE_AS_SELECTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHRASE_CHOICE_REARWARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.physicalKeyboardPresented
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys

/**
 * What this input method sets on a libchewing context, from the user's preferences and its
 * own constants. A fresh context starts without any of it, so [applyAll] goes with every
 * connection, and [onPreferenceChanged] keeps the context in step afterwards.
 */
class ChewingSettings(private val sharedPreferences: SharedPreferences) {
    private val logTag = "ChewingSettings"

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
        ChewingBridge.chewing.connect(dataPath)
        Log.d(logTag, "Chewing context ptr: ${ChewingBridge.chewing.context}")

        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        applyAll()
    }

    fun applyAll() {
        applySpaceAsSelection()
        applyPhraseChoiceRearward()
        applyConversionEngine()
        ChewingBridge.chewing.setCandPerPage(CANDIDATES_PER_PAGE)
        ChewingBridge.chewing.configSetInt("chewing.sort_candidates_by_frequency", 1)
        applySelectionKeys()
    }

    /** The engine depends on which keyboard is in use, so this goes with every change of that. */
    fun applyConversionEngine() {
        val key = if (physicalKeyboardPresented)
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
    }
}
