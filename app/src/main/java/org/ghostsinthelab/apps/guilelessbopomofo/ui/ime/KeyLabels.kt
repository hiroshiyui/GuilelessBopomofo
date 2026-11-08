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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SwipeKeySpec

/**
 * What a screen reader says for a key that types. The keycap drawables are named after
 * what they show (`guileless_bopomofo_keyboard_hsu_x_shi` shows ㄒ and ㄕ), so the label
 * is read off the drawable's name; a QWERTY keycap, or a name this does not understand,
 * falls back to the key code's own character.
 */
object KeyLabels {
    private const val KEYCAP_PREFIX = "guileless_bopomofo_keyboard_"
    private const val QWERTY_KEYCAP = "qwerty_"
    private val LAYOUT_PREFIXES = listOf("dachen_cp26_", "hsu_", "et26_")
    private const val KEY_CODE_PREFIX = "KEYCODE_"
    private const val SYMBOL_SEPARATOR = " "

    private val glyphsByToken: Map<String, BopomofoGlyph> = buildMap {
        BopomofoGlyph.entries.forEach { glyph ->
            put(glyph.name.lowercase(), glyph)
            // tone marks are spelt both `tone_4` and `tone4` across the drawables
            put(glyph.name.lowercase().replace("_", ""), glyph)
        }
    }

    private val keyCodeCharacters = mapOf(
        "GRAVE" to "`", "MINUS" to "-", "EQUALS" to "=", "LEFT_BRACKET" to "[", "RIGHT_BRACKET" to "]",
        "BACKSLASH" to "\\", "SEMICOLON" to ";", "APOSTROPHE" to "'", "COMMA" to ",", "PERIOD" to ".",
        "SLASH" to "/",
    )

    /** The label of the key whose keycap drawable is named [iconName] and which types [keyCode]. */
    fun forCharacterKey(iconName: String, keyCode: String): String =
        bopomofoSymbolsOf(iconName) ?: forKeyCode(keyCode)

    /** The label of a key on the compact keypad: what a tap types, then what the swipes type. */
    fun forSwipeKey(spec: SwipeKeySpec): String =
        listOfNotNull(spec.tap, spec.up, spec.right, spec.left, spec.down)
            .joinToString(SYMBOL_SEPARATOR) { it.symbol }

    /** The character a key code stands for: `KEYCODE_A` is "A", `KEYCODE_GRAVE` is "`". */
    fun forKeyCode(keyCode: String): String {
        val name = keyCode.removePrefix(KEY_CODE_PREFIX)
        return keyCodeCharacters[name] ?: name
    }

    /** The Bopomofo symbols a keycap drawable shows, or null for a QWERTY keycap or an unknown name. */
    private fun bopomofoSymbolsOf(iconName: String): String? {
        if (!iconName.startsWith(KEYCAP_PREFIX) || QWERTY_KEYCAP in iconName) return null
        var body = iconName.removePrefix(KEYCAP_PREFIX)
        LAYOUT_PREFIXES.firstOrNull { body.startsWith(it) }?.let { body = body.removePrefix(it) }
        return glyphsOf(body.split('_'))?.joinToString(SYMBOL_SEPARATOR) { it.symbol }
    }

    /** The glyphs named by [tokens], where a tone takes two tokens (`tone`, `4`); null if any is unknown. */
    private fun glyphsOf(tokens: List<String>): List<BopomofoGlyph>? {
        val glyphs = mutableListOf<BopomofoGlyph>()
        var index = 0
        while (index < tokens.size) {
            val single = glyphsByToken[tokens[index]]
            val pair = tokens.getOrNull(index + 1)?.let { glyphsByToken["${tokens[index]}_$it"] }
            when {
                pair != null -> { glyphs += pair; index += 2 }
                single != null -> { glyphs += single; index += 1 }
                else -> return null
            }
        }
        return glyphs.takeIf { it.isNotEmpty() }
    }
}
