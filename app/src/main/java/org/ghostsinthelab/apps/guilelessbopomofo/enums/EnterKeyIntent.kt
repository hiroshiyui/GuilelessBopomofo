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

package org.ghostsinthelab.apps.guilelessbopomofo.enums

import android.view.KeyEvent

/**
 * What the user meant by pressing the Enter key.
 */
enum class EnterKeyIntent {
    /** Leave it to the text field: go, search, send, or a line break when it asks for none. */
    EDITOR_ACTION,

    /** A line break, whichever action the text field declares. */
    LINE_BREAK,

    ;

    companion object {
        // KeyEvent.isShiftPressed() and isAltPressed() look at META_SHIFT_ON and META_ALT_ON
        // alone, which the input pipeline normalizes in for us. Accept the side specific bits
        // too, so that a keyboard which reports only those is not left without a line break.
        private const val LINE_BREAK_META_STATE =
            KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON or KeyEvent.META_SHIFT_RIGHT_ON or
                    KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON or KeyEvent.META_ALT_RIGHT_ON

        /**
         * `Shift` + `Enter` or `Alt` + `Enter` is how one asks a physical keyboard for a line
         * break without submitting the text field.
         */
        fun forMetaState(metaState: Int): EnterKeyIntent =
            if ((metaState and LINE_BREAK_META_STATE) != 0) LINE_BREAK else EDITOR_ACTION
    }
}
