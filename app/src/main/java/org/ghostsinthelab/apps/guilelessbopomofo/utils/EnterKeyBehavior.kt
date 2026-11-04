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
 * following the same rules as [android.inputmethodservice.InputMethodService.sendDefaultEditorAction]
 * so that we behave like every other well-mannered IME.
 */
object EnterKeyBehaviorResolver {
    /** What Enter should do in the text field described by [editorInfo]. */
    fun resolve(editorInfo: EditorInfo): EnterKeyBehavior = resolve(
        imeOptions = editorInfo.imeOptions,
        actionId = editorInfo.actionId,
        hasCustomActionLabel = editorInfo.actionLabel != null,
    )

    /** The same, from the parts of an [EditorInfo], for tests that have no real one. */
    fun resolve(imeOptions: Int, actionId: Int, hasCustomActionLabel: Boolean): EnterKeyBehavior {
        // The text field explicitly wants Enter to remain Enter. Multiple line text fields
        // land here as well, since TextView sets this flag for them on our behalf.
        if ((imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
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
