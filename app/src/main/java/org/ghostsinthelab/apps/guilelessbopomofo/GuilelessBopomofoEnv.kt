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

object GuilelessBopomofoEnv {
    const val APP_SHARED_PREFERENCES: String = "GuilelessBopomofoService"
    var physicalKeyboardPresented: Boolean = false

    typealias RegisteredSharedPreferences = String
    const val SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS : RegisteredSharedPreferences = "same_haptic_feedback_to_function_buttons"
    const val USER_CANDIDATE_SELECTION_KEYS_OPTION : RegisteredSharedPreferences = "user_candidate_selection_keys_option"
    const val USER_CONVERSION_ENGINE : RegisteredSharedPreferences = "user_conversion_engine"
    const val USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT : RegisteredSharedPreferences = "user_display_dvorak_hsu_both_layout"
    const val USER_DISPLAY_ETEN26_QWERTY_LAYOUT : RegisteredSharedPreferences = "user_display_eten26_qwerty_layout"
    const val USER_DISPLAY_HSU_QWERTY_LAYOUT : RegisteredSharedPreferences = "user_display_hsu_qwerty_layout"
    const val USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH : RegisteredSharedPreferences = "user_enable_double_touch_ime_switch"
    const val USER_ENABLE_IME_SWITCH : RegisteredSharedPreferences = "user_enable_ime_switch"
    const val USER_ENABLE_SPACE_AS_SELECTION : RegisteredSharedPreferences = "user_enable_space_as_selection"
    const val USER_FULLSCREEN_WHEN_IN_LANDSCAPE : RegisteredSharedPreferences = "user_fullscreen_when_in_landscape"
    const val USER_FULLSCREEN_WHEN_IN_PORTRAIT : RegisteredSharedPreferences = "user_fullscreen_when_in_portrait"
    const val USER_HAPTIC_FEEDBACK_STRENGTH : RegisteredSharedPreferences = "user_haptic_feedback_strength"
    const val USER_KEYBOARD_LAYOUT : RegisteredSharedPreferences = "user_keyboard_layout"
    const val USER_KEY_BUTTON_HEIGHT : RegisteredSharedPreferences = "user_key_button_height"
    const val USER_PHRASE_CHOICE_REARWARD : RegisteredSharedPreferences = "user_phrase_choice_rearward"
}