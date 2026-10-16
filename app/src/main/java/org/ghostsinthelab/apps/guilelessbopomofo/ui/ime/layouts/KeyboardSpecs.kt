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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts

import androidx.annotation.DrawableRes
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.BACKSPACE
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.ENTER
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.MODE_SWITCH_TO_BOPOMOFO
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.MODE_SWITCH_TO_ENGLISH
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.PUNCTUATION
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.SHIFT
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.SPACE
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey.SYMBOL

/**
 * Every on-screen keyboard. The rows of keys are the same on every screen; what changes on
 * wider ones is a few keys' widths, and on QWERTY the arrangement of the rows.
 */
object KeyboardSpecs {
    /**
     * The keys of [keyboard], arranged for the screen [variant] describes; null for a keyboard
     * that is no rows of keys, the [CompactDachenKeypad].
     */
    fun of(keyboard: SoftKeyboard, variant: KeyboardVariant): KeyboardSpec? = when (keyboard) {
        SoftKeyboard.DACHEN_COMPACT -> null
        SoftKeyboard.DACHEN -> dachen(variant)
        SoftKeyboard.DACHEN_CP26 -> dachenCp26()
        SoftKeyboard.ET41 -> et41(variant)
        SoftKeyboard.ET26 -> et26(variant)
        SoftKeyboard.ET26_QWERTY -> et26Qwerty(variant)
        SoftKeyboard.HSU -> hsu(variant)
        SoftKeyboard.HSU_QWERTY -> hsuQwerty(variant)
        SoftKeyboard.QWERTY -> qwerty(variant)
    }

