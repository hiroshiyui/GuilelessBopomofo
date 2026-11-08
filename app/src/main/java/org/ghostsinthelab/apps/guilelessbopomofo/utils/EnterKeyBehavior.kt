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

package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.view.inputmethod.EditorInfo

/**
 * What the `Enter` key should do for the text field currently being edited.
 */
sealed interface EnterKeyBehavior {
    /** Behave as a plain Enter key: insert a line break, or let the target app deal with it. */
    data object NewLine : EnterKeyBehavior

    /** Trigger the action the text field asked for: go, search, send, next, done, ... */
    data class EditorAction(val actionId: Int) : EnterKeyBehavior
}

/**
 * Works out whether Enter should commit an editor action or stay an ordinary Enter key,
 * following the rules of [android.inputmethodservice.InputMethodService.sendDefaultEditorAction].
 *
 * One of those rules is optional here: [EditorInfo.IME_FLAG_NO_ENTER_ACTION], which TextView
 * sets for every multiple line field, forces a line break only when
 * `lineBreakInMultiLineFields` says so. The on-screen Enter keeps that rule, like every
 * other IME. A physical Enter drops it, so that a field that declares an action (Send,
 * Done, ...) gets that action from a bare Enter and a line break from Shift+Enter or
 * Alt+Enter, see [org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent].
 */
object EnterKeyBehaviorResolver {
    /** What Enter should do in the text field described by [editorInfo]. */
    fun resolve(editorInfo: EditorInfo, lineBreakInMultiLineFields: Boolean): EnterKeyBehavior = resolve(
        imeOptions = editorInfo.imeOptions,
        actionId = editorInfo.actionId,
        hasCustomActionLabel = editorInfo.actionLabel != null,
        lineBreakInMultiLineFields = lineBreakInMultiLineFields,
    )

    /** The same, from the parts of an [EditorInfo], for tests that have no real one. */
    fun resolve(
        imeOptions: Int,
        actionId: Int,
        hasCustomActionLabel: Boolean,
        lineBreakInMultiLineFields: Boolean,
    ): EnterKeyBehavior {
        // The text field explicitly wants Enter to remain Enter. Multiple line text fields
        // land here as well, since TextView sets this flag for them on our behalf.
        if (lineBreakInMultiLineFields && (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return EnterKeyBehavior.NewLine
        }

        // A custom action label ("Send", "Join", ...) carries its own action id, which is
        // unrelated to the action encoded in imeOptions.
        if (hasCustomActionLabel && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            return EnterKeyBehavior.EditorAction(actionId)
        }

        return when (val imeAction = imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_UNSPECIFIED, EditorInfo.IME_ACTION_NONE -> EnterKeyBehavior.NewLine
            else -> EnterKeyBehavior.EditorAction(imeAction)
        }
    }
}
