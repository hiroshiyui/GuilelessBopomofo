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

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import android.text.InputType
import java.util.Locale

object EnglishWords {
    private const val APOSTROPHE = '\''

    private fun Char.isWordChar(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this == APOSTROPHE

    /**
     * The English word the cursor sits at the end of, or an empty string if there is none.
     * Anything else, a Han character say, ends it, and so do quotes in front of it:
     * `我喜歡Andr` gives `Andr`, `'hel` gives `hel`.
     */
    fun trailingWord(textBeforeCursor: CharSequence): String {
        val start = textBeforeCursor.indexOfLast { !it.isWordChar() } + 1
        return textBeforeCursor.substring(start).trimStart(APOSTROPHE)
    }

    /**
     * Writes [word] the way [prefix] was typed: `TH` makes it all capitals, `Th` capitalizes
     * it, and anything else leaves it as the dictionary spells it, so that `i` still gives `I`.
     */
    fun matchCase(word: String, prefix: String): String = when {
        prefix.length > 1 && prefix.none { it.isLowerCase() } -> word.uppercase(Locale.ROOT)
        prefix.first().isUpperCase() -> word.replaceFirstChar { it.uppercaseChar() }
        else -> word
    }

    /**
     * Whether a text field of the given input type wants word suggestions at all: plain text
     * fields do, unless they hold a password or have asked not to get any.
     */
    fun fieldAllowsSuggestions(inputType: Int): Boolean {
        if (inputType and InputType.TYPE_MASK_CLASS != InputType.TYPE_CLASS_TEXT) return false
        if (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0) return false
        return inputType and InputType.TYPE_MASK_VARIATION !in PASSWORD_VARIATIONS
    }

    private val PASSWORD_VARIATIONS = setOf(
        InputType.TYPE_TEXT_VARIATION_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
        InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
    )
}