    private fun dachen(variant: KeyboardVariant): KeyboardSpec {
        return KeyboardSpec(
            listOf(
                characterRow(
                    "1" to R.drawable.guileless_bopomofo_keyboard_b,
                    "2" to R.drawable.guileless_bopomofo_keyboard_d,
                    "3" to R.drawable.guileless_bopomofo_keyboard_tone_3,
                    "4" to R.drawable.guileless_bopomofo_keyboard_tone_4,
                    "5" to R.drawable.guileless_bopomofo_keyboard_zhi,
                    "6" to R.drawable.guileless_bopomofo_keyboard_tone_2,
                    "7" to R.drawable.guileless_bopomofo_keyboard_tone_light,
                    "8" to R.drawable.guileless_bopomofo_keyboard_a,
                    "9" to R.drawable.guileless_bopomofo_keyboard_ai,
                    "0" to R.drawable.guileless_bopomofo_keyboard_an,
                ),
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_p,
                    "W" to R.drawable.guileless_bopomofo_keyboard_t,
                    "E" to R.drawable.guileless_bopomofo_keyboard_g,
                    "R" to R.drawable.guileless_bopomofo_keyboard_j,
                    "T" to R.drawable.guileless_bopomofo_keyboard_chi,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_zi,
                    "U" to R.drawable.guileless_bopomofo_keyboard_yi,
                    "I" to R.drawable.guileless_bopomofo_keyboard_o,
                    "O" to R.drawable.guileless_bopomofo_keyboard_ei,
                    "P" to R.drawable.guileless_bopomofo_keyboard_en,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_m,
                    "S" to R.drawable.guileless_bopomofo_keyboard_n,
                    "D" to R.drawable.guileless_bopomofo_keyboard_k,
                    "F" to R.drawable.guileless_bopomofo_keyboard_q,
                    "G" to R.drawable.guileless_bopomofo_keyboard_shi,
                    "H" to R.drawable.guileless_bopomofo_keyboard_ci,
                    "J" to R.drawable.guileless_bopomofo_keyboard_wu,
                    "K" to R.drawable.guileless_bopomofo_keyboard_e,
                    "L" to R.drawable.guileless_bopomofo_keyboard_ao,
                    "SEMICOLON" to R.drawable.guileless_bopomofo_keyboard_ang,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_f,
                    "X" to R.drawable.guileless_bopomofo_keyboard_l,
                    "C" to R.drawable.guileless_bopomofo_keyboard_h,
                    "V" to R.drawable.guileless_bopomofo_keyboard_x,
                    "B" to R.drawable.guileless_bopomofo_keyboard_ri,
                    "N" to R.drawable.guileless_bopomofo_keyboard_si,
                    "M" to R.drawable.guileless_bopomofo_keyboard_yu,
                    "COMMA" to R.drawable.guileless_bopomofo_keyboard_ie,
                    "PERIOD" to R.drawable.guileless_bopomofo_keyboard_ou,
                    "SLASH" to R.drawable.guileless_bopomofo_keyboard_eng,
                ),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, 2f),
                    character("MINUS", R.drawable.guileless_bopomofo_keyboard_er),
                    function(PUNCTUATION),
                    function(BACKSPACE),
                    function(ENTER, if (variant == KeyboardVariant.LARGE_SCREEN) 1f else 1.5f),
                ),
            )
        )
    }

    private fun dachenCp26(): KeyboardSpec {
        return KeyboardSpec(
            listOf(
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_b_p,
                    "W" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_d_t,
                    "E" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_g_tone2,
                    "R" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_j_tone3,
                    "T" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_zhi_chi,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_zi_tone_light,
                    "U" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_yi_a,
                    "I" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_o_ai,
                    "O" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_ei_an,
                    "P" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_en_er,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_m,
                    "S" to R.drawable.guileless_bopomofo_keyboard_n,
                    "D" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_k_tone4,
                    "F" to R.drawable.guileless_bopomofo_keyboard_q,
                    "G" to R.drawable.guileless_bopomofo_keyboard_shi,
                    "H" to R.drawable.guileless_bopomofo_keyboard_ci,
                    "J" to R.drawable.guileless_bopomofo_keyboard_wu,
                    "K" to R.drawable.guileless_bopomofo_keyboard_e,
                    "L" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_ao_ang,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_f,
                    "X" to R.drawable.guileless_bopomofo_keyboard_l,
                    "C" to R.drawable.guileless_bopomofo_keyboard_h,
                    "V" to R.drawable.guileless_bopomofo_keyboard_x,
                    "B" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_ri_ie,
                    "N" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_si_eng,
                    "M" to R.drawable.guileless_bopomofo_keyboard_dachen_cp26_yu_ou,
                ) + function(PUNCTUATION),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, 3f),
                    function(BACKSPACE, 1.5f),
                    function(ENTER, 1.5f),
                ),
            )
        )
    }

    private fun et41(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        return KeyboardSpec(
            listOf(
                characterRow(
                    "1" to R.drawable.guileless_bopomofo_keyboard_tone_light,
                    "2" to R.drawable.guileless_bopomofo_keyboard_tone_2,
                    "3" to R.drawable.guileless_bopomofo_keyboard_tone_3,
                    "4" to R.drawable.guileless_bopomofo_keyboard_tone_4,
                    "7" to R.drawable.guileless_bopomofo_keyboard_q,
                    "8" to R.drawable.guileless_bopomofo_keyboard_an,
                    "9" to R.drawable.guileless_bopomofo_keyboard_en,
                    "0" to R.drawable.guileless_bopomofo_keyboard_ang,
                    "MINUS" to R.drawable.guileless_bopomofo_keyboard_eng,
                    "EQUALS" to R.drawable.guileless_bopomofo_keyboard_er,
                ),
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_ei,
                    "W" to R.drawable.guileless_bopomofo_keyboard_ie,
                    "E" to R.drawable.guileless_bopomofo_keyboard_yi,
                    "R" to R.drawable.guileless_bopomofo_keyboard_e,
                    "T" to R.drawable.guileless_bopomofo_keyboard_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_ou,
                    "U" to R.drawable.guileless_bopomofo_keyboard_yu,
                    "I" to R.drawable.guileless_bopomofo_keyboard_ai,
                    "O" to R.drawable.guileless_bopomofo_keyboard_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_p,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_si,
                    "D" to R.drawable.guileless_bopomofo_keyboard_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_j,
                    "H" to R.drawable.guileless_bopomofo_keyboard_h,
                    "J" to R.drawable.guileless_bopomofo_keyboard_ri,
                    "K" to R.drawable.guileless_bopomofo_keyboard_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_l,
                    "SEMICOLON" to R.drawable.guileless_bopomofo_keyboard_zi,
                    "APOSTROPHE" to R.drawable.guileless_bopomofo_keyboard_ci,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_ao,
                    "X" to R.drawable.guileless_bopomofo_keyboard_wu,
                    "C" to R.drawable.guileless_bopomofo_keyboard_x,
                    "V" to R.drawable.guileless_bopomofo_keyboard_g,
                    "B" to R.drawable.guileless_bopomofo_keyboard_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_n,
                    "M" to R.drawable.guileless_bopomofo_keyboard_m,
                    "COMMA" to R.drawable.guileless_bopomofo_keyboard_zhi,
                    "PERIOD" to R.drawable.guileless_bopomofo_keyboard_chi,
                    "SLASH" to R.drawable.guileless_bopomofo_keyboard_shi,
                ),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, if (wide) 5f else 2f),
                    function(PUNCTUATION),
                    function(BACKSPACE),
                    function(ENTER, 1.5f),
                ),
            )
        )
    }

    private fun et26(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        return KeyboardSpec(
            listOf(
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_et26_zi_ei,
                    "W" to R.drawable.guileless_bopomofo_keyboard_et26_ci_ie,
                    "E" to R.drawable.guileless_bopomofo_keyboard_yi,
                    "R" to R.drawable.guileless_bopomofo_keyboard_e,
                    "T" to R.drawable.guileless_bopomofo_keyboard_et26_t_ang,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_chi,
                    "U" to R.drawable.guileless_bopomofo_keyboard_yu,
                    "I" to R.drawable.guileless_bopomofo_keyboard_ai,
                    "O" to R.drawable.guileless_bopomofo_keyboard_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_et26_p_ou,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_si,
                    "D" to R.drawable.guileless_bopomofo_keyboard_et26_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_et26_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_et26_j_zhi,
                    "H" to R.drawable.guileless_bopomofo_keyboard_et26_h_er,
                    "J" to R.drawable.guileless_bopomofo_keyboard_et26_ri,
                    "K" to R.drawable.guileless_bopomofo_keyboard_et26_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_et26_l_eng,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_ao,
                    "X" to R.drawable.guileless_bopomofo_keyboard_wu,
                    "C" to R.drawable.guileless_bopomofo_keyboard_hsu_x_shi,
                    "V" to R.drawable.guileless_bopomofo_keyboard_et26_g_q,
                    "B" to R.drawable.guileless_bopomofo_keyboard_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_hsu_n_en,
                    "M" to R.drawable.guileless_bopomofo_keyboard_hsu_m_an,
                ) + function(PUNCTUATION),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, if (wide) 5f else 3f),
                    function(BACKSPACE),
                    function(ENTER, if (wide) 1f else 1.5f),
                ),
            )
        )
    }

    private fun et26Qwerty(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        return KeyboardSpec(
            listOf(
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_q,
                    "W" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_w,
                    "E" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_e,
                    "R" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_r,
                    "T" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_y,
                    "U" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_u,
                    "I" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_i,
                    "O" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_p,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_s,
                    "D" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_g,
                    "H" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_h,
                    "J" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_j,
                    "K" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_l,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_z,
                    "X" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_x,
                    "C" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_c,
                    "V" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_v,
                    "B" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_n,
                    "M" to R.drawable.guileless_bopomofo_keyboard_eten26_qwerty_m,
                ) + function(PUNCTUATION),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, if (wide) 5f else 3f),
                    function(BACKSPACE),
                    function(ENTER, if (wide) 1f else 1.5f),
                ),
            )
        )
    }

    private fun hsu(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        return KeyboardSpec(
            listOf(
                characterRow(
                    "W" to R.drawable.guileless_bopomofo_keyboard_ao,
                    "E" to R.drawable.guileless_bopomofo_keyboard_hsu_yi_ie,
                    "R" to R.drawable.guileless_bopomofo_keyboard_ri,
                    "T" to R.drawable.guileless_bopomofo_keyboard_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_a,
                    "U" to R.drawable.guileless_bopomofo_keyboard_yu,
                    "I" to R.drawable.guileless_bopomofo_keyboard_ai,
                    "O" to R.drawable.guileless_bopomofo_keyboard_ou,
                    "P" to R.drawable.guileless_bopomofo_keyboard_p,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_hsu_ci_ei,
                    "S" to R.drawable.guileless_bopomofo_keyboard_hsu_si,
                    "D" to R.drawable.guileless_bopomofo_keyboard_hsu_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_hsu_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_hsu_g_e,
                    "H" to R.drawable.guileless_bopomofo_keyboard_hsu_h_o,
                    "J" to R.drawable.guileless_bopomofo_keyboard_hsu_j_zhi,
                    "K" to R.drawable.guileless_bopomofo_keyboard_hsu_k_ang,
                    "L" to R.drawable.guileless_bopomofo_keyboard_hsu_l_eng_er,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_zi,
                    "X" to R.drawable.guileless_bopomofo_keyboard_wu,
                    "C" to R.drawable.guileless_bopomofo_keyboard_hsu_x_shi,
                    "V" to R.drawable.guileless_bopomofo_keyboard_hsu_q_chi,
                    "B" to R.drawable.guileless_bopomofo_keyboard_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_hsu_n_en,
                    "M" to R.drawable.guileless_bopomofo_keyboard_hsu_m_an,
                ) + function(PUNCTUATION),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, if (wide) 5f else 3f),
                    function(BACKSPACE),
                    function(ENTER, if (wide) 1f else 1.5f),
                ),
            )
        )
    }

    private fun hsuQwerty(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        return KeyboardSpec(
            listOf(
                characterRow(
                    "W" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_w,
                    "E" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_e,
                    "R" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_r,
                    "T" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_y,
                    "U" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_u,
                    "I" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_i,
                    "O" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_p,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_s,
                    "D" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_g,
                    "H" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_h,
                    "J" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_j,
                    "K" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_l,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_z,
                    "X" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_x,
                    "C" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_c,
                    "V" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_v,
                    "B" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_n,
                    "M" to R.drawable.guileless_bopomofo_keyboard_hsu_qwerty_m,
                ) + function(PUNCTUATION),
                listOf(
                    function(MODE_SWITCH_TO_ENGLISH),
                    function(SYMBOL),
                    function(IME_SWITCH),
                    function(SPACE, if (wide) 5f else 3f),
                    function(BACKSPACE),
                    function(ENTER, if (wide) 1f else 1.5f),
                ),
            )
        )
    }

    private fun qwerty(variant: KeyboardVariant): KeyboardSpec {
        val wide = variant != KeyboardVariant.DEFAULT
        val characterRows = if (wide) {
            listOf(
                characterRow(
                    "GRAVE" to R.drawable.guileless_bopomofo_keyboard_qwerty_grave,
                    "1" to R.drawable.guileless_bopomofo_keyboard_qwerty_1,
                    "2" to R.drawable.guileless_bopomofo_keyboard_qwerty_2,
                    "3" to R.drawable.guileless_bopomofo_keyboard_qwerty_3,
                    "4" to R.drawable.guileless_bopomofo_keyboard_qwerty_4,
                    "5" to R.drawable.guileless_bopomofo_keyboard_qwerty_5,
                    "6" to R.drawable.guileless_bopomofo_keyboard_qwerty_6,
                    "7" to R.drawable.guileless_bopomofo_keyboard_qwerty_7,
                    "8" to R.drawable.guileless_bopomofo_keyboard_qwerty_8,
                    "9" to R.drawable.guileless_bopomofo_keyboard_qwerty_9,
                    "0" to R.drawable.guileless_bopomofo_keyboard_qwerty_0,
                    "MINUS" to R.drawable.guileless_bopomofo_keyboard_qwerty_minus,
                    "EQUALS" to R.drawable.guileless_bopomofo_keyboard_qwerty_equal,
                ),
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_qwerty_q,
                    "W" to R.drawable.guileless_bopomofo_keyboard_qwerty_w,
                    "E" to R.drawable.guileless_bopomofo_keyboard_qwerty_e,
                    "R" to R.drawable.guileless_bopomofo_keyboard_qwerty_r,
                    "T" to R.drawable.guileless_bopomofo_keyboard_qwerty_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_qwerty_y,
                    "U" to R.drawable.guileless_bopomofo_keyboard_qwerty_u,
                    "I" to R.drawable.guileless_bopomofo_keyboard_qwerty_i,
                    "O" to R.drawable.guileless_bopomofo_keyboard_qwerty_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_qwerty_p,
                    "LEFT_BRACKET" to R.drawable.guileless_bopomofo_keyboard_qwerty_left_bracket,
                    "RIGHT_BRACKET" to R.drawable.guileless_bopomofo_keyboard_qwerty_right_bracket,
                    "BACKSLASH" to R.drawable.guileless_bopomofo_keyboard_qwerty_backslash,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_qwerty_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_qwerty_s,
                    "D" to R.drawable.guileless_bopomofo_keyboard_qwerty_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_qwerty_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_qwerty_g,
                    "H" to R.drawable.guileless_bopomofo_keyboard_qwerty_h,
                    "J" to R.drawable.guileless_bopomofo_keyboard_qwerty_j,
                    "K" to R.drawable.guileless_bopomofo_keyboard_qwerty_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_qwerty_l,
                    "SEMICOLON" to R.drawable.guileless_bopomofo_keyboard_qwerty_semicolon,
                    "APOSTROPHE" to R.drawable.guileless_bopomofo_keyboard_qwerty_apostrophe,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_qwerty_z,
                    "X" to R.drawable.guileless_bopomofo_keyboard_qwerty_x,
                    "C" to R.drawable.guileless_bopomofo_keyboard_qwerty_c,
                    "V" to R.drawable.guileless_bopomofo_keyboard_qwerty_v,
                    "B" to R.drawable.guileless_bopomofo_keyboard_qwerty_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_qwerty_n,
                    "M" to R.drawable.guileless_bopomofo_keyboard_qwerty_m,
                    "COMMA" to R.drawable.guileless_bopomofo_keyboard_qwerty_comma,
                    "PERIOD" to R.drawable.guileless_bopomofo_keyboard_qwerty_period,
                    "SLASH" to R.drawable.guileless_bopomofo_keyboard_qwerty_slash,
                ),
            )
        } else {
            listOf(
                characterRow(
                    "GRAVE" to R.drawable.guileless_bopomofo_keyboard_qwerty_grave,
                    "BACKSLASH" to R.drawable.guileless_bopomofo_keyboard_qwerty_backslash,
                    "LEFT_BRACKET" to R.drawable.guileless_bopomofo_keyboard_qwerty_left_bracket,
                    "RIGHT_BRACKET" to R.drawable.guileless_bopomofo_keyboard_qwerty_right_bracket,
                    "MINUS" to R.drawable.guileless_bopomofo_keyboard_qwerty_minus,
                    "EQUALS" to R.drawable.guileless_bopomofo_keyboard_qwerty_equal,
                    "APOSTROPHE" to R.drawable.guileless_bopomofo_keyboard_qwerty_apostrophe,
                    "SEMICOLON" to R.drawable.guileless_bopomofo_keyboard_qwerty_semicolon,
                    "SLASH" to R.drawable.guileless_bopomofo_keyboard_qwerty_slash,
                ),
                characterRow(
                    "1" to R.drawable.guileless_bopomofo_keyboard_qwerty_1,
                    "2" to R.drawable.guileless_bopomofo_keyboard_qwerty_2,
                    "3" to R.drawable.guileless_bopomofo_keyboard_qwerty_3,
                    "4" to R.drawable.guileless_bopomofo_keyboard_qwerty_4,
                    "5" to R.drawable.guileless_bopomofo_keyboard_qwerty_5,
                    "6" to R.drawable.guileless_bopomofo_keyboard_qwerty_6,
                    "7" to R.drawable.guileless_bopomofo_keyboard_qwerty_7,
                    "8" to R.drawable.guileless_bopomofo_keyboard_qwerty_8,
                    "9" to R.drawable.guileless_bopomofo_keyboard_qwerty_9,
                    "0" to R.drawable.guileless_bopomofo_keyboard_qwerty_0,
                ),
                characterRow(
                    "Q" to R.drawable.guileless_bopomofo_keyboard_qwerty_q,
                    "W" to R.drawable.guileless_bopomofo_keyboard_qwerty_w,
                    "E" to R.drawable.guileless_bopomofo_keyboard_qwerty_e,
                    "R" to R.drawable.guileless_bopomofo_keyboard_qwerty_r,
                    "T" to R.drawable.guileless_bopomofo_keyboard_qwerty_t,
                    "Y" to R.drawable.guileless_bopomofo_keyboard_qwerty_y,
                    "U" to R.drawable.guileless_bopomofo_keyboard_qwerty_u,
                    "I" to R.drawable.guileless_bopomofo_keyboard_qwerty_i,
                    "O" to R.drawable.guileless_bopomofo_keyboard_qwerty_o,
                    "P" to R.drawable.guileless_bopomofo_keyboard_qwerty_p,
                ),
                characterRow(
                    "A" to R.drawable.guileless_bopomofo_keyboard_qwerty_a,
                    "S" to R.drawable.guileless_bopomofo_keyboard_qwerty_s,
                    "D" to R.drawable.guileless_bopomofo_keyboard_qwerty_d,
                    "F" to R.drawable.guileless_bopomofo_keyboard_qwerty_f,
                    "G" to R.drawable.guileless_bopomofo_keyboard_qwerty_g,
                    "H" to R.drawable.guileless_bopomofo_keyboard_qwerty_h,
                    "J" to R.drawable.guileless_bopomofo_keyboard_qwerty_j,
                    "K" to R.drawable.guileless_bopomofo_keyboard_qwerty_k,
                    "L" to R.drawable.guileless_bopomofo_keyboard_qwerty_l,
                ),
                characterRow(
                    "Z" to R.drawable.guileless_bopomofo_keyboard_qwerty_z,
                    "X" to R.drawable.guileless_bopomofo_keyboard_qwerty_x,
                    "C" to R.drawable.guileless_bopomofo_keyboard_qwerty_c,
                    "V" to R.drawable.guileless_bopomofo_keyboard_qwerty_v,
                    "B" to R.drawable.guileless_bopomofo_keyboard_qwerty_b,
                    "N" to R.drawable.guileless_bopomofo_keyboard_qwerty_n,
                    "M" to R.drawable.guileless_bopomofo_keyboard_qwerty_m,
                    "COMMA" to R.drawable.guileless_bopomofo_keyboard_qwerty_comma,
                    "PERIOD" to R.drawable.guileless_bopomofo_keyboard_qwerty_period,
                ),
            )
        }
        return KeyboardSpec(
            characterRows + listOf(
                listOf(
                    function(MODE_SWITCH_TO_BOPOMOFO),
                    function(SHIFT),
                    function(SPACE, if (wide) 7f else 3f),
                    function(BACKSPACE),
                    function(ENTER, 1.5f),
                ),
            )
        )
    }

    private fun characterRow(vararg keys: Pair<String, Int>): List<KeySpec> =
        keys.map { (keyCode, icon) -> character(keyCode, icon) }

    private fun character(keyCode: String, @DrawableRes icon: Int): KeySpec =
        KeySpec.Character("KEYCODE_$keyCode", icon)

    private fun function(key: FunctionKey, weight: Float = 1f): KeySpec = KeySpec.Function(key, weight)
}
