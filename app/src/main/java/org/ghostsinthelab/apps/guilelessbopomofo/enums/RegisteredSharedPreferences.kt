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

package org.ghostsinthelab.apps.guilelessbopomofo.enums

enum class RegisteredSharedPreferences(val key: String) {
    SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS("same_haptic_feedback_to_function_buttons"),
    USER_CANDIDATE_SELECTION_KEYS_OPTION("user_candidate_selection_keys_option"),
    USER_CONVERSION_ENGINE("user_conversion_engine"),
    USER_DISPLAY_DVORAK_HSU_BOTH_LAYOUT("user_display_dvorak_hsu_both_layout"),
    USER_DISPLAY_ETEN26_QWERTY_LAYOUT("user_display_eten26_qwerty_layout"),
    USER_DISPLAY_HSU_QWERTY_LAYOUT("user_display_hsu_qwerty_layout"),
    USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH("user_enable_double_touch_ime_switch"),
    USER_ENABLE_IME_SWITCH("user_enable_ime_switch"),
    USER_ENABLE_SPACE_AS_SELECTION("user_enable_space_as_selection"),
    USER_FULLSCREEN_WHEN_IN_LANDSCAPE("user_fullscreen_when_in_landscape"),
    USER_FULLSCREEN_WHEN_IN_PORTRAIT("user_fullscreen_when_in_portrait"),
    USER_HAPTIC_FEEDBACK_STRENGTH("user_haptic_feedback_strength"),
    USER_KEYBOARD_LAYOUT("user_keyboard_layout"),
    USER_KEY_BUTTON_HEIGHT("user_key_button_height"),
    USER_PHRASE_CHOICE_REARWARD("user_phrase_choice_rearward"),
}