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

import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins every on-screen keyboard to what the XML layouts it replaced held: for each keyboard
 * and screen variant, the keys row by row, as `KEYCODE/icon/weight` for a character key and
 * `FUNCTION/weight` for the others. The comment above each entry names the layout file
 * Android would have picked for it.
 */
class KeyboardSpecsTest {
    @Test
    fun everyKeyboard_matchesTheXmlLayoutItReplaced() {
        EXPECTED.forEach { (keyboard, variant, rows) ->
            assertEquals("$keyboard $variant", parse(rows), KeyboardSpecs.of(keyboard, variant))
        }
    }

    @Test
    fun everyKeyboardAndVariant_isCovered() {
        val covered = EXPECTED.map { (keyboard, variant, _) -> keyboard to variant }.toSet()
        SoftKeyboard.entries.filterNot { it.isKeypad }.forEach { keyboard ->
            KeyboardVariant.entries.forEach { variant ->
                assertTrue("$keyboard $variant has no expectation", keyboard to variant in covered)
            }
        }
        assertEquals(SoftKeyboard.entries.count { !it.isKeypad } * KeyboardVariant.entries.size, covered.size)
    }

    @Test
    fun variant_smallestWidthWinsOverOrientation() {
        assertEquals(KeyboardVariant.DEFAULT, KeyboardVariant.of(smallestScreenWidthDp = 411, isLandscape = false))
        assertEquals(KeyboardVariant.LANDSCAPE, KeyboardVariant.of(smallestScreenWidthDp = 411, isLandscape = true))
        assertEquals(KeyboardVariant.LARGE_SCREEN, KeyboardVariant.of(smallestScreenWidthDp = 600, isLandscape = false))
        assertEquals(KeyboardVariant.LARGE_SCREEN, KeyboardVariant.of(smallestScreenWidthDp = 800, isLandscape = true))
    }

    private fun parse(rows: List<String>): KeyboardSpec = KeyboardSpec(
        rows.map { row -> row.split(' ').map(::parseKey) }
    )

    private fun parseKey(token: String): KeySpec {
        val parts = token.split('/')
        return when (parts.size) {
            3 -> KeySpec.Character("KEYCODE_${parts[0]}", drawable(parts[1]), parts[2].toFloat())
            2 -> KeySpec.Function(FunctionKey.valueOf(parts[0]), parts[1].toFloat())
            else -> error("Malformed key: $token")
        }
    }

    private fun drawable(name: String): Int = R.drawable::class.java.getField(name).getInt(null)

