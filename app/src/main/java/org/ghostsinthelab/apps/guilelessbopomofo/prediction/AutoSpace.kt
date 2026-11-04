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

/**
 * The space a chosen suggestion is committed with, until something else happens in the text
 * field. Punctuation typed right after the word takes it back, so that `word ,` is `word,`.
 */
class AutoSpace {
    private var context: String? = null

    /** How much text before the cursor [takesBack] wants to see, zero when nothing is pending. */
    val lookbehind: Int get() = context?.length ?: 0

    /** A suggestion was just committed as [word] followed by a space. */
    fun committed(word: String) {
        context = "$word "
    }

    /** The cursor is no longer right after the space, or the text field has changed. */
    fun forget() {
        context = null
    }

    /**
     * Whether the space should go before [committed] is written, given the [textBeforeCursor]
     * as it is now. The space is only taken back when the text still ends with the very word
     * and space that were committed, so that a space of the user's own, elsewhere in the text,
     * is never eaten. Either way nothing is pending afterwards.
     */
    fun takesBack(textBeforeCursor: CharSequence?, committed: CharSequence): Boolean {
        val pending = context ?: return false
        context = null
        return EnglishWords.closesUpToPreviousWord(committed) &&
                textBeforeCursor?.endsWith(pending) == true
    }
}
