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

/** How a key is coloured. */
enum class KeyStyle { NORMAL, SPECIAL, ENTER }

/** The keys that do something other than type a character. */
enum class FunctionKey(@param:DrawableRes val icon: Int, val style: KeyStyle) {
    MODE_SWITCH_TO_ENGLISH(R.drawable.ic_keyboard_mode_switch_eng, KeyStyle.SPECIAL),
    MODE_SWITCH_TO_BOPOMOFO(R.drawable.ic_keyboard_mode_switch_bopomofo, KeyStyle.SPECIAL),
    SYMBOL(R.drawable.ic_keyboard_symbol_chooser_24, KeyStyle.SPECIAL),
    IME_SWITCH(R.drawable.ic_keyboard_ime_switch_24, KeyStyle.SPECIAL),
    SPACE(R.drawable.ic_keyboard_space_24, KeyStyle.NORMAL),
    PUNCTUATION(R.drawable.guileless_bopomofo_keyboard_punctuation, KeyStyle.SPECIAL),
    BACKSPACE(R.drawable.ic_keyboard_backspace_24, KeyStyle.SPECIAL),
    ENTER(R.drawable.ic_keyboard_return_24, KeyStyle.ENTER),
    SHIFT(R.drawable.ic_keyboard_shift_24, KeyStyle.SPECIAL),
}

/** One key of an on-screen keyboard; [weight] is its share of the row's width. */
sealed interface KeySpec {
    val weight: Float

    /** Types the key named by [keyCode], `KEYCODE_A` say. */
    data class Character(val keyCode: String, @param:DrawableRes val icon: Int, override val weight: Float = 1f) : KeySpec

    data class Function(val key: FunctionKey, override val weight: Float = 1f) : KeySpec
}

/** An on-screen keyboard, top row first. */
data class KeyboardSpec(val rows: List<List<KeySpec>>)

/** The on-screen keyboards there are. */
enum class SoftKeyboard(val isKeypad: Boolean = false) {
    DACHEN, DACHEN_CP26, ET41, ET26, ET26_QWERTY, HSU, HSU_QWERTY, QWERTY,

    /** Drawn from [CompactDachenKeypad] rather than a [KeyboardSpec]. */
    DACHEN_COMPACT(isKeypad = true),
}

/**
 * The screens a keyboard is arranged differently for. Mirrors how Android used to pick
 * between `layout`, `layout-land` and `layout-sw600dp`: the smallest width wins over the
 * orientation.
 */
enum class KeyboardVariant {
    DEFAULT, LANDSCAPE, LARGE_SCREEN;

    companion object {
        private const val LARGE_SCREEN_SMALLEST_WIDTH_DP = 600

        fun of(smallestScreenWidthDp: Int, isLandscape: Boolean): KeyboardVariant = when {
            smallestScreenWidthDp >= LARGE_SCREEN_SMALLEST_WIDTH_DP -> LARGE_SCREEN
            isLandscape -> LANDSCAPE
            else -> DEFAULT
        }
    }
}
