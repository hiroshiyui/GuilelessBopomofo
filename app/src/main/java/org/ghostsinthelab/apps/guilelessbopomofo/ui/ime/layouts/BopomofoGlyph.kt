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

/**
 * Every Bopomofo symbol and tone mark a keyboard can type, with the key that types it on the
 * standard (Dachen) layout, the glyph drawn for it and the [symbol] itself, which is what a
 * screen reader says for it. The first tone has no symbol: it is the space key.
 */
enum class BopomofoGlyph(val dachenKeyCode: String, @param:DrawableRes val icon: Int, val symbol: String) {
    B("KEYCODE_1", R.drawable.guileless_bopomofo_keyboard_b, "ㄅ"),
    P("KEYCODE_Q", R.drawable.guileless_bopomofo_keyboard_p, "ㄆ"),
    M("KEYCODE_A", R.drawable.guileless_bopomofo_keyboard_m, "ㄇ"),
    F("KEYCODE_Z", R.drawable.guileless_bopomofo_keyboard_f, "ㄈ"),
    D("KEYCODE_2", R.drawable.guileless_bopomofo_keyboard_d, "ㄉ"),
    T("KEYCODE_W", R.drawable.guileless_bopomofo_keyboard_t, "ㄊ"),
    N("KEYCODE_S", R.drawable.guileless_bopomofo_keyboard_n, "ㄋ"),
    L("KEYCODE_X", R.drawable.guileless_bopomofo_keyboard_l, "ㄌ"),
    G("KEYCODE_E", R.drawable.guileless_bopomofo_keyboard_g, "ㄍ"),
    K("KEYCODE_D", R.drawable.guileless_bopomofo_keyboard_k, "ㄎ"),
    H("KEYCODE_C", R.drawable.guileless_bopomofo_keyboard_h, "ㄏ"),
    J("KEYCODE_R", R.drawable.guileless_bopomofo_keyboard_j, "ㄐ"),
    Q("KEYCODE_F", R.drawable.guileless_bopomofo_keyboard_q, "ㄑ"),
    X("KEYCODE_V", R.drawable.guileless_bopomofo_keyboard_x, "ㄒ"),
    ZHI("KEYCODE_5", R.drawable.guileless_bopomofo_keyboard_zhi, "ㄓ"),
    CHI("KEYCODE_T", R.drawable.guileless_bopomofo_keyboard_chi, "ㄔ"),
    SHI("KEYCODE_G", R.drawable.guileless_bopomofo_keyboard_shi, "ㄕ"),
    RI("KEYCODE_B", R.drawable.guileless_bopomofo_keyboard_ri, "ㄖ"),
    ZI("KEYCODE_Y", R.drawable.guileless_bopomofo_keyboard_zi, "ㄗ"),
    CI("KEYCODE_H", R.drawable.guileless_bopomofo_keyboard_ci, "ㄘ"),
    SI("KEYCODE_N", R.drawable.guileless_bopomofo_keyboard_si, "ㄙ"),
    YI("KEYCODE_U", R.drawable.guileless_bopomofo_keyboard_yi, "ㄧ"),
    WU("KEYCODE_J", R.drawable.guileless_bopomofo_keyboard_wu, "ㄨ"),
    YU("KEYCODE_M", R.drawable.guileless_bopomofo_keyboard_yu, "ㄩ"),
    A("KEYCODE_8", R.drawable.guileless_bopomofo_keyboard_a, "ㄚ"),
    O("KEYCODE_I", R.drawable.guileless_bopomofo_keyboard_o, "ㄛ"),
    E("KEYCODE_K", R.drawable.guileless_bopomofo_keyboard_e, "ㄜ"),
    IE("KEYCODE_COMMA", R.drawable.guileless_bopomofo_keyboard_ie, "ㄝ"),
    AI("KEYCODE_9", R.drawable.guileless_bopomofo_keyboard_ai, "ㄞ"),
    EI("KEYCODE_O", R.drawable.guileless_bopomofo_keyboard_ei, "ㄟ"),
    AO("KEYCODE_L", R.drawable.guileless_bopomofo_keyboard_ao, "ㄠ"),
    OU("KEYCODE_PERIOD", R.drawable.guileless_bopomofo_keyboard_ou, "ㄡ"),
    AN("KEYCODE_0", R.drawable.guileless_bopomofo_keyboard_an, "ㄢ"),
    EN("KEYCODE_P", R.drawable.guileless_bopomofo_keyboard_en, "ㄣ"),
    ANG("KEYCODE_SEMICOLON", R.drawable.guileless_bopomofo_keyboard_ang, "ㄤ"),
    ENG("KEYCODE_SLASH", R.drawable.guileless_bopomofo_keyboard_eng, "ㄥ"),
    ER("KEYCODE_MINUS", R.drawable.guileless_bopomofo_keyboard_er, "ㄦ"),
    TONE_2("KEYCODE_6", R.drawable.guileless_bopomofo_keyboard_tone_2, "ˊ"),
    TONE_3("KEYCODE_3", R.drawable.guileless_bopomofo_keyboard_tone_3, "ˇ"),
    TONE_4("KEYCODE_4", R.drawable.guileless_bopomofo_keyboard_tone_4, "ˋ"),
    TONE_LIGHT("KEYCODE_7", R.drawable.guileless_bopomofo_keyboard_tone_light, "˙"),
}