    private companion object {
        val EXPECTED: List<Triple<SoftKeyboard, KeyboardVariant, List<String>>> = listOf(
        // layout/keyboard_dachen_layout.xml
        Triple(SoftKeyboard.DACHEN, KeyboardVariant.DEFAULT, listOf(
            "1/guileless_bopomofo_keyboard_b/1 2/guileless_bopomofo_keyboard_d/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 5/guileless_bopomofo_keyboard_zhi/1 6/guileless_bopomofo_keyboard_tone_2/1 7/guileless_bopomofo_keyboard_tone_light/1 8/guileless_bopomofo_keyboard_a/1 9/guileless_bopomofo_keyboard_ai/1 0/guileless_bopomofo_keyboard_an/1",
            "Q/guileless_bopomofo_keyboard_p/1 W/guileless_bopomofo_keyboard_t/1 E/guileless_bopomofo_keyboard_g/1 R/guileless_bopomofo_keyboard_j/1 T/guileless_bopomofo_keyboard_chi/1 Y/guileless_bopomofo_keyboard_zi/1 U/guileless_bopomofo_keyboard_yi/1 I/guileless_bopomofo_keyboard_o/1 O/guileless_bopomofo_keyboard_ei/1 P/guileless_bopomofo_keyboard_en/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_k/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_ao/1 SEMICOLON/guileless_bopomofo_keyboard_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_ri/1 N/guileless_bopomofo_keyboard_si/1 M/guileless_bopomofo_keyboard_yu/1 COMMA/guileless_bopomofo_keyboard_ie/1 PERIOD/guileless_bopomofo_keyboard_ou/1 SLASH/guileless_bopomofo_keyboard_eng/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/2 MINUS/guileless_bopomofo_keyboard_er/1 PUNCTUATION/1 BACKSPACE/1 ENTER/1.5",
        )),
        // layout/keyboard_dachen_layout.xml
        Triple(SoftKeyboard.DACHEN, KeyboardVariant.LANDSCAPE, listOf(
            "1/guileless_bopomofo_keyboard_b/1 2/guileless_bopomofo_keyboard_d/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 5/guileless_bopomofo_keyboard_zhi/1 6/guileless_bopomofo_keyboard_tone_2/1 7/guileless_bopomofo_keyboard_tone_light/1 8/guileless_bopomofo_keyboard_a/1 9/guileless_bopomofo_keyboard_ai/1 0/guileless_bopomofo_keyboard_an/1",
            "Q/guileless_bopomofo_keyboard_p/1 W/guileless_bopomofo_keyboard_t/1 E/guileless_bopomofo_keyboard_g/1 R/guileless_bopomofo_keyboard_j/1 T/guileless_bopomofo_keyboard_chi/1 Y/guileless_bopomofo_keyboard_zi/1 U/guileless_bopomofo_keyboard_yi/1 I/guileless_bopomofo_keyboard_o/1 O/guileless_bopomofo_keyboard_ei/1 P/guileless_bopomofo_keyboard_en/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_k/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_ao/1 SEMICOLON/guileless_bopomofo_keyboard_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_ri/1 N/guileless_bopomofo_keyboard_si/1 M/guileless_bopomofo_keyboard_yu/1 COMMA/guileless_bopomofo_keyboard_ie/1 PERIOD/guileless_bopomofo_keyboard_ou/1 SLASH/guileless_bopomofo_keyboard_eng/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/2 MINUS/guileless_bopomofo_keyboard_er/1 PUNCTUATION/1 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-sw600dp/keyboard_dachen_layout.xml
        Triple(SoftKeyboard.DACHEN, KeyboardVariant.LARGE_SCREEN, listOf(
            "1/guileless_bopomofo_keyboard_b/1 2/guileless_bopomofo_keyboard_d/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 5/guileless_bopomofo_keyboard_zhi/1 6/guileless_bopomofo_keyboard_tone_2/1 7/guileless_bopomofo_keyboard_tone_light/1 8/guileless_bopomofo_keyboard_a/1 9/guileless_bopomofo_keyboard_ai/1 0/guileless_bopomofo_keyboard_an/1",
            "Q/guileless_bopomofo_keyboard_p/1 W/guileless_bopomofo_keyboard_t/1 E/guileless_bopomofo_keyboard_g/1 R/guileless_bopomofo_keyboard_j/1 T/guileless_bopomofo_keyboard_chi/1 Y/guileless_bopomofo_keyboard_zi/1 U/guileless_bopomofo_keyboard_yi/1 I/guileless_bopomofo_keyboard_o/1 O/guileless_bopomofo_keyboard_ei/1 P/guileless_bopomofo_keyboard_en/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_k/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_ao/1 SEMICOLON/guileless_bopomofo_keyboard_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_ri/1 N/guileless_bopomofo_keyboard_si/1 M/guileless_bopomofo_keyboard_yu/1 COMMA/guileless_bopomofo_keyboard_ie/1 PERIOD/guileless_bopomofo_keyboard_ou/1 SLASH/guileless_bopomofo_keyboard_eng/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/2 MINUS/guileless_bopomofo_keyboard_er/1 PUNCTUATION/1 BACKSPACE/1 ENTER/1",
        )),
        // layout/keyboard_dachen_cp26_layout.xml
        Triple(SoftKeyboard.DACHEN_CP26, KeyboardVariant.DEFAULT, listOf(
            "Q/guileless_bopomofo_keyboard_dachen_cp26_b_p/1 W/guileless_bopomofo_keyboard_dachen_cp26_d_t/1 E/guileless_bopomofo_keyboard_dachen_cp26_g_tone2/1 R/guileless_bopomofo_keyboard_dachen_cp26_j_tone3/1 T/guileless_bopomofo_keyboard_dachen_cp26_zhi_chi/1 Y/guileless_bopomofo_keyboard_dachen_cp26_zi_tone_light/1 U/guileless_bopomofo_keyboard_dachen_cp26_yi_a/1 I/guileless_bopomofo_keyboard_dachen_cp26_o_ai/1 O/guileless_bopomofo_keyboard_dachen_cp26_ei_an/1 P/guileless_bopomofo_keyboard_dachen_cp26_en_er/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_dachen_cp26_k_tone4/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_dachen_cp26_ao_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_dachen_cp26_ri_ie/1 N/guileless_bopomofo_keyboard_dachen_cp26_si_eng/1 M/guileless_bopomofo_keyboard_dachen_cp26_yu_ou/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1.5 ENTER/1.5",
        )),
        // layout-land/keyboard_dachen_cp26_layout.xml
        Triple(SoftKeyboard.DACHEN_CP26, KeyboardVariant.LANDSCAPE, listOf(
            "Q/guileless_bopomofo_keyboard_dachen_cp26_b_p/1 W/guileless_bopomofo_keyboard_dachen_cp26_d_t/1 E/guileless_bopomofo_keyboard_dachen_cp26_g_tone2/1 R/guileless_bopomofo_keyboard_dachen_cp26_j_tone3/1 T/guileless_bopomofo_keyboard_dachen_cp26_zhi_chi/1 Y/guileless_bopomofo_keyboard_dachen_cp26_zi_tone_light/1 U/guileless_bopomofo_keyboard_dachen_cp26_yi_a/1 I/guileless_bopomofo_keyboard_dachen_cp26_o_ai/1 O/guileless_bopomofo_keyboard_dachen_cp26_ei_an/1 P/guileless_bopomofo_keyboard_dachen_cp26_en_er/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_dachen_cp26_k_tone4/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_dachen_cp26_ao_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_dachen_cp26_ri_ie/1 N/guileless_bopomofo_keyboard_dachen_cp26_si_eng/1 M/guileless_bopomofo_keyboard_dachen_cp26_yu_ou/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1.5 ENTER/1.5",
        )),
        // layout-sw600dp/keyboard_dachen_cp26_layout.xml
        Triple(SoftKeyboard.DACHEN_CP26, KeyboardVariant.LARGE_SCREEN, listOf(
            "Q/guileless_bopomofo_keyboard_dachen_cp26_b_p/1 W/guileless_bopomofo_keyboard_dachen_cp26_d_t/1 E/guileless_bopomofo_keyboard_dachen_cp26_g_tone2/1 R/guileless_bopomofo_keyboard_dachen_cp26_j_tone3/1 T/guileless_bopomofo_keyboard_dachen_cp26_zhi_chi/1 Y/guileless_bopomofo_keyboard_dachen_cp26_zi_tone_light/1 U/guileless_bopomofo_keyboard_dachen_cp26_yi_a/1 I/guileless_bopomofo_keyboard_dachen_cp26_o_ai/1 O/guileless_bopomofo_keyboard_dachen_cp26_ei_an/1 P/guileless_bopomofo_keyboard_dachen_cp26_en_er/1",
            "A/guileless_bopomofo_keyboard_m/1 S/guileless_bopomofo_keyboard_n/1 D/guileless_bopomofo_keyboard_dachen_cp26_k_tone4/1 F/guileless_bopomofo_keyboard_q/1 G/guileless_bopomofo_keyboard_shi/1 H/guileless_bopomofo_keyboard_ci/1 J/guileless_bopomofo_keyboard_wu/1 K/guileless_bopomofo_keyboard_e/1 L/guileless_bopomofo_keyboard_dachen_cp26_ao_ang/1",
            "Z/guileless_bopomofo_keyboard_f/1 X/guileless_bopomofo_keyboard_l/1 C/guileless_bopomofo_keyboard_h/1 V/guileless_bopomofo_keyboard_x/1 B/guileless_bopomofo_keyboard_dachen_cp26_ri_ie/1 N/guileless_bopomofo_keyboard_dachen_cp26_si_eng/1 M/guileless_bopomofo_keyboard_dachen_cp26_yu_ou/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1.5 ENTER/1.5",
        )),
        // layout/keyboard_et41_layout.xml
        Triple(SoftKeyboard.ET41, KeyboardVariant.DEFAULT, listOf(
            "1/guileless_bopomofo_keyboard_tone_light/1 2/guileless_bopomofo_keyboard_tone_2/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 7/guileless_bopomofo_keyboard_q/1 8/guileless_bopomofo_keyboard_an/1 9/guileless_bopomofo_keyboard_en/1 0/guileless_bopomofo_keyboard_ang/1 MINUS/guileless_bopomofo_keyboard_eng/1 EQUALS/guileless_bopomofo_keyboard_er/1",
            "Q/guileless_bopomofo_keyboard_ei/1 W/guileless_bopomofo_keyboard_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_ou/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_d/1 F/guileless_bopomofo_keyboard_f/1 G/guileless_bopomofo_keyboard_j/1 H/guileless_bopomofo_keyboard_h/1 J/guileless_bopomofo_keyboard_ri/1 K/guileless_bopomofo_keyboard_k/1 L/guileless_bopomofo_keyboard_l/1 SEMICOLON/guileless_bopomofo_keyboard_zi/1 APOSTROPHE/guileless_bopomofo_keyboard_ci/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_x/1 V/guileless_bopomofo_keyboard_g/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_n/1 M/guileless_bopomofo_keyboard_m/1 COMMA/guileless_bopomofo_keyboard_zhi/1 PERIOD/guileless_bopomofo_keyboard_chi/1 SLASH/guileless_bopomofo_keyboard_shi/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/2 PUNCTUATION/1 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_et41_layout.xml
        Triple(SoftKeyboard.ET41, KeyboardVariant.LANDSCAPE, listOf(
            "1/guileless_bopomofo_keyboard_tone_light/1 2/guileless_bopomofo_keyboard_tone_2/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 7/guileless_bopomofo_keyboard_q/1 8/guileless_bopomofo_keyboard_an/1 9/guileless_bopomofo_keyboard_en/1 0/guileless_bopomofo_keyboard_ang/1 MINUS/guileless_bopomofo_keyboard_eng/1 EQUALS/guileless_bopomofo_keyboard_er/1",
            "Q/guileless_bopomofo_keyboard_ei/1 W/guileless_bopomofo_keyboard_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_ou/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_d/1 F/guileless_bopomofo_keyboard_f/1 G/guileless_bopomofo_keyboard_j/1 H/guileless_bopomofo_keyboard_h/1 J/guileless_bopomofo_keyboard_ri/1 K/guileless_bopomofo_keyboard_k/1 L/guileless_bopomofo_keyboard_l/1 SEMICOLON/guileless_bopomofo_keyboard_zi/1 APOSTROPHE/guileless_bopomofo_keyboard_ci/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_x/1 V/guileless_bopomofo_keyboard_g/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_n/1 M/guileless_bopomofo_keyboard_m/1 COMMA/guileless_bopomofo_keyboard_zhi/1 PERIOD/guileless_bopomofo_keyboard_chi/1 SLASH/guileless_bopomofo_keyboard_shi/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 PUNCTUATION/1 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-sw600dp/keyboard_et41_layout.xml
        Triple(SoftKeyboard.ET41, KeyboardVariant.LARGE_SCREEN, listOf(
            "1/guileless_bopomofo_keyboard_tone_light/1 2/guileless_bopomofo_keyboard_tone_2/1 3/guileless_bopomofo_keyboard_tone_3/1 4/guileless_bopomofo_keyboard_tone_4/1 7/guileless_bopomofo_keyboard_q/1 8/guileless_bopomofo_keyboard_an/1 9/guileless_bopomofo_keyboard_en/1 0/guileless_bopomofo_keyboard_ang/1 MINUS/guileless_bopomofo_keyboard_eng/1 EQUALS/guileless_bopomofo_keyboard_er/1",
            "Q/guileless_bopomofo_keyboard_ei/1 W/guileless_bopomofo_keyboard_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_ou/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_d/1 F/guileless_bopomofo_keyboard_f/1 G/guileless_bopomofo_keyboard_j/1 H/guileless_bopomofo_keyboard_h/1 J/guileless_bopomofo_keyboard_ri/1 K/guileless_bopomofo_keyboard_k/1 L/guileless_bopomofo_keyboard_l/1 SEMICOLON/guileless_bopomofo_keyboard_zi/1 APOSTROPHE/guileless_bopomofo_keyboard_ci/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_x/1 V/guileless_bopomofo_keyboard_g/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_n/1 M/guileless_bopomofo_keyboard_m/1 COMMA/guileless_bopomofo_keyboard_zhi/1 PERIOD/guileless_bopomofo_keyboard_chi/1 SLASH/guileless_bopomofo_keyboard_shi/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 PUNCTUATION/1 BACKSPACE/1 ENTER/1.5",
        )),
        // layout/keyboard_et26_layout.xml
        Triple(SoftKeyboard.ET26, KeyboardVariant.DEFAULT, listOf(
            "Q/guileless_bopomofo_keyboard_et26_zi_ei/1 W/guileless_bopomofo_keyboard_et26_ci_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_et26_t_ang/1 Y/guileless_bopomofo_keyboard_chi/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_et26_p_ou/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_et26_d/1 F/guileless_bopomofo_keyboard_et26_f/1 G/guileless_bopomofo_keyboard_et26_j_zhi/1 H/guileless_bopomofo_keyboard_et26_h_er/1 J/guileless_bopomofo_keyboard_et26_ri/1 K/guileless_bopomofo_keyboard_et26_k/1 L/guileless_bopomofo_keyboard_et26_l_eng/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_et26_g_q/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_et26_layout.xml
        Triple(SoftKeyboard.ET26, KeyboardVariant.LANDSCAPE, listOf(
            "Q/guileless_bopomofo_keyboard_et26_zi_ei/1 W/guileless_bopomofo_keyboard_et26_ci_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_et26_t_ang/1 Y/guileless_bopomofo_keyboard_chi/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_et26_p_ou/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_et26_d/1 F/guileless_bopomofo_keyboard_et26_f/1 G/guileless_bopomofo_keyboard_et26_j_zhi/1 H/guileless_bopomofo_keyboard_et26_h_er/1 J/guileless_bopomofo_keyboard_et26_ri/1 K/guileless_bopomofo_keyboard_et26_k/1 L/guileless_bopomofo_keyboard_et26_l_eng/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_et26_g_q/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout-sw600dp/keyboard_et26_layout.xml
        Triple(SoftKeyboard.ET26, KeyboardVariant.LARGE_SCREEN, listOf(
            "Q/guileless_bopomofo_keyboard_et26_zi_ei/1 W/guileless_bopomofo_keyboard_et26_ci_ie/1 E/guileless_bopomofo_keyboard_yi/1 R/guileless_bopomofo_keyboard_e/1 T/guileless_bopomofo_keyboard_et26_t_ang/1 Y/guileless_bopomofo_keyboard_chi/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_o/1 P/guileless_bopomofo_keyboard_et26_p_ou/1",
            "A/guileless_bopomofo_keyboard_a/1 S/guileless_bopomofo_keyboard_si/1 D/guileless_bopomofo_keyboard_et26_d/1 F/guileless_bopomofo_keyboard_et26_f/1 G/guileless_bopomofo_keyboard_et26_j_zhi/1 H/guileless_bopomofo_keyboard_et26_h_er/1 J/guileless_bopomofo_keyboard_et26_ri/1 K/guileless_bopomofo_keyboard_et26_k/1 L/guileless_bopomofo_keyboard_et26_l_eng/1",
            "Z/guileless_bopomofo_keyboard_ao/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_et26_g_q/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout/keyboard_et26_qwerty_layout.xml
        Triple(SoftKeyboard.ET26_QWERTY, KeyboardVariant.DEFAULT, listOf(
            "Q/guileless_bopomofo_keyboard_eten26_qwerty_q/1 W/guileless_bopomofo_keyboard_eten26_qwerty_w/1 E/guileless_bopomofo_keyboard_eten26_qwerty_e/1 R/guileless_bopomofo_keyboard_eten26_qwerty_r/1 T/guileless_bopomofo_keyboard_eten26_qwerty_t/1 Y/guileless_bopomofo_keyboard_eten26_qwerty_y/1 U/guileless_bopomofo_keyboard_eten26_qwerty_u/1 I/guileless_bopomofo_keyboard_eten26_qwerty_i/1 O/guileless_bopomofo_keyboard_eten26_qwerty_o/1 P/guileless_bopomofo_keyboard_eten26_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_eten26_qwerty_a/1 S/guileless_bopomofo_keyboard_eten26_qwerty_s/1 D/guileless_bopomofo_keyboard_eten26_qwerty_d/1 F/guileless_bopomofo_keyboard_eten26_qwerty_f/1 G/guileless_bopomofo_keyboard_eten26_qwerty_g/1 H/guileless_bopomofo_keyboard_eten26_qwerty_h/1 J/guileless_bopomofo_keyboard_eten26_qwerty_j/1 K/guileless_bopomofo_keyboard_eten26_qwerty_k/1 L/guileless_bopomofo_keyboard_eten26_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_eten26_qwerty_z/1 X/guileless_bopomofo_keyboard_eten26_qwerty_x/1 C/guileless_bopomofo_keyboard_eten26_qwerty_c/1 V/guileless_bopomofo_keyboard_eten26_qwerty_v/1 B/guileless_bopomofo_keyboard_eten26_qwerty_b/1 N/guileless_bopomofo_keyboard_eten26_qwerty_n/1 M/guileless_bopomofo_keyboard_eten26_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_et26_qwerty_layout.xml
        Triple(SoftKeyboard.ET26_QWERTY, KeyboardVariant.LANDSCAPE, listOf(
            "Q/guileless_bopomofo_keyboard_eten26_qwerty_q/1 W/guileless_bopomofo_keyboard_eten26_qwerty_w/1 E/guileless_bopomofo_keyboard_eten26_qwerty_e/1 R/guileless_bopomofo_keyboard_eten26_qwerty_r/1 T/guileless_bopomofo_keyboard_eten26_qwerty_t/1 Y/guileless_bopomofo_keyboard_eten26_qwerty_y/1 U/guileless_bopomofo_keyboard_eten26_qwerty_u/1 I/guileless_bopomofo_keyboard_eten26_qwerty_i/1 O/guileless_bopomofo_keyboard_eten26_qwerty_o/1 P/guileless_bopomofo_keyboard_eten26_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_eten26_qwerty_a/1 S/guileless_bopomofo_keyboard_eten26_qwerty_s/1 D/guileless_bopomofo_keyboard_eten26_qwerty_d/1 F/guileless_bopomofo_keyboard_eten26_qwerty_f/1 G/guileless_bopomofo_keyboard_eten26_qwerty_g/1 H/guileless_bopomofo_keyboard_eten26_qwerty_h/1 J/guileless_bopomofo_keyboard_eten26_qwerty_j/1 K/guileless_bopomofo_keyboard_eten26_qwerty_k/1 L/guileless_bopomofo_keyboard_eten26_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_eten26_qwerty_z/1 X/guileless_bopomofo_keyboard_eten26_qwerty_x/1 C/guileless_bopomofo_keyboard_eten26_qwerty_c/1 V/guileless_bopomofo_keyboard_eten26_qwerty_v/1 B/guileless_bopomofo_keyboard_eten26_qwerty_b/1 N/guileless_bopomofo_keyboard_eten26_qwerty_n/1 M/guileless_bopomofo_keyboard_eten26_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout-sw600dp/keyboard_et26_qwerty_layout.xml
        Triple(SoftKeyboard.ET26_QWERTY, KeyboardVariant.LARGE_SCREEN, listOf(
            "Q/guileless_bopomofo_keyboard_eten26_qwerty_q/1 W/guileless_bopomofo_keyboard_eten26_qwerty_w/1 E/guileless_bopomofo_keyboard_eten26_qwerty_e/1 R/guileless_bopomofo_keyboard_eten26_qwerty_r/1 T/guileless_bopomofo_keyboard_eten26_qwerty_t/1 Y/guileless_bopomofo_keyboard_eten26_qwerty_y/1 U/guileless_bopomofo_keyboard_eten26_qwerty_u/1 I/guileless_bopomofo_keyboard_eten26_qwerty_i/1 O/guileless_bopomofo_keyboard_eten26_qwerty_o/1 P/guileless_bopomofo_keyboard_eten26_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_eten26_qwerty_a/1 S/guileless_bopomofo_keyboard_eten26_qwerty_s/1 D/guileless_bopomofo_keyboard_eten26_qwerty_d/1 F/guileless_bopomofo_keyboard_eten26_qwerty_f/1 G/guileless_bopomofo_keyboard_eten26_qwerty_g/1 H/guileless_bopomofo_keyboard_eten26_qwerty_h/1 J/guileless_bopomofo_keyboard_eten26_qwerty_j/1 K/guileless_bopomofo_keyboard_eten26_qwerty_k/1 L/guileless_bopomofo_keyboard_eten26_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_eten26_qwerty_z/1 X/guileless_bopomofo_keyboard_eten26_qwerty_x/1 C/guileless_bopomofo_keyboard_eten26_qwerty_c/1 V/guileless_bopomofo_keyboard_eten26_qwerty_v/1 B/guileless_bopomofo_keyboard_eten26_qwerty_b/1 N/guileless_bopomofo_keyboard_eten26_qwerty_n/1 M/guileless_bopomofo_keyboard_eten26_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout/keyboard_hsu_layout.xml
        Triple(SoftKeyboard.HSU, KeyboardVariant.DEFAULT, listOf(
            "W/guileless_bopomofo_keyboard_ao/1 E/guileless_bopomofo_keyboard_hsu_yi_ie/1 R/guileless_bopomofo_keyboard_ri/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_a/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_ou/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_hsu_ci_ei/1 S/guileless_bopomofo_keyboard_hsu_si/1 D/guileless_bopomofo_keyboard_hsu_d/1 F/guileless_bopomofo_keyboard_hsu_f/1 G/guileless_bopomofo_keyboard_hsu_g_e/1 H/guileless_bopomofo_keyboard_hsu_h_o/1 J/guileless_bopomofo_keyboard_hsu_j_zhi/1 K/guileless_bopomofo_keyboard_hsu_k_ang/1 L/guileless_bopomofo_keyboard_hsu_l_eng_er/1",
            "Z/guileless_bopomofo_keyboard_zi/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_hsu_q_chi/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_hsu_layout.xml
        Triple(SoftKeyboard.HSU, KeyboardVariant.LANDSCAPE, listOf(
            "W/guileless_bopomofo_keyboard_ao/1 E/guileless_bopomofo_keyboard_hsu_yi_ie/1 R/guileless_bopomofo_keyboard_ri/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_a/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_ou/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_hsu_ci_ei/1 S/guileless_bopomofo_keyboard_hsu_si/1 D/guileless_bopomofo_keyboard_hsu_d/1 F/guileless_bopomofo_keyboard_hsu_f/1 G/guileless_bopomofo_keyboard_hsu_g_e/1 H/guileless_bopomofo_keyboard_hsu_h_o/1 J/guileless_bopomofo_keyboard_hsu_j_zhi/1 K/guileless_bopomofo_keyboard_hsu_k_ang/1 L/guileless_bopomofo_keyboard_hsu_l_eng_er/1",
            "Z/guileless_bopomofo_keyboard_zi/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_hsu_q_chi/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout-sw600dp/keyboard_hsu_layout.xml
        Triple(SoftKeyboard.HSU, KeyboardVariant.LARGE_SCREEN, listOf(
            "W/guileless_bopomofo_keyboard_ao/1 E/guileless_bopomofo_keyboard_hsu_yi_ie/1 R/guileless_bopomofo_keyboard_ri/1 T/guileless_bopomofo_keyboard_t/1 Y/guileless_bopomofo_keyboard_a/1 U/guileless_bopomofo_keyboard_yu/1 I/guileless_bopomofo_keyboard_ai/1 O/guileless_bopomofo_keyboard_ou/1 P/guileless_bopomofo_keyboard_p/1",
            "A/guileless_bopomofo_keyboard_hsu_ci_ei/1 S/guileless_bopomofo_keyboard_hsu_si/1 D/guileless_bopomofo_keyboard_hsu_d/1 F/guileless_bopomofo_keyboard_hsu_f/1 G/guileless_bopomofo_keyboard_hsu_g_e/1 H/guileless_bopomofo_keyboard_hsu_h_o/1 J/guileless_bopomofo_keyboard_hsu_j_zhi/1 K/guileless_bopomofo_keyboard_hsu_k_ang/1 L/guileless_bopomofo_keyboard_hsu_l_eng_er/1",
            "Z/guileless_bopomofo_keyboard_zi/1 X/guileless_bopomofo_keyboard_wu/1 C/guileless_bopomofo_keyboard_hsu_x_shi/1 V/guileless_bopomofo_keyboard_hsu_q_chi/1 B/guileless_bopomofo_keyboard_b/1 N/guileless_bopomofo_keyboard_hsu_n_en/1 M/guileless_bopomofo_keyboard_hsu_m_an/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout/keyboard_hsu_qwerty_layout.xml
        Triple(SoftKeyboard.HSU_QWERTY, KeyboardVariant.DEFAULT, listOf(
            "W/guileless_bopomofo_keyboard_hsu_qwerty_w/1 E/guileless_bopomofo_keyboard_hsu_qwerty_e/1 R/guileless_bopomofo_keyboard_hsu_qwerty_r/1 T/guileless_bopomofo_keyboard_hsu_qwerty_t/1 Y/guileless_bopomofo_keyboard_hsu_qwerty_y/1 U/guileless_bopomofo_keyboard_hsu_qwerty_u/1 I/guileless_bopomofo_keyboard_hsu_qwerty_i/1 O/guileless_bopomofo_keyboard_hsu_qwerty_o/1 P/guileless_bopomofo_keyboard_hsu_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_hsu_qwerty_a/1 S/guileless_bopomofo_keyboard_hsu_qwerty_s/1 D/guileless_bopomofo_keyboard_hsu_qwerty_d/1 F/guileless_bopomofo_keyboard_hsu_qwerty_f/1 G/guileless_bopomofo_keyboard_hsu_qwerty_g/1 H/guileless_bopomofo_keyboard_hsu_qwerty_h/1 J/guileless_bopomofo_keyboard_hsu_qwerty_j/1 K/guileless_bopomofo_keyboard_hsu_qwerty_k/1 L/guileless_bopomofo_keyboard_hsu_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_hsu_qwerty_z/1 X/guileless_bopomofo_keyboard_hsu_qwerty_x/1 C/guileless_bopomofo_keyboard_hsu_qwerty_c/1 V/guileless_bopomofo_keyboard_hsu_qwerty_v/1 B/guileless_bopomofo_keyboard_hsu_qwerty_b/1 N/guileless_bopomofo_keyboard_hsu_qwerty_n/1 M/guileless_bopomofo_keyboard_hsu_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/3 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_hsu_qwerty_layout.xml
        Triple(SoftKeyboard.HSU_QWERTY, KeyboardVariant.LANDSCAPE, listOf(
            "W/guileless_bopomofo_keyboard_hsu_qwerty_w/1 E/guileless_bopomofo_keyboard_hsu_qwerty_e/1 R/guileless_bopomofo_keyboard_hsu_qwerty_r/1 T/guileless_bopomofo_keyboard_hsu_qwerty_t/1 Y/guileless_bopomofo_keyboard_hsu_qwerty_y/1 U/guileless_bopomofo_keyboard_hsu_qwerty_u/1 I/guileless_bopomofo_keyboard_hsu_qwerty_i/1 O/guileless_bopomofo_keyboard_hsu_qwerty_o/1 P/guileless_bopomofo_keyboard_hsu_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_hsu_qwerty_a/1 S/guileless_bopomofo_keyboard_hsu_qwerty_s/1 D/guileless_bopomofo_keyboard_hsu_qwerty_d/1 F/guileless_bopomofo_keyboard_hsu_qwerty_f/1 G/guileless_bopomofo_keyboard_hsu_qwerty_g/1 H/guileless_bopomofo_keyboard_hsu_qwerty_h/1 J/guileless_bopomofo_keyboard_hsu_qwerty_j/1 K/guileless_bopomofo_keyboard_hsu_qwerty_k/1 L/guileless_bopomofo_keyboard_hsu_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_hsu_qwerty_z/1 X/guileless_bopomofo_keyboard_hsu_qwerty_x/1 C/guileless_bopomofo_keyboard_hsu_qwerty_c/1 V/guileless_bopomofo_keyboard_hsu_qwerty_v/1 B/guileless_bopomofo_keyboard_hsu_qwerty_b/1 N/guileless_bopomofo_keyboard_hsu_qwerty_n/1 M/guileless_bopomofo_keyboard_hsu_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout-sw600dp/keyboard_hsu_qwerty_layout.xml
        Triple(SoftKeyboard.HSU_QWERTY, KeyboardVariant.LARGE_SCREEN, listOf(
            "W/guileless_bopomofo_keyboard_hsu_qwerty_w/1 E/guileless_bopomofo_keyboard_hsu_qwerty_e/1 R/guileless_bopomofo_keyboard_hsu_qwerty_r/1 T/guileless_bopomofo_keyboard_hsu_qwerty_t/1 Y/guileless_bopomofo_keyboard_hsu_qwerty_y/1 U/guileless_bopomofo_keyboard_hsu_qwerty_u/1 I/guileless_bopomofo_keyboard_hsu_qwerty_i/1 O/guileless_bopomofo_keyboard_hsu_qwerty_o/1 P/guileless_bopomofo_keyboard_hsu_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_hsu_qwerty_a/1 S/guileless_bopomofo_keyboard_hsu_qwerty_s/1 D/guileless_bopomofo_keyboard_hsu_qwerty_d/1 F/guileless_bopomofo_keyboard_hsu_qwerty_f/1 G/guileless_bopomofo_keyboard_hsu_qwerty_g/1 H/guileless_bopomofo_keyboard_hsu_qwerty_h/1 J/guileless_bopomofo_keyboard_hsu_qwerty_j/1 K/guileless_bopomofo_keyboard_hsu_qwerty_k/1 L/guileless_bopomofo_keyboard_hsu_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_hsu_qwerty_z/1 X/guileless_bopomofo_keyboard_hsu_qwerty_x/1 C/guileless_bopomofo_keyboard_hsu_qwerty_c/1 V/guileless_bopomofo_keyboard_hsu_qwerty_v/1 B/guileless_bopomofo_keyboard_hsu_qwerty_b/1 N/guileless_bopomofo_keyboard_hsu_qwerty_n/1 M/guileless_bopomofo_keyboard_hsu_qwerty_m/1 PUNCTUATION/1",
            "MODE_SWITCH_TO_ENGLISH/1 SYMBOL/1 IME_SWITCH/1 SPACE/5 BACKSPACE/1 ENTER/1",
        )),
        // layout/keyboard_qwerty_layout.xml
        Triple(SoftKeyboard.QWERTY, KeyboardVariant.DEFAULT, listOf(
            "GRAVE/guileless_bopomofo_keyboard_qwerty_grave/1 BACKSLASH/guileless_bopomofo_keyboard_qwerty_backslash/1 LEFT_BRACKET/guileless_bopomofo_keyboard_qwerty_left_bracket/1 RIGHT_BRACKET/guileless_bopomofo_keyboard_qwerty_right_bracket/1 MINUS/guileless_bopomofo_keyboard_qwerty_minus/1 EQUALS/guileless_bopomofo_keyboard_qwerty_equal/1 APOSTROPHE/guileless_bopomofo_keyboard_qwerty_apostrophe/1 SEMICOLON/guileless_bopomofo_keyboard_qwerty_semicolon/1 SLASH/guileless_bopomofo_keyboard_qwerty_slash/1",
            "1/guileless_bopomofo_keyboard_qwerty_1/1 2/guileless_bopomofo_keyboard_qwerty_2/1 3/guileless_bopomofo_keyboard_qwerty_3/1 4/guileless_bopomofo_keyboard_qwerty_4/1 5/guileless_bopomofo_keyboard_qwerty_5/1 6/guileless_bopomofo_keyboard_qwerty_6/1 7/guileless_bopomofo_keyboard_qwerty_7/1 8/guileless_bopomofo_keyboard_qwerty_8/1 9/guileless_bopomofo_keyboard_qwerty_9/1 0/guileless_bopomofo_keyboard_qwerty_0/1",
            "Q/guileless_bopomofo_keyboard_qwerty_q/1 W/guileless_bopomofo_keyboard_qwerty_w/1 E/guileless_bopomofo_keyboard_qwerty_e/1 R/guileless_bopomofo_keyboard_qwerty_r/1 T/guileless_bopomofo_keyboard_qwerty_t/1 Y/guileless_bopomofo_keyboard_qwerty_y/1 U/guileless_bopomofo_keyboard_qwerty_u/1 I/guileless_bopomofo_keyboard_qwerty_i/1 O/guileless_bopomofo_keyboard_qwerty_o/1 P/guileless_bopomofo_keyboard_qwerty_p/1",
            "A/guileless_bopomofo_keyboard_qwerty_a/1 S/guileless_bopomofo_keyboard_qwerty_s/1 D/guileless_bopomofo_keyboard_qwerty_d/1 F/guileless_bopomofo_keyboard_qwerty_f/1 G/guileless_bopomofo_keyboard_qwerty_g/1 H/guileless_bopomofo_keyboard_qwerty_h/1 J/guileless_bopomofo_keyboard_qwerty_j/1 K/guileless_bopomofo_keyboard_qwerty_k/1 L/guileless_bopomofo_keyboard_qwerty_l/1",
            "Z/guileless_bopomofo_keyboard_qwerty_z/1 X/guileless_bopomofo_keyboard_qwerty_x/1 C/guileless_bopomofo_keyboard_qwerty_c/1 V/guileless_bopomofo_keyboard_qwerty_v/1 B/guileless_bopomofo_keyboard_qwerty_b/1 N/guileless_bopomofo_keyboard_qwerty_n/1 M/guileless_bopomofo_keyboard_qwerty_m/1 COMMA/guileless_bopomofo_keyboard_qwerty_comma/1 PERIOD/guileless_bopomofo_keyboard_qwerty_period/1",
            "MODE_SWITCH_TO_BOPOMOFO/1 SHIFT/1 SPACE/3 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-land/keyboard_qwerty_layout.xml
        Triple(SoftKeyboard.QWERTY, KeyboardVariant.LANDSCAPE, listOf(
            "GRAVE/guileless_bopomofo_keyboard_qwerty_grave/1 1/guileless_bopomofo_keyboard_qwerty_1/1 2/guileless_bopomofo_keyboard_qwerty_2/1 3/guileless_bopomofo_keyboard_qwerty_3/1 4/guileless_bopomofo_keyboard_qwerty_4/1 5/guileless_bopomofo_keyboard_qwerty_5/1 6/guileless_bopomofo_keyboard_qwerty_6/1 7/guileless_bopomofo_keyboard_qwerty_7/1 8/guileless_bopomofo_keyboard_qwerty_8/1 9/guileless_bopomofo_keyboard_qwerty_9/1 0/guileless_bopomofo_keyboard_qwerty_0/1 MINUS/guileless_bopomofo_keyboard_qwerty_minus/1 EQUALS/guileless_bopomofo_keyboard_qwerty_equal/1",
            "Q/guileless_bopomofo_keyboard_qwerty_q/1 W/guileless_bopomofo_keyboard_qwerty_w/1 E/guileless_bopomofo_keyboard_qwerty_e/1 R/guileless_bopomofo_keyboard_qwerty_r/1 T/guileless_bopomofo_keyboard_qwerty_t/1 Y/guileless_bopomofo_keyboard_qwerty_y/1 U/guileless_bopomofo_keyboard_qwerty_u/1 I/guileless_bopomofo_keyboard_qwerty_i/1 O/guileless_bopomofo_keyboard_qwerty_o/1 P/guileless_bopomofo_keyboard_qwerty_p/1 LEFT_BRACKET/guileless_bopomofo_keyboard_qwerty_left_bracket/1 RIGHT_BRACKET/guileless_bopomofo_keyboard_qwerty_right_bracket/1 BACKSLASH/guileless_bopomofo_keyboard_qwerty_backslash/1",
            "A/guileless_bopomofo_keyboard_qwerty_a/1 S/guileless_bopomofo_keyboard_qwerty_s/1 D/guileless_bopomofo_keyboard_qwerty_d/1 F/guileless_bopomofo_keyboard_qwerty_f/1 G/guileless_bopomofo_keyboard_qwerty_g/1 H/guileless_bopomofo_keyboard_qwerty_h/1 J/guileless_bopomofo_keyboard_qwerty_j/1 K/guileless_bopomofo_keyboard_qwerty_k/1 L/guileless_bopomofo_keyboard_qwerty_l/1 SEMICOLON/guileless_bopomofo_keyboard_qwerty_semicolon/1 APOSTROPHE/guileless_bopomofo_keyboard_qwerty_apostrophe/1",
            "Z/guileless_bopomofo_keyboard_qwerty_z/1 X/guileless_bopomofo_keyboard_qwerty_x/1 C/guileless_bopomofo_keyboard_qwerty_c/1 V/guileless_bopomofo_keyboard_qwerty_v/1 B/guileless_bopomofo_keyboard_qwerty_b/1 N/guileless_bopomofo_keyboard_qwerty_n/1 M/guileless_bopomofo_keyboard_qwerty_m/1 COMMA/guileless_bopomofo_keyboard_qwerty_comma/1 PERIOD/guileless_bopomofo_keyboard_qwerty_period/1 SLASH/guileless_bopomofo_keyboard_qwerty_slash/1",
            "MODE_SWITCH_TO_BOPOMOFO/1 SHIFT/1 SPACE/7 BACKSPACE/1 ENTER/1.5",
        )),
        // layout-sw600dp/keyboard_qwerty_layout.xml
        Triple(SoftKeyboard.QWERTY, KeyboardVariant.LARGE_SCREEN, listOf(
            "GRAVE/guileless_bopomofo_keyboard_qwerty_grave/1 1/guileless_bopomofo_keyboard_qwerty_1/1 2/guileless_bopomofo_keyboard_qwerty_2/1 3/guileless_bopomofo_keyboard_qwerty_3/1 4/guileless_bopomofo_keyboard_qwerty_4/1 5/guileless_bopomofo_keyboard_qwerty_5/1 6/guileless_bopomofo_keyboard_qwerty_6/1 7/guileless_bopomofo_keyboard_qwerty_7/1 8/guileless_bopomofo_keyboard_qwerty_8/1 9/guileless_bopomofo_keyboard_qwerty_9/1 0/guileless_bopomofo_keyboard_qwerty_0/1 MINUS/guileless_bopomofo_keyboard_qwerty_minus/1 EQUALS/guileless_bopomofo_keyboard_qwerty_equal/1",
            "Q/guileless_bopomofo_keyboard_qwerty_q/1 W/guileless_bopomofo_keyboard_qwerty_w/1 E/guileless_bopomofo_keyboard_qwerty_e/1 R/guileless_bopomofo_keyboard_qwerty_r/1 T/guileless_bopomofo_keyboard_qwerty_t/1 Y/guileless_bopomofo_keyboard_qwerty_y/1 U/guileless_bopomofo_keyboard_qwerty_u/1 I/guileless_bopomofo_keyboard_qwerty_i/1 O/guileless_bopomofo_keyboard_qwerty_o/1 P/guileless_bopomofo_keyboard_qwerty_p/1 LEFT_BRACKET/guileless_bopomofo_keyboard_qwerty_left_bracket/1 RIGHT_BRACKET/guileless_bopomofo_keyboard_qwerty_right_bracket/1 BACKSLASH/guileless_bopomofo_keyboard_qwerty_backslash/1",
            "A/guileless_bopomofo_keyboard_qwerty_a/1 S/guileless_bopomofo_keyboard_qwerty_s/1 D/guileless_bopomofo_keyboard_qwerty_d/1 F/guileless_bopomofo_keyboard_qwerty_f/1 G/guileless_bopomofo_keyboard_qwerty_g/1 H/guileless_bopomofo_keyboard_qwerty_h/1 J/guileless_bopomofo_keyboard_qwerty_j/1 K/guileless_bopomofo_keyboard_qwerty_k/1 L/guileless_bopomofo_keyboard_qwerty_l/1 SEMICOLON/guileless_bopomofo_keyboard_qwerty_semicolon/1 APOSTROPHE/guileless_bopomofo_keyboard_qwerty_apostrophe/1",
            "Z/guileless_bopomofo_keyboard_qwerty_z/1 X/guileless_bopomofo_keyboard_qwerty_x/1 C/guileless_bopomofo_keyboard_qwerty_c/1 V/guileless_bopomofo_keyboard_qwerty_v/1 B/guileless_bopomofo_keyboard_qwerty_b/1 N/guileless_bopomofo_keyboard_qwerty_n/1 M/guileless_bopomofo_keyboard_qwerty_m/1 COMMA/guileless_bopomofo_keyboard_qwerty_comma/1 PERIOD/guileless_bopomofo_keyboard_qwerty_period/1 SLASH/guileless_bopomofo_keyboard_qwerty_slash/1",
            "MODE_SWITCH_TO_BOPOMOFO/1 SHIFT/1 SPACE/7 BACKSPACE/1 ENTER/1.5",
        )),
        )
    }
}
