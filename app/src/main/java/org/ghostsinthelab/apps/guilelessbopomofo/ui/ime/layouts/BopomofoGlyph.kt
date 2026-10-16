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
 * standard (Dachen) layout and the glyph drawn for it. The first tone has no symbol: it is
 * the space key.
 */
enum class BopomofoGlyph(val dachenKeyCode: String, @param:DrawableRes val icon: Int) {
    B("KEYCODE_1", R.drawable.guileless_bopomofo_keyboard_b),
    P("KEYCODE_Q", R.drawable.guileless_bopomofo_keyboard_p),
    M("KEYCODE_A", R.drawable.guileless_bopomofo_keyboard_m),
    F("KEYCODE_Z", R.drawable.guileless_bopomofo_keyboard_f),
    D("KEYCODE_2", R.drawable.guileless_bopomofo_keyboard_d),
    T("KEYCODE_W", R.drawable.guileless_bopomofo_keyboard_t),
    N("KEYCODE_S", R.drawable.guileless_bopomofo_keyboard_n),
    L("KEYCODE_X", R.drawable.guileless_bopomofo_keyboard_l),
    G("KEYCODE_E", R.drawable.guileless_bopomofo_keyboard_g),
    K("KEYCODE_D", R.drawable.guileless_bopomofo_keyboard_k),
    H("KEYCODE_C", R.drawable.guileless_bopomofo_keyboard_h),
    J("KEYCODE_R", R.drawable.guileless_bopomofo_keyboard_j),
    Q("KEYCODE_F", R.drawable.guileless_bopomofo_keyboard_q),
    X("KEYCODE_V", R.drawable.guileless_bopomofo_keyboard_x),
    ZHI("KEYCODE_5", R.drawable.guileless_bopomofo_keyboard_zhi),
    CHI("KEYCODE_T", R.drawable.guileless_bopomofo_keyboard_chi),
    SHI("KEYCODE_G", R.drawable.guileless_bopomofo_keyboard_shi),
    RI("KEYCODE_B", R.drawable.guileless_bopomofo_keyboard_ri),
    ZI("KEYCODE_Y", R.drawable.guileless_bopomofo_keyboard_zi),
    CI("KEYCODE_H", R.drawable.guileless_bopomofo_keyboard_ci),
    SI("KEYCODE_N", R.drawable.guileless_bopomofo_keyboard_si),
    YI("KEYCODE_U", R.drawable.guileless_bopomofo_keyboard_yi),
    WU("KEYCODE_J", R.drawable.guileless_bopomofo_keyboard_wu),
    YU("KEYCODE_M", R.drawable.guileless_bopomofo_keyboard_yu),
    A("KEYCODE_8", R.drawable.guileless_bopomofo_keyboard_a),
    O("KEYCODE_I", R.drawable.guileless_bopomofo_keyboard_o),
    E("KEYCODE_K", R.drawable.guileless_bopomofo_keyboard_e),
    IE("KEYCODE_COMMA", R.drawable.guileless_bopomofo_keyboard_ie),
    AI("KEYCODE_9", R.drawable.guileless_bopomofo_keyboard_ai),
    EI("KEYCODE_O", R.drawable.guileless_bopomofo_keyboard_ei),
    AO("KEYCODE_L", R.drawable.guileless_bopomofo_keyboard_ao),
    OU("KEYCODE_PERIOD", R.drawable.guileless_bopomofo_keyboard_ou),
    AN("KEYCODE_0", R.drawable.guileless_bopomofo_keyboard_an),
    EN("KEYCODE_P", R.drawable.guileless_bopomofo_keyboard_en),
    ANG("KEYCODE_SEMICOLON", R.drawable.guileless_bopomofo_keyboard_ang),
    ENG("KEYCODE_SLASH", R.drawable.guileless_bopomofo_keyboard_eng),
    ER("KEYCODE_MINUS", R.drawable.guileless_bopomofo_keyboard_er),
    TONE_2("KEYCODE_6", R.drawable.guileless_bopomofo_keyboard_tone_2),
    TONE_3("KEYCODE_3", R.drawable.guileless_bopomofo_keyboard_tone_3),
    TONE_4("KEYCODE_4", R.drawable.guileless_bopomofo_keyboard_tone_4),
    TONE_LIGHT("KEYCODE_7", R.drawable.guileless_bopomofo_keyboard_tone_light),
}
